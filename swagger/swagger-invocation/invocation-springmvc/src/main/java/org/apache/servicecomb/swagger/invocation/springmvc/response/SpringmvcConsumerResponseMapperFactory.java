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
package org.apache.servicecomb.swagger.invocation.springmvc.response;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.invocation.response.ResponseMapperFactorys;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapper;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapperFactory;
import org.springframework.http.ResponseEntity;

public class SpringmvcConsumerResponseMapperFactory implements ConsumerResponseMapperFactory {
  @Override
  public boolean isMatch(Type consumerType) {
    if (!ParameterizedType.class.isAssignableFrom(consumerType.getClass())) {
      return false;
    }

    return ((ParameterizedType) consumerType).getRawType().equals(ResponseEntity.class);
  }

  @Override
  public ConsumerResponseMapper createResponseMapper(ResponseMapperFactorys<ConsumerResponseMapper> factorys,
      Type consumerType) {
    Type realConsumerType = ((ParameterizedType) consumerType).getActualTypeArguments()[0];
    ConsumerResponseMapper realMapper = factorys.createResponseMapper(realConsumerType);
    return new SpringmvcConsumerResponseMapper(realMapper);
  }
}
