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

package org.apache.servicecomb.swagger.invocation.arguments.producer;

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.collectParameterName;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.LambdaMetafactoryUtils;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.invocation.arguments.AbstractArgumentsMapperCreator;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ContextArgumentMapperFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.reflect.TypeToken;
import com.netflix.config.DynamicPropertyFactory;

import io.swagger.models.parameters.SerializableParameter;

public class ProducerArgumentsMapperCreator extends AbstractArgumentsMapperCreator {
  // using swagger type instead of method type for primitive method types when deserialize
  // query/header/etc. parameters.
  private final boolean useWrapperTypeForPrimitives = DynamicPropertyFactory.getInstance()
      .getBooleanProperty("servicecomb.rest.parameter.useWrapperTypeForPrimitives", false).get();

  // swagger parameter types relate to producer
  // because features of @BeanParam/query, and rpc mode parameter wrapper
  // types is not always equals to producerMethod parameter types directly
  private final Map<String, Type> swaggerParameterTypes;

  public ProducerArgumentsMapperCreator(SerializationConfig serializationConfig,
      Map<Class<?>, ContextArgumentMapperFactory> contextFactorys, Class<?> producerClass,
      Method producerMethod, SwaggerOperation swaggerOperation) {
    super(serializationConfig, contextFactorys, producerClass, producerMethod, swaggerOperation);

    swaggerParameterTypes = new HashMap<>();
  }

  public Map<String, Type> getSwaggerParameterTypes() {
    return swaggerParameterTypes;
  }

  public ProducerArgumentsMapper createArgumentsMapper() {
    doCreateArgumentsMapper();
    return new ProducerArgumentsMapper(mappers);
  }

  @Override
  protected void processUnknownParameter(int providerParamIdx, java.lang.reflect.Parameter providerParameter,
      String parameterName) {
    throw new IllegalStateException(String
        .format("failed to find producer parameter in contract, method=%s:%s, parameter name=%s.",
            providerMethod.getDeclaringClass().getName(), providerMethod.getName(), parameterName));
  }

  @Override
  protected void processPendingSwaggerParameter(io.swagger.models.parameters.Parameter parameter) {
    swaggerParameterTypes.put(parameter.getName(), swaggerTypes(parameter, Object.class));
  }

  @Override
  protected ArgumentMapper createKnownParameterMapper(int providerParamIdx, Integer swaggerIdx) {
    String swaggerArgumentName = swaggerParameters.get(swaggerIdx).getName();
    Type providerType = TypeToken.of(providerClass)
        .resolveType(providerMethod.getGenericParameterTypes()[providerParamIdx])
        .getType();
    if (useWrapperTypeForPrimitives && providerType instanceof Class<?> && ((Class<?>) providerType).isPrimitive()) {
      // add swagger type to java type
      providerType = swaggerTypes(swaggerParameters.get(swaggerIdx), providerType);
    }
    swaggerParameterTypes.put(swaggerArgumentName, providerType);
    return new ProducerArgumentSame(providerMethod.getParameters()[providerParamIdx].getName(), swaggerArgumentName);
  }

  private Type swaggerTypes(io.swagger.models.parameters.Parameter parameter, Type defaultType) {
    if (!(parameter instanceof SerializableParameter)) {
      return defaultType;
    }
    SerializableParameter serializableParameter = (SerializableParameter) parameter;
    switch (serializableParameter.getType()) {
      case "integer":
        if ("int64".equalsIgnoreCase(serializableParameter.getFormat())) {
          return Long.class;
        }
        return Integer.class;
      case "number":
        if ("double".equalsIgnoreCase(serializableParameter.getFormat())) {
          return Double.class;
        }
        return Float.class;
      case "boolean":
        return Boolean.class;
      default:
        return defaultType;
    }
  }

  @Override
  protected ArgumentMapper createSwaggerBodyFieldMapper(int producerParamIdx, String parameterName,
      int swaggerBodyIdx) {
    String swaggerArgumentName = swaggerParameters.get(swaggerBodyIdx).getName();
    swaggerParameterTypes.put(swaggerArgumentName, Object.class);
    Type parameterType = TypeToken.of(providerClass)
        .resolveType(providerMethod.getGenericParameterTypes()[producerParamIdx])
        .getType();
    return new SwaggerBodyFieldToProducerArgument(providerMethod.getParameters()[producerParamIdx].getName(),
        swaggerArgumentName,
        parameterName, parameterType);
  }

  @Override
  protected boolean processBeanParameter(int producerParamIdx, Parameter producerParameter) {
    JavaType providerType = TypeFactory.defaultInstance().constructType(producerParameter.getParameterizedType());
    if (!SwaggerUtils.isBean(providerType)) {
      return false;
    }
    ProducerBeanParamMapper mapper = new ProducerBeanParamMapper(
        providerMethod.getParameters()[producerParamIdx].getName(), producerParameter.getType());
    JavaType producerType = TypeFactory.defaultInstance().constructType(producerParameter.getParameterizedType());
    for (BeanPropertyDefinition propertyDefinition : serializationConfig.introspect(producerType)
        .findProperties()) {
      String parameterName = collectParameterName(providerMethod, propertyDefinition);

      Integer swaggerIdx = findSwaggerParameterIndex(parameterName);
      if (swaggerIdx == null) {
        throw new IllegalStateException(String
            .format("failed to find producer parameter in contract, method=%s:%s, bean parameter name=%s.",
                providerMethod.getDeclaringClass().getName(), providerMethod.getName(), parameterName));
      }

      swaggerParameterTypes.put(parameterName, propertyDefinition.getPrimaryType());
      mapper.addField(parameterName, LambdaMetafactoryUtils.createObjectSetter(propertyDefinition));
      processedSwaggerParamters.add(parameterName);
    }
    mappers.add(mapper);
    return true;
  }
}
