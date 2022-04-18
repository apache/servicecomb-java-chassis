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

import java.util.Map.Entry;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.vertx.core.MultiMap;

public class SpringmvcConsumerResponseMapper implements ConsumerResponseMapper {
  private final ConsumerResponseMapper realMapper;

  public SpringmvcConsumerResponseMapper(ConsumerResponseMapper realMapper) {
    this.realMapper = realMapper;
  }

  @Override
  public Object mapResponse(Response response) {
    HttpStatus status = HttpStatus.valueOf(response.getStatusCode());

    MultiMap headers = response.getHeaders();
    if (headers == null) {
      Object realResult = realMapper.mapResponse(response);
      return new ResponseEntity<>(realResult, null, status);
    }

    HttpHeaders httpHeaders = new HttpHeaders();
    for (Entry<String, String> entry : headers.entries()) {
      httpHeaders.add(entry.getKey(), entry.getValue());
    }

    Object realResult = realMapper.mapResponse(response);
    return new ResponseEntity<>(realResult, httpHeaders, status);
  }
}
