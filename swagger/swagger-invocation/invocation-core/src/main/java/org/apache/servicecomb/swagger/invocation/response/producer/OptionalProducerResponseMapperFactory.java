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
package org.apache.servicecomb.swagger.invocation.response.producer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import org.apache.servicecomb.swagger.invocation.response.ResponseMapperFactories;

public class OptionalProducerResponseMapperFactory implements ProducerResponseMapperFactory {
  @Override
  public boolean isMatch(Type producerType) {
    if (!ParameterizedType.class.isAssignableFrom(producerType.getClass())) {
      return false;
    }

    return ((ParameterizedType) producerType).getRawType().equals(Optional.class);
  }

  @Override
  public ProducerResponseMapper createResponseMapper(ResponseMapperFactories<ProducerResponseMapper> factorys,
      Type producerType) {
    Type realProducerType = ((ParameterizedType) producerType).getActualTypeArguments()[0];
    ProducerResponseMapper realMapper = factorys.createResponseMapper(realProducerType);
    return new OptionalProducerResponseMapper(realMapper);
  }
}
