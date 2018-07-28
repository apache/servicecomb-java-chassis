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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
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

import io.swagger.models.parameters.AbstractSerializableParameter;

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
    try {
      // traversal fields, get those JAX-RS params
      processParamField(operationGenerator, beanParamClazz);
      // traversal setter methods, some setter method may also be tagged with param annotations
      processParamSetter(operationGenerator, beanParamClazz);
    } catch (IllegalArgumentException e) {
      throw new Error(String.format(
          "Processing param failed, method=%s:%s, beanParamIdx=%d",
          operationGenerator.getProviderMethod().getDeclaringClass().getName(),
          operationGenerator.getProviderMethod().getName(),
          paramIdx)
          , e);
    }
  }

  /**
   * Process those setter methods tagged by JAX-RS param annotations.
   */
  private void processParamSetter(OperationGenerator operationGenerator, Class<?> beanParamClazz) {
    for (Method method : beanParamClazz.getDeclaredMethods()) {
      if (!method.getName().startsWith(SETTER_METHOD_PREFIX)) {
        // only process setter methods
        continue;
      }
      // There should be one and only one param in a setter method
      final Type genericParamType = method.getGenericParameterTypes()[0];
      processBeanParamMember(operationGenerator, method.getAnnotations(), genericParamType);
    }
  }

  /**
   * Process those fields tagged by JAX-RS param annotations.
   */
  private void processParamField(OperationGenerator operationGenerator, Class<?> beanParamClazz) {
    for (Field beanParamField : beanParamClazz.getDeclaredFields()) {
      processBeanParamMember(operationGenerator, beanParamField.getAnnotations(), beanParamField.getGenericType());
    }
  }

  /**
   * Process a swagger parameter defined by field or setter method in this BeanParam.
   * After processing, a swagger parameter is generated and set into {@code operationGenerator}.
   *
   * @param operationGenerator operationGenerator
   * @param annotations annotations on fields or setter methods
   * @param genericType type of the fields, or the param type of the setter methods
   */
  private void processBeanParamMember(OperationGenerator operationGenerator, Annotation[] annotations,
      Type genericType) {
    String defaultValue = null;
    for (Annotation fieldAnnotation : annotations) {
      if (!SUPPORTED_PARAM_ANNOTATIONS.contains(fieldAnnotation.annotationType())) {
        if (fieldAnnotation instanceof DefaultValue) {
          defaultValue = ((DefaultValue) fieldAnnotation).value();
        }
        continue;
      }

      setUpParameter(operationGenerator, fieldAnnotation, genericType, defaultValue);
    }
  }

  /**
   * Generate swagger parameter, set default value, and add it into {@code operationGenerator}.
   *
   * @param operationGenerator operationGenerator
   * @param fieldAnnotation JAX-RS param annotation
   * @param genericParamType type of the parameter
   * @param defaultValue default value, can be null
   */
  private void setUpParameter(
      OperationGenerator operationGenerator,
      Annotation fieldAnnotation,
      Type genericParamType,
      String defaultValue) {
    AbstractSerializableParameter<?> parameter = generateParameter(
        operationGenerator.getContext(),
        fieldAnnotation,
        genericParamType);

    if (null != defaultValue) {
      parameter.setDefaultValue(defaultValue);
    }
    operationGenerator.addProviderParameter(parameter);
  }

  /**
   * Generate a swagger parameter, set up name and type info.
   *
   * @param swaggerGeneratorContext context data carried by {@linkplain OperationGenerator}
   * @param fieldAnnotation JAX-RS param annotation
   * @param genericParamType default value, can be null
   * @return the generated swagger parameter
   */
  private AbstractSerializableParameter<?> generateParameter(
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
}
