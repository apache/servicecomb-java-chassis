/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.ParameterAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.core.processor.parameter.AbstractParameterProcessor;
import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.Parameter;

/**
 * For {@link javax.ws.rs.BeanParam}
 */
public class BeanParamAnnotationProcessor implements ParameterAnnotationProcessor {
  public static final Set<Class<?>> SUPPORTED_PARAM_ANNOTATIONS = new HashSet<>();

  public static final String SETTER_METHOD_PREFIX = "set";

  static {
    SUPPORTED_PARAM_ANNOTATIONS.add(PathParam.class);
    SUPPORTED_PARAM_ANNOTATIONS.add(QueryParam.class);
    SUPPORTED_PARAM_ANNOTATIONS.add(HeaderParam.class);
    SUPPORTED_PARAM_ANNOTATIONS.add(CookieParam.class);
    SUPPORTED_PARAM_ANNOTATIONS.add(FormParam.class);
  }

  @Override
  public void process(Object annotation, OperationGenerator operationGenerator, int paramIdx) {
    final Class<?> beanParamClazz = operationGenerator.getProviderMethod().getParameterTypes()[paramIdx];
    Map<String, Parameter> swaggerParamMap = new HashMap<>();
    try {
      // traversal fields, get those JAX-RS params
      traversalParamField(operationGenerator, beanParamClazz, swaggerParamMap);
    } catch (IllegalArgumentException | IntrospectionException e) {
      throw new Error(String.format(
          "Processing param failed, method=%s:%s, beanParamIdx=%d",
          operationGenerator.getProviderMethod().getDeclaringClass().getName(),
          operationGenerator.getProviderMethod().getName(),
          paramIdx)
          , e);
    }

    // set swagger params into operationGenerator, in declared field order
    Field[] declaredProducerFields = beanParamClazz.getDeclaredFields();
    Arrays.stream(declaredProducerFields)
        .map(declaredProducerField -> swaggerParamMap.get(declaredProducerField.getName()))
        .filter(Objects::nonNull)
        .forEach(operationGenerator::addProviderParameter);
  }

  /**
   * Traversal fields of {@code beanParamClazz},
   * generate swagger params according to JAX-RS param annotations and set them into {@code swaggerParamMap}.
   *
   * @param swaggerParamMap the map contains the generated swagger param,
   * key is the name defined by source code(the declared field name) and value is the swagger param
   */
  private void traversalParamField(OperationGenerator operationGenerator, Class<?> beanParamClazz,
      Map<String, Parameter> swaggerParamMap) throws IntrospectionException {
    for (Field beanParamField : beanParamClazz.getDeclaredFields()) {
      // ignore synthetic member to avoid build failure
      // see https://github.com/jacoco/jacoco/issues/168
      if (fieldShouldIgnore(beanParamField) || beanParamField.isSynthetic()) {
        continue;
      }
      // try to process this field directly
      Parameter swaggerParam = generateSwaggerParam(operationGenerator, beanParamField.getAnnotations(),
          beanParamField.getGenericType());
      if (null == swaggerParam) {
        // if swaggerParam is null, maybe the JAX-RS param annotation is tagged onto the write method
        swaggerParam = processFieldSetter(operationGenerator, beanParamClazz, beanParamField);
      }

      if (null == swaggerParam) {
        throw new IllegalArgumentException(String.format(
            "There is a field[%s] cannot be mapped to swagger param. Maybe you should tag @JsonIgnore on it.",
            beanParamField.getName())
        );
      }
      swaggerParamMap.put(beanParamField.getName(), swaggerParam);
    }
  }

  /**
   * Sometimes user may tag JAX-RS param annotations on setter method instead of fields.
   *
   * @param beanParamClazz class of the BeanParam
   * @param beanParamField the field of BeanParam whose setter method is processed
   * @return the generated swagger param, or null if the setter method is not tagged by JAX-RS param annotations
   * @throws IntrospectionException see {@linkplain PropertyDescriptor#PropertyDescriptor(String, Class)}
   */
  private Parameter processFieldSetter(OperationGenerator operationGenerator, Class<?> beanParamClazz,
      Field beanParamField) throws IntrospectionException {
    Parameter swaggerParam = null;
    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanParamField.getName(), beanParamClazz);
    Method writeMethod = propertyDescriptor.getWriteMethod();
    if (null != writeMethod) {
      swaggerParam = generateSwaggerParam(operationGenerator, writeMethod.getAnnotations(),
          beanParamField.getGenericType());
    }
    return swaggerParam;
  }

  /**
   * Generate a swagger parameter according to {@code annotations} and {@code genericType}.
   *
   * @param operationGenerator operationGenerator
   * @param annotations annotations on fields or setter methods
   * @param genericType type of the fields, or the param type of the setter methods
   * @return a swagger param, or null if there is no JAX-RS annotation in {@code annotations}
   */
  private Parameter generateSwaggerParam(
      OperationGenerator operationGenerator,
      Annotation[] annotations,
      Type genericType) {
    String defaultValue = null;
    for (Annotation fieldAnnotation : annotations) {
      if (!SUPPORTED_PARAM_ANNOTATIONS.contains(fieldAnnotation.annotationType())) {
        if (fieldAnnotation instanceof DefaultValue) {
          defaultValue = ((DefaultValue) fieldAnnotation).value();
        }
        continue;
      }

      return setUpParameter(operationGenerator, fieldAnnotation, genericType, defaultValue);
    }
    return null;
  }

  /**
   * Generate swagger parameter, set default value, and return it.
   *
   * @param operationGenerator operationGenerator
   * @param fieldAnnotation JAX-RS param annotation
   * @param genericParamType type of the parameter
   * @param defaultValue default value, can be null
   * @return the generated swagger Parameter
   */
  private Parameter setUpParameter(
      OperationGenerator operationGenerator,
      Annotation fieldAnnotation,
      Type genericParamType,
      String defaultValue) {
    AbstractSerializableParameter<?> parameter = createParameter(
        operationGenerator.getContext(),
        fieldAnnotation,
        genericParamType);

    if (null != defaultValue) {
      parameter.setDefaultValue(defaultValue);
    }
    return parameter;
  }

  /**
   * Generate a swagger parameter, set up name and type info.
   *
   * @param swaggerGeneratorContext context data carried by {@linkplain OperationGenerator}
   * @param fieldAnnotation JAX-RS param annotation
   * @param genericParamType default value, can be null
   * @return the generated swagger parameter
   */
  private AbstractSerializableParameter<?> createParameter(
      SwaggerGeneratorContext swaggerGeneratorContext,
      Annotation fieldAnnotation,
      Type genericParamType) {
    // find the corresponding ParameterProcessor and process the parameter
    final AbstractParameterProcessor<?> parameterAnnotationProcessor =
        (AbstractParameterProcessor<?>) swaggerGeneratorContext
            .findParameterAnnotationProcessor(fieldAnnotation.annotationType());
    AbstractSerializableParameter<?> parameter = parameterAnnotationProcessor.createParameter();
    String paramName = parameterAnnotationProcessor.getAnnotationParameterName(fieldAnnotation);
    parameter.setName(paramName);
    ParamUtils.setParameterType(genericParamType, parameter);
    return parameter;
  }

  /**
   * Those fields tagged by @JsonIgnore should be ignored.
   */
  private boolean fieldShouldIgnore(Field beanParamField) {
    for (Annotation annotation : beanParamField.getAnnotations()) {
      if (annotation instanceof JsonIgnore) {
        return true;
      }
    }
    return false;
  }
}
