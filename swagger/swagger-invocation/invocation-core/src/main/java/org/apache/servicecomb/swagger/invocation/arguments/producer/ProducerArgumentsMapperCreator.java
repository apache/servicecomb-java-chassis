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
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.LambdaMetafactoryUtils;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.invocation.arguments.AbstractArgumentsMapperCreator;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ContextArgumentMapperFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ProducerArgumentsMapperCreator extends AbstractArgumentsMapperCreator {
  // swagger parameter types relate to producer
  // because features of @BeanParam/query, and rpc mode parameter wrapper
  // types is not always equals to producerMethod parameter types directly
  private Type[] swaggerParameterTypes;

  public ProducerArgumentsMapperCreator(SerializationConfig serializationConfig,
      Map<Class<?>, ContextArgumentMapperFactory> contextFactorys,
      Method producerMethod, SwaggerOperation swaggerOperation) {
    super(serializationConfig, contextFactorys, producerMethod, swaggerOperation);

    swaggerParameterTypes = new Type[swaggerOperation.getOperation().getParameters().size()];
  }

  public Type[] getSwaggerParameterTypes() {
    return swaggerParameterTypes;
  }

  public ProducerArgumentsMapper createArgumentsMapper() {
    doCreateArgumentsMapper();
    return new ProducerArgumentsMapper(mappers, providerMethod.getParameterCount());
  }

  @Override
  protected void processUnknownParameter(String parameterName) {
    throw new IllegalStateException(String
        .format("failed to find producer parameter in contract, method=%s:%s, parameter name=%s.",
            providerMethod.getDeclaringClass().getName(), providerMethod.getName(), parameterName));
  }

  @Override
  protected ArgumentMapper createKnownParameterMapper(int producerParamIdx, Integer swaggerIdx) {
    swaggerParameterTypes[swaggerIdx] = providerMethod.getGenericParameterTypes()[producerParamIdx];
    return new ProducerArgumentSame(swaggerIdx, producerParamIdx);
  }

  @Override
  protected ArgumentMapper createSwaggerBodyFieldMapper(int producerParamIdx, String parameterName,
      int swaggerBodyIdx) {
    swaggerParameterTypes[swaggerBodyIdx] = Object.class;
    return new SwaggerBodyFieldToProducerArgument(producerParamIdx, parameterName,
        providerMethod.getGenericParameterTypes()[producerParamIdx], swaggerBodyIdx);
  }

  @Override
  protected void processBeanParameter(int producerParamIdx, Parameter producerParameter) {
    ProducerBeanParamMapper mapper = new ProducerBeanParamMapper(producerParamIdx, producerParameter.getType());
    JavaType producerType = TypeFactory.defaultInstance().constructType(producerParameter.getParameterizedType());
    for (BeanPropertyDefinition propertyDefinition : serializationConfig.introspect(producerType)
        .findProperties()) {
      String parameterName = collectParameterName(providerMethod, propertyDefinition);
      Integer swaggerIdx = findAndClearSwaggerParameterIndex(parameterName);
      if (swaggerIdx == null) {
        throw new IllegalStateException(String
            .format("failed to find producer parameter in contract, method=%s:%s, bean parameter name=%s.",
                providerMethod.getDeclaringClass().getName(), providerMethod.getName(), parameterName));
      }

      Setter<Object, Object> setter;
      if (propertyDefinition.hasSetter()) {
        setter = LambdaMetafactoryUtils.createLambda(propertyDefinition.getSetter().getAnnotated(), Setter.class);
      } else {
        setter = LambdaMetafactoryUtils.createSetter(propertyDefinition.getField().getAnnotated());
      }

      swaggerParameterTypes[swaggerIdx] = propertyDefinition.getPrimaryType();
      mapper.addField(swaggerIdx, setter);
    }
    mappers.add(mapper);
  }
}
