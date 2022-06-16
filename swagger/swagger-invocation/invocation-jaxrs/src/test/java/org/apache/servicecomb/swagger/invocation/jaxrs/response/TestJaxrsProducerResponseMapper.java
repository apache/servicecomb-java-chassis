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

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.swagger.invocation.Response;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestJaxrsProducerResponseMapper {
  JaxrsProducerResponseMapper mapper = new JaxrsProducerResponseMapper();

  @Test
  public void mapResponse_withoutHeaders() {
    javax.ws.rs.core.Response jaxrsResponse = Mockito.mock(javax.ws.rs.core.Response.class);
    Mockito.when(jaxrsResponse.getStatusInfo()).thenReturn(Status.OK);
    Mockito.when(jaxrsResponse.getEntity()).thenReturn("result");
    Mockito.when(jaxrsResponse.getHeaders()).thenReturn(new MultivaluedHashMap<>());

    Response response = mapper.mapResponse(null, jaxrsResponse);
    Assertions.assertEquals(Status.OK, response.getStatus());
    Assertions.assertEquals("result", response.getResult());
    Assertions.assertNull(response.getHeaders());
  }

  @Test
  public void mapResponse_withHeaders() {
    javax.ws.rs.core.Response jaxrsResponse = Mockito.mock(javax.ws.rs.core.Response.class);
    Mockito.when(jaxrsResponse.getStatusInfo()).thenReturn(Status.OK);
    Mockito.when(jaxrsResponse.getEntity()).thenReturn("result");

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add("h", "v");
    Mockito.when(jaxrsResponse.getHeaders()).thenReturn(headers);

    Response response = mapper.mapResponse(null, jaxrsResponse);
    Assertions.assertEquals(Status.OK, response.getStatus());
    Assertions.assertEquals("result", response.getResult());
    Assertions.assertEquals(1, response.getHeaders().size());
    MatcherAssert.assertThat(response.getHeaders("h"), Matchers.contains("v"));
  }
}
