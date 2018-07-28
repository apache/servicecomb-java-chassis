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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapperConfig;
import org.apache.servicecomb.swagger.invocation.arguments.ProviderParameter;
import org.springframework.stereotype.Component;

import io.swagger.converter.ModelConverters;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

@Component
public class SpringMVCProducerArgumentsMapperFactory extends ProducerArgumentsMapperFactory {

  @Override
  public boolean canProcess(ArgumentsMapperConfig config) {
    return config.getSwaggerGeneratorContext().getClass().getCanonicalName().equals(
        "org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGeneratorContext");
  }

  /**
   * Find all query object params
   * @return the names of the query object params
   */
  @Override
  protected Set<String> findAggregatedParamNames(Map<String, ProviderParameter> providerParamMap,
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

  protected void generateAggregatedParamMapper(ArgumentsMapperConfig config,
      Map<String, ProviderParameter> providerParamMap, Map<String, ParamWrapper<Parameter>> swaggerParamMap,
      Set<String> aggregatedParamNames) {
    // collect all query params
    Map<String, Integer> querySwaggerParamsIndex = new HashMap<>();
    for (Entry<String, ParamWrapper<Parameter>> wrapperEntry : swaggerParamMap.entrySet()) {
      if (wrapperEntry.getValue().getParam() instanceof QueryParameter) {
        querySwaggerParamsIndex.put(wrapperEntry.getKey(), wrapperEntry.getValue().getIndex());
      }
    }
    // create mapper for each query objects
    for (String queryObjectName : aggregatedParamNames) {
      final ProviderParameter providerParameter = providerParamMap.get(queryObjectName);
      ArgumentMapper mapper = new ProducerSpringMVCQueryObjectMapper(querySwaggerParamsIndex,
          providerParameter.getIndex(),
          providerParameter.getType());
      config.addArgumentMapper(mapper);
    }
  }
}
