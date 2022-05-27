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
package org.apache.servicecomb.swagger.invocation.jaxrs.response;

import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;

public class JaxrsProducerResponseMapper implements ProducerResponseMapper {
  @Override
  public Response mapResponse(StatusType status, Object response) {
    javax.ws.rs.core.Response jaxrsResponse = (javax.ws.rs.core.Response) response;

    Response cseResponse = Response.status(jaxrsResponse.getStatusInfo()).entity(jaxrsResponse.getEntity());
    MultivaluedMap<String, Object> headers = jaxrsResponse.getHeaders();
    for (Entry<String, List<Object>> entry : headers.entrySet()) {
      if (entry.getValue() == null) {
        continue;
      }

      for (Object value : entry.getValue()) {
        if (value == null) {
          continue;
        }
        cseResponse.addHeader(entry.getKey(), value.toString());
      }
    }
    return cseResponse;
  }
}
