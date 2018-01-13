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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.servicecomb.swagger.invocation.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestJaxrsConsumerResponseMapper {
  JaxrsConsumerResponseMapper mapper = new JaxrsConsumerResponseMapper();

  int status;

  Object entity;

  Map<String, Object> headers = new LinkedHashMap<>();

  ResponseBuilder responseBuilder;

  @Mocked
  RuntimeDelegate runtimeDelegate;

  @Before
  public void setup() {
    responseBuilder = new MockUp<ResponseBuilder>() {
      @Mock
      ResponseBuilder status(int status) {
        TestJaxrsConsumerResponseMapper.this.status = status;
        return responseBuilder;
      }

      @Mock
      ResponseBuilder entity(Object entity) {
        TestJaxrsConsumerResponseMapper.this.entity = entity;
        return responseBuilder;
      }

      @Mock
      ResponseBuilder header(String name, Object value) {
        headers.put(name, value);
        return responseBuilder;
      }
    }.getMockInstance();

    new Expectations() {
      {
        runtimeDelegate.createResponseBuilder();
        result = responseBuilder;
      }
    };
  }

  @SuppressWarnings("unchecked")
  @Test
  public void mapResponse_withHeaders() {
    Response response = Response.create(Status.OK, "ret");
    response.getHeaders().addHeader("h", "v");
    mapper.mapResponse(response);

    Assert.assertEquals(Status.OK.getStatusCode(), status);
    Assert.assertEquals("ret", entity);
    Assert.assertEquals(1, headers.size());
    Assert.assertThat((List<Object>) headers.get("h"), Matchers.contains("v"));
  }

  @Test
  public void mapResponse_withoutHeaders() {
    Response response = Response.create(Status.OK, "ret");
    mapper.mapResponse(response);

    Assert.assertEquals(Status.OK.getStatusCode(), status);
    Assert.assertEquals("ret", entity);
    Assert.assertEquals(0, headers.size());
  }
}
