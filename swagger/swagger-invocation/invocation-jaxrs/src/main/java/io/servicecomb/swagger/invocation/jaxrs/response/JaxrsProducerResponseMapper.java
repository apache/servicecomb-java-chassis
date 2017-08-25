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
package io.servicecomb.swagger.invocation.jaxrs.response;

import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.StatusType;

import org.springframework.stereotype.Component;

import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;

@Component
public class JaxrsProducerResponseMapper implements ProducerResponseMapper {
  @Override
  public Class<?> getResponseClass() {
    return javax.ws.rs.core.Response.class;
  }

  @Override
  public Response mapResponse(StatusType status, Object response) {
    javax.ws.rs.core.Response jaxrsResponse = (javax.ws.rs.core.Response) response;

    Response cseResponse = Response.status(jaxrsResponse.getStatusInfo()).entity(jaxrsResponse.getEntity());
    MultivaluedMap<String, Object> headers = jaxrsResponse.getHeaders();
    for (Entry<String, List<Object>> entry : headers.entrySet()) {
      if (entry.getValue() == null || entry.getValue().isEmpty()) {
        continue;
      }

      cseResponse.getHeaders().getHeaderMap().put(entry.getKey(), entry.getValue());
    }
    return cseResponse;
  }
}
