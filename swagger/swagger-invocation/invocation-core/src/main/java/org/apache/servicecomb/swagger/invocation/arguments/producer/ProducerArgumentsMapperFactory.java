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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapperConfig;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.ContextArgumentMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.FieldInfo;
import org.apache.servicecomb.swagger.invocation.converter.Converter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ProducerArgumentsMapperFactory extends ArgumentsMapperFactory {
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
  protected <T> T createArgumentsMapper(ArgumentsMapperConfig config) {
    return (T) new ProducerArgumentsMapper(config.getArgumentMapperList(),
        config.getProviderMethod().getParameterCount());
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
}
