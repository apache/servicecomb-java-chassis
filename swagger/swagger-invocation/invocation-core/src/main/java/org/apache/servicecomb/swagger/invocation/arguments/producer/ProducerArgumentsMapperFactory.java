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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.servicecomb.swagger.generator.rest.RestSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapperConfig;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.ContextArgumentMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.FieldInfo;
import org.apache.servicecomb.swagger.invocation.arguments.ProviderParameter;
import org.apache.servicecomb.swagger.invocation.converter.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.swagger.models.parameters.Parameter;

@Component
@Primary
public class ProducerArgumentsMapperFactory extends ArgumentsMapperFactory<ProducerArgumentsMapper> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerArgumentsMapperFactory.class);

  public ProducerArgumentsMapperFactory() {
    type = InvocationType.PRODUCER;
  }

  @Inject
  @Qualifier("producer")
  public void setFactoryList(List<ContextArgumentMapperFactory> factoryList) {
    createFactoryMap(factoryList);
  }

  @Override
  public ProducerArgumentsMapper createArgumentsMapper(ArgumentsMapperConfig config) {
    collectArgumentsMapper(config);
    return new ProducerArgumentsMapper(config.getArgumentMapperList(),
        config.getProviderMethod().getParameterCount());
  }

  public boolean canProcess(ArgumentsMapperConfig config) {
    return false;
  }

  @Override
  protected void collectSwaggerArgumentsMapper(ArgumentsMapperConfig config,
      List<ProviderParameter> providerNormalParams) {
    if (!RestSwaggerGeneratorContext.class.isInstance(config.getSwaggerGeneratorContext())) {
      // POJO style provider does not support aggregated param
      super.collectSwaggerArgumentsMapper(config, providerNormalParams);
      return;
    }

    // JAX-RS and SpringMVC style provider support aggregated param, i.e. @BeanParam and query object, respectively
    Map<String, ProviderParameter> providerParamMap = getProviderParamMap(providerNormalParams);
    Map<String, ParamWrapper<Parameter>> swaggerParamMap = getSwaggerParamMap(config);

    Set<String> aggregatedParamNames = findAggregatedParamNames(providerParamMap, swaggerParamMap);
    if (aggregatedParamNames.isEmpty()) {
      // there is no aggregated param, run as 1-to-1 param mapping mode
      super.collectSwaggerArgumentsMapper(config, providerNormalParams);
      return;
    }

    // There is at lease one aggregated param, so the param mapping mode becomes to M-to-N
    // try to map params by name
    generateParamMapperByName(config, providerParamMap, swaggerParamMap, aggregatedParamNames);
  }

  private void generateParamMapperByName(ArgumentsMapperConfig config, Map<String, ProviderParameter> providerParamMap,
      Map<String, ParamWrapper<Parameter>> swaggerParamMap, Set<String> aggregatedParamNames) {
    LOGGER.info("mapping aggregated params: [{}]", aggregatedParamNames);
    generateAggregatedParamMapper(config, providerParamMap, swaggerParamMap, aggregatedParamNames);
    generateDefaultParamMapper(config, providerParamMap, swaggerParamMap, aggregatedParamNames);
  }

  /**
   * Generate default argument mappers. One swagger argument is mapped to one producer argument.
   */
  private void generateDefaultParamMapper(ArgumentsMapperConfig config, Map<String, ProviderParameter> providerParamMap,
      Map<String, ParamWrapper<Parameter>> swaggerParamMap, Set<String> aggregatedParamNames) {
    Type[] swaggerParamTypes = config.getSwaggerMethod().getGenericParameterTypes();
    for (Entry<String, ProviderParameter> providerParamEntry : providerParamMap.entrySet()) {
      if (aggregatedParamNames.contains(providerParamEntry.getKey())) {
        continue;
      }

      final int swaggerIdx = swaggerParamMap.get(providerParamEntry.getKey()).getIndex();
      Converter converter = converterMgr.findConverter(type, providerParamEntry.getValue().getType(),
          swaggerParamTypes[swaggerIdx]);
      ArgumentMapper mapper =
          createArgumentMapperWithConverter(swaggerIdx, providerParamEntry.getValue().getIndex(), converter);
      config.addArgumentMapper(mapper);
    }
  }

  /**
   * Generate argument mappers for aggregated params.
   * Collect related swagger params and map them to an aggregated param.
   * It's implemented by SpringMVC and JAX-RS.
   */
  protected void generateAggregatedParamMapper(ArgumentsMapperConfig config,
      Map<String, ProviderParameter> providerParamMap, Map<String, ParamWrapper<Parameter>> swaggerParamMap,
      Set<String> aggregatedParamNames) {
    // do nothing, not supported by default
  }

  protected Map<String, ParamWrapper<Parameter>> getSwaggerParamMap(ArgumentsMapperConfig config) {
    Map<String, ParamWrapper<Parameter>> swaggerParamMap = new HashMap<>();
    if (null == config.getSwaggerOperation() || null == config.getSwaggerOperation().getParameters()) {
      return swaggerParamMap;
    }
    List<Parameter> parameters = config.getSwaggerOperation().getParameters();
    for (int i = 0; i < parameters.size(); i++) {
      Parameter parameter = parameters.get(i);
      swaggerParamMap.put(parameter.getName(), new ParamWrapper<>(parameter).setIndex(i));
    }
    return swaggerParamMap;
  }

  protected Map<String, ProviderParameter> getProviderParamMap(List<ProviderParameter> providerNormalParams) {
    Map<String, ProviderParameter> providerParamMap = new HashMap<>(providerNormalParams.size());
    providerNormalParams.forEach(
        providerParameter -> providerParamMap.put(providerParameter.getName(), providerParameter));
    return providerParamMap;
  }

  /**
   * Find all aggregated params
   * @return the names of the aggregated params
   */
  protected Set<String> findAggregatedParamNames(Map<String, ProviderParameter> providerParamMap,
      Map<String, ParamWrapper<Parameter>> swaggerParamMap) {
    return Collections.emptySet();
  }

  @Override
  protected ArgumentMapper createArgumentMapperWithConverter(int swaggerIdx, int producerIdx, Converter converter) {
    return new ProducerArgumentSame(swaggerIdx, producerIdx, converter);
  }

  @Override
  protected ArgumentMapper createBodyFieldArgMapper(ArgumentsMapperConfig config,
      Map<Integer, FieldInfo> fieldMap) {
    return new SwaggerArgumentToProducerBodyField(fieldMap);
  }

  public static class ParamWrapper<T> {
    T param;

    int index;

    public ParamWrapper(T param) {
      this.param = param;
    }

    public T getParam() {
      return param;
    }

    public ParamWrapper<T> setParam(T param) {
      this.param = param;
      return this;
    }

    public int getIndex() {
      return index;
    }

    public ParamWrapper<T> setIndex(int index) {
      this.index = index;
      return this;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("ParamWrapper{");
      sb.append("param=").append(param);
      sb.append(", index=").append(index);
      sb.append('}');
      return sb.toString();
    }
  }
}
