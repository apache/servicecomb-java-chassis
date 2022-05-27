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

package org.apache.servicecomb.swagger.invocation.arguments.consumer;

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.collectParameterName;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.LambdaMetafactoryUtils;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.invocation.arguments.AbstractArgumentsMapperCreator;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ContextArgumentMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.parameters.Parameter;

public class ConsumerArgumentsMapperCreator extends AbstractArgumentsMapperCreator {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerArgumentsMapperCreator.class);

  private int unknownConsumerParams = 0;

  public ConsumerArgumentsMapperCreator(SerializationConfig serializationConfig,
      Map<Class<?>, ContextArgumentMapperFactory> contextFactorys, Class<?> consumerClass,
      Method consumerMethod, SwaggerOperation swaggerOperation) {
    super(serializationConfig, contextFactorys, consumerClass, consumerMethod, swaggerOperation);
  }

  private boolean isAllSameMapper() {
    for (ArgumentMapper mapper : mappers) {
      if (mapper instanceof ConsumerArgumentSame && ((ConsumerArgumentSame) mapper).isSameMapping()) {
        continue;
      }

      return false;
    }

    return true;
  }

  public ArgumentsMapper createArgumentsMapper() {
    doCreateArgumentsMapper();

    // if all mappers are SameMapper, then no need any mapper
    if (unknownConsumerParams == 0
        && mappers.size() == swaggerOperation.getOperation().getParameters().size()
        && isAllSameMapper()) {
      return new ArgumentsMapperDirectReuse();
    }

    return new ArgumentsMapperCommon(mappers);
  }

  @Override
  protected void processUnknownParameter(String parameterName) {
    // real unknown parameter, new consumer invoke old producer, just ignore this parameter
    LOGGER.warn("new consumer invoke old version producer, parameter({}) is not exist in contract, method={}:{}.",
        parameterName, providerMethod.getDeclaringClass().getName(), providerMethod.getName());
    unknownConsumerParams++;
  }

  @Override
  protected void processPendingSwaggerParameter(Parameter parameter) {

  }

  @Override
  protected ArgumentMapper createKnownParameterMapper(int consumerParamIdx, Integer swaggerIdx) {
    return new ConsumerArgumentSame(this.providerMethod.getParameters()[consumerParamIdx].getName(),
        this.swaggerParameters.get(swaggerIdx).getName());
  }

  @Override
  protected ArgumentMapper createSwaggerBodyFieldMapper(int consumerParamIdx, String parameterName,
      int swaggerBodyIdx) {
    return new ConsumerArgumentToBodyField(this.providerMethod.getParameters()[consumerParamIdx].getName(),
        this.swaggerParameters.get(swaggerBodyIdx).getName(), parameterName);
  }

  @Override
  protected void processBeanParameter(int consumerParamIdx, java.lang.reflect.Parameter consumerParameter) {
    ConsumerBeanParamMapper mapper = new ConsumerBeanParamMapper(
        this.providerMethod.getParameters()[consumerParamIdx].getName());
    JavaType consumerType = TypeFactory.defaultInstance().constructType(consumerParameter.getParameterizedType());
    for (BeanPropertyDefinition propertyDefinition : serializationConfig.introspect(consumerType).findProperties()) {
      String parameterName = collectParameterName(providerMethod, propertyDefinition);
      Integer swaggerIdx = findSwaggerParameterIndex(parameterName);
      if (swaggerIdx == null) {
        // unknown field, ignore it
        LOGGER.warn(
            "new consumer invoke old version producer, bean parameter({}) is not exist in contract, method={}:{}.",
            parameterName, providerMethod.getDeclaringClass().getName(), providerMethod.getName());
        continue;
      }

      mapper.addField(parameterName, LambdaMetafactoryUtils.createObjectGetter(propertyDefinition));
      processedSwaggerParamters.add(parameterName);
    }
    mappers.add(mapper);
  }
}
