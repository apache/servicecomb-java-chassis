/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.swagger.invocation.springmvc.response;

import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.StatusType;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.context.HttpStatus;
import io.servicecomb.swagger.invocation.response.Headers;
import io.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;

@Component
public class SpringmvcProducerResponseMapper implements ProducerResponseMapper {
  @Override
  public Class<?> getResponseClass() {
    return ResponseEntity.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Response mapResponse(StatusType status, Object response) {
    ResponseEntity<Object> springmvcResponse = (ResponseEntity<Object>) response;

    StatusType responseStatus = new HttpStatus(springmvcResponse.getStatusCode().value(),
        springmvcResponse.getStatusCode().getReasonPhrase());
    Response cseResponse = Response.status(responseStatus).entity(springmvcResponse.getBody());
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
