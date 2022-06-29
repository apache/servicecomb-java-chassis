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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestVertxClientResponseToHttpServletResponse {
  HttpClientResponse clientResponse;

  Buffer bodyBuffer = Buffer.buffer();

  VertxClientResponseToHttpServletResponse response;

  @BeforeEach
  public void setup() {
    clientResponse = Mockito.mock(HttpClientResponse.class);
    response = new VertxClientResponseToHttpServletResponse(clientResponse, bodyBuffer);
  }

  @AfterEach
  public void after() {
    Mockito.reset(clientResponse);
  }

  @Test
  public void getStatus() {
    Mockito.when(clientResponse.statusCode()).thenReturn(123);

    Assertions.assertEquals(123, response.getStatus());
  }

  @Test
  public void getStatusType() {
    Mockito.when(clientResponse.statusCode()).thenReturn(123);
    Mockito.when(clientResponse.statusMessage()).thenReturn("test");

    StatusType type = response.getStatusType();
    Assertions.assertSame(type, response.getStatusType());
    Assertions.assertEquals(123, type.getStatusCode());
    Assertions.assertEquals("test", type.getReasonPhrase());
  }

  @Test
  public void getContentType() {
    Mockito.when(clientResponse.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("json");

    Assertions.assertEquals("json", response.getContentType());
  }

  @Test
  public void getHeader() {
    Mockito.when(clientResponse.getHeader("name")).thenReturn("value");

    Assertions.assertEquals("value", response.getHeader("name"));
  }

  @Test
  public void getHeaders() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("name", "v1");
    headers.add("name", "v2");
    Mockito.when(clientResponse.headers()).thenReturn(headers);

    MatcherAssert.assertThat(response.getHeaders("name"), Matchers.contains("v1", "v2"));
  }

  @Test
  public void getHeaderNames() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("n1", "v1");
    headers.add("n2", "v2");
    Mockito.when(clientResponse.headers()).thenReturn(headers);

    MatcherAssert.assertThat(response.getHeaderNames(), Matchers.contains("n1", "n2"));
  }
}
