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
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestJaxrsProducerResponseMapper {
  JaxrsProducerResponseMapper mapper = new JaxrsProducerResponseMapper();

  @Mocked
  javax.ws.rs.core.Response jaxrsResponse;

  @Test
  public void mapResponse_withoutHeaders() {
    new Expectations() {
      {
        jaxrsResponse.getStatusInfo();
        result = Status.OK;
        jaxrsResponse.getEntity();
        result = "result";
      }
    };
    Response response = mapper.mapResponse(null, jaxrsResponse);
    Assert.assertEquals(Status.OK, response.getStatus());
    Assert.assertEquals("result", response.getResult());
    Assert.assertNull(response.getHeaders().getHeaderMap());
  }

  @Test
  public void mapResponse_withHeaders() {
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add("h", "v");

    new Expectations() {
      {
        jaxrsResponse.getStatusInfo();
        result = Status.OK;
        jaxrsResponse.getEntity();
        result = "result";
        jaxrsResponse.getHeaders();
        result = headers;
      }
    };
    Response response = mapper.mapResponse(null, jaxrsResponse);
    Assert.assertEquals(Status.OK, response.getStatus());
    Assert.assertEquals("result", response.getResult());
    Assert.assertEquals(1, response.getHeaders().getHeaderMap().size());
    Assert.assertThat(response.getHeaders().getHeader("h"), Matchers.contains("v"));
  }
}
