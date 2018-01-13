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

import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.HttpStatus;
import org.apache.servicecomb.swagger.invocation.response.Headers;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class SpringmvcProducerResponseMapper implements ProducerResponseMapper {
  private ProducerResponseMapper realMapper;

  public SpringmvcProducerResponseMapper(ProducerResponseMapper realMapper) {
    this.realMapper = realMapper;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Response mapResponse(StatusType status, Object response) {
    ResponseEntity<Object> springmvcResponse = (ResponseEntity<Object>) response;

    StatusType responseStatus = new HttpStatus(springmvcResponse.getStatusCode().value(),
        springmvcResponse.getStatusCode().getReasonPhrase());

    Response cseResponse = null;
    if (HttpStatus.isSuccess(responseStatus)) {
      cseResponse = realMapper.mapResponse(responseStatus, springmvcResponse.getBody());
    } else {
      // not support fail response mapper now
      cseResponse = Response.status(responseStatus).entity(springmvcResponse.getBody());
    }

    HttpHeaders headers = springmvcResponse.getHeaders();
    Headers cseHeaders = cseResponse.getHeaders();
    for (Entry<String, List<String>> entry : headers.entrySet()) {
      if (entry.getValue() == null || entry.getValue().isEmpty()) {
        continue;
      }

      for (String value : entry.getValue()) {
        cseHeaders.addHeader(entry.getKey(), value);
      }
    }
    return cseResponse;
  }
}
