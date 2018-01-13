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

package org.apache.servicecomb.foundation.vertx.http;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.StatusType;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import mockit.Expectations;
import mockit.Mocked;

public class TestVertxClientResponseToHttpServletResponse {
  @Mocked
  HttpClientResponse clientResponse;

  Buffer bodyBuffer = Buffer.buffer();

  VertxClientResponseToHttpServletResponse response;

  @Before
  public void setup() {
    response = new VertxClientResponseToHttpServletResponse(clientResponse, bodyBuffer);
  }

  @Test
  public void getStatus() {
    new Expectations() {
      {
        clientResponse.statusCode();
        result = 123;
      }
    };

    Assert.assertEquals(123, response.getStatus());
  }

  @Test
  public void getStatusType() {
    new Expectations() {
      {
        clientResponse.statusCode();
        result = 123;
        clientResponse.statusMessage();
        result = "test";
      }
    };

    StatusType type = response.getStatusType();
    Assert.assertSame(type, response.getStatusType());
    Assert.assertEquals(123, type.getStatusCode());
    Assert.assertEquals("test", type.getReasonPhrase());
  }

  @Test
  public void getContentType() {
    new Expectations() {
      {
        clientResponse.getHeader(HttpHeaders.CONTENT_TYPE);
        result = "json";
      }
    };

    Assert.assertEquals("json", response.getContentType());
  }

  @Test
  public void getHeader() {
    new Expectations() {
      {
        clientResponse.getHeader("name");
        result = "value";
      }
    };

    Assert.assertEquals("value", response.getHeader("name"));
  }

  @Test
  public void getHeaders() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("name", "v1");
    headers.add("name", "v2");
    new Expectations() {
      {
        clientResponse.headers();
        result = headers;
      }
    };

    Assert.assertThat(response.getHeaders("name"), Matchers.contains("v1", "v2"));
  }

  @Test
  public void getHeaderNames() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("n1", "v1");
    headers.add("n2", "v2");
    new Expectations() {
      {
        clientResponse.headers();
        result = headers;
      }
    };

    Assert.assertThat(response.getHeaderNames(), Matchers.contains("n1", "n2"));
  }
}
