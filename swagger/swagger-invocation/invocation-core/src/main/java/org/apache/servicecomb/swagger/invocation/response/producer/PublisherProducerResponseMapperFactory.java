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

import org.apache.servicecomb.swagger.invocation.response.ResponseMapperFactorys;
import org.apache.servicecomb.swagger.invocation.sse.SseEventResponseEntity;
import org.reactivestreams.Publisher;

public class PublisherProducerResponseMapperFactory implements ProducerResponseMapperFactory {
  @Override
  public boolean isMatch(Type producerType) {
    if (!ParameterizedType.class.isAssignableFrom(producerType.getClass())) {
      return false;
    }

    return ((ParameterizedType) producerType).getRawType().equals(Publisher.class);
  }

  @Override
  public ProducerResponseMapper createResponseMapper(ResponseMapperFactorys<ProducerResponseMapper> factorys,
      Type providerType) {
    Type realProducerType = ((ParameterizedType) providerType).getActualTypeArguments()[0];
    return new PublisherProducerResponseMapper(
        !realProducerType.getTypeName().contains(SseEventResponseEntity.class.getName()));
  }
}
