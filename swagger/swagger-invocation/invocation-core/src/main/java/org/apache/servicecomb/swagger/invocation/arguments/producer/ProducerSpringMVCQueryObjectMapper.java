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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.converter.Converter;
import org.apache.servicecomb.swagger.invocation.converter.impl.ConverterCommon;

/**
 * Argument mapper for object params.
 * Collect all query swagger params as json and deserialize to object param.
 */
public class ProducerSpringMVCQueryObjectMapper implements ArgumentMapper {
  private int producerIdx;

  private Map<String, Integer> swaggerParamIndexMap;

  private Converter converter;

  public ProducerSpringMVCQueryObjectMapper(Map<String, Integer> swaggerParamIndexMap, int producerIdx,
      Type producerParamType) {
    this.producerIdx = producerIdx;
    this.swaggerParamIndexMap = new HashMap<>();
    this.swaggerParamIndexMap.putAll(swaggerParamIndexMap);
    converter = new ConverterCommon(producerParamType);
  }

  @Override
  public void mapArgument(SwaggerInvocation invocation, Object[] producerArguments) {
    Map<String, Object> jsonMap = new HashMap<>(swaggerParamIndexMap.size());

    for (Entry<String, Integer> swaggerIndexEntry : swaggerParamIndexMap.entrySet()) {
      jsonMap.put(swaggerIndexEntry.getKey(), invocation.getSwaggerArgument(swaggerIndexEntry.getValue()));
    }

    final Object producerParam = converter.convert(jsonMap);
    producerArguments[producerIdx] = producerParam;
  }
}
