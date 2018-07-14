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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
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
import org.springframework.stereotype.Component;

import io.swagger.converter.ModelConverters;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

@Component
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

  @SuppressWarnings("unchecked")
  @Override
  public ProducerArgumentsMapper createArgumentsMapper(ArgumentsMapperConfig config) {
    collectArgumentsMapper(config);
    return new ProducerArgumentsMapper(config.getArgumentMapperList(),
        config.getProviderMethod().getParameterCount());
  }

  @Override
  protected void collectSwaggerArgumentsMapper(ArgumentsMapperConfig config,
      List<ProviderParameter> providerNormalParams) {
    if (!config.getSwaggerGeneratorContext().getClass().getCanonicalName().equals(
        "org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGeneratorContext")) {
      // if this is not a SpringMVC style provider operation, there is no need to consider query object param
      super.collectSwaggerArgumentsMapper(config, providerNormalParams);
      return;
    }

    Map<String, ProviderParameter> providerParamMap = getProviderParamMap(providerNormalParams);
    Map<String, ParamWrapper<Parameter>> swaggerParamMap = getSwaggerParamMap(config);

    Set<String> queryObjectNames = findSpringMvcQueryObject(providerParamMap, swaggerParamMap);
    if (queryObjectNames.isEmpty()) {
      // there is no query object param, run as 1-to-1 param mapping mode
      super.collectSwaggerArgumentsMapper(config, providerNormalParams);
      return;
    }

    // There is at lease one query object param, so the param mapping mode becomes to M-to-N
    // try to map params by name
    generateParamMapperByName(config, providerParamMap, swaggerParamMap, queryObjectNames);
  }

  private void generateParamMapperByName(ArgumentsMapperConfig config, Map<String, ProviderParameter> providerParamMap,
      Map<String, ParamWrapper<Parameter>> swaggerParamMap, Set<String> queryObjectNames) {
    LOGGER.info("mapping query object params: [{}]", queryObjectNames);
    generateObjectQueryParamMapper(config, providerParamMap, swaggerParamMap, queryObjectNames);
    generateDefaultParamMapper(config, providerParamMap, swaggerParamMap, queryObjectNames);
  }

  /**
   * Generate default argument mappers. One swagger argument is mapped to one producer argument.
   */
  private void generateDefaultParamMapper(ArgumentsMapperConfig config, Map<String, ProviderParameter> providerParamMap,
      Map<String, ParamWrapper<Parameter>> swaggerParamMap, Set<String> queryObjectNames) {
    Type[] swaggerParamTypes = config.getSwaggerMethod().getGenericParameterTypes();
    for (Entry<String, ProviderParameter> providerParamEntry : providerParamMap.entrySet()) {
      if (queryObjectNames.contains(providerParamEntry.getKey())) {
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
   * Generate argument mappers for query object params. Collect all query params as json and map them to object param.
   */
  private void generateObjectQueryParamMapper(ArgumentsMapperConfig config,
      Map<String, ProviderParameter> providerParamMap, Map<String, ParamWrapper<Parameter>> swaggerParamMap,
      Set<String> queryObjectNames) {
    // collect all query params
    Map<String, Integer> querySwaggerParamsIndex = new HashMap<>();
    for (Entry<String, ParamWrapper<Parameter>> wrapperEntry : swaggerParamMap.entrySet()) {
      if (wrapperEntry.getValue().getParam() instanceof QueryParameter) {
        querySwaggerParamsIndex.put(wrapperEntry.getKey(), wrapperEntry.getValue().getIndex());
      }
    }
    // create mapper for each query objects
    for (String queryObjectName : queryObjectNames) {
      final ProviderParameter providerParameter = providerParamMap.get(queryObjectName);
      ArgumentMapper mapper = new ProducerSpringMVCQueryObjectMapper(querySwaggerParamsIndex,
          providerParameter.getIndex(),
          providerParameter.getType());
      config.addArgumentMapper(mapper);
    }
  }

  private Map<String, ParamWrapper<Parameter>> getSwaggerParamMap(ArgumentsMapperConfig config) {
    Map<String, ParamWrapper<Parameter>> swaggerParamMap =
        new HashMap<>(config.getSwaggerOperation().getParameters().size());
    List<Parameter> parameters = config.getSwaggerOperation().getParameters();
    for (int i = 0; i < parameters.size(); i++) {
      Parameter parameter = parameters.get(i);
      swaggerParamMap.put(parameter.getName(), new ParamWrapper<>(parameter).setIndex(i));
    }
    return swaggerParamMap;
  }

  private Map<String, ProviderParameter> getProviderParamMap(List<ProviderParameter> providerNormalParams) {
    Map<String, ProviderParameter> providerParamMap = new HashMap<>(providerNormalParams.size());
    providerNormalParams.forEach(
        providerParameter -> providerParamMap.put(providerParameter.getName(), providerParameter));
    return providerParamMap;
  }

  /**
   * Find all query object params
   * @return the names of the query object params
   */
  private Set<String> findSpringMvcQueryObject(Map<String, ProviderParameter> providerParamMap,
      Map<String, ParamWrapper<Parameter>> swaggerParamMap) {
    // find all reference type producer params, and exclude body param
    Set<String> queryObjectSet = new HashSet<>();

    for (Entry<String, ProviderParameter> paramEntry : providerParamMap.entrySet()) {
      Type paramType = paramEntry.getValue().getType();
      Property property = ModelConverters.getInstance().readAsProperty(paramType);
      if (RefProperty.class.isInstance(property)) {
        queryObjectSet.add(paramEntry.getKey());
      }
    }

    for (Entry<String, ParamWrapper<Parameter>> paramEntry : swaggerParamMap.entrySet()) {
      if (ParamUtils.isRealBodyParameter(paramEntry.getValue().getParam())) {
        queryObjectSet.remove(paramEntry.getKey());
      }
    }

    return queryObjectSet;
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
