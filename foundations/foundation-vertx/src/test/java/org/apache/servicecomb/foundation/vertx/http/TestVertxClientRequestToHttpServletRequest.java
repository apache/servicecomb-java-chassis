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

import java.util.Collections;

import javax.ws.rs.core.HttpHeaders;

import org.apache.servicecomb.foundation.common.http.HttpUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import mockit.Expectations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestVertxClientRequestToHttpServletRequest {
  HttpClientRequest clientRequest;

  Buffer bodyBuffer = Buffer.buffer();

  VertxClientRequestToHttpServletRequest request;

  @BeforeEach
  public void setup() {
    clientRequest = Mockito.mock(HttpClientRequest.class);
    request = new VertxClientRequestToHttpServletRequest(clientRequest, bodyBuffer);
  }

  @AfterEach
  public void after() {
    Mockito.reset(clientRequest);
  }

  @Test
  public void testGetRequestURI() {
    Mockito.when(clientRequest.path()).thenReturn("/path");

    Assertions.assertEquals("/path", request.getRequestURI());
  }

  @Test
  public void testGetQueryString() {
    Mockito.when(clientRequest.query()).thenReturn("a=1&b=2");

    Assertions.assertEquals("a=1&b=2", request.getQueryString());
  }

  @Test
  public void testGetHeader() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("name", "value");
    Mockito.when(clientRequest.headers()).thenReturn(headers);

    Assertions.assertEquals("value", request.getHeader("name"));
  }

  @Test
  public void testGetHeaders() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("name", "value");
    Mockito.when(clientRequest.headers()).thenReturn(headers);

    MatcherAssert.assertThat(Collections.list(request.getHeaders("name")), Matchers.contains("value"));
  }

  @Test
  public void testGetHeaderNames() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("name", "value");
    Mockito.when(clientRequest.headers()).thenReturn(headers);

    MatcherAssert.assertThat(Collections.list(request.getHeaderNames()), Matchers.contains("name"));
  }

  @Test
  public void testSetHeader() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    Mockito.when(clientRequest.headers()).thenReturn(headers);

    request.setHeader("name", "v1");
    request.setHeader("name", "v2");
    MatcherAssert.assertThat(headers.getAll("name"), Matchers.contains("v2"));
  }

  @Test
  public void testAddHeader() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    Mockito.when(clientRequest.headers()).thenReturn(headers);

    request.addHeader("name", "v1");
    request.addHeader("name", "v2");
    MatcherAssert.assertThat(headers.getAll("name"), Matchers.contains("v1", "v2"));
  }

  @Test
  public void testGetContextPath() {
    Assertions.assertEquals("", request.getContextPath());
  }

  @Test
  public void getMethod() {
    Mockito.when(clientRequest.getMethod()).thenReturn(HttpMethod.GET);

    Assertions.assertEquals("GET", request.getMethod());
  }

  @Test
  public void getContentType() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    Mockito.when(clientRequest.headers()).thenReturn(headers);

    request.addHeader(HttpHeaders.CONTENT_TYPE, "ct");

    Assertions.assertEquals("ct", request.getContentType());
  }

  @Test
  public void getCharacterEncoding() {
    String contentType = "ct";
    String characterEncoding = "ce";

    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    new Expectations(HttpUtils.class) {
      {
        HttpUtils.getCharsetFromContentType(contentType);
        result = characterEncoding;
      }
    };
    Mockito.when(clientRequest.headers()).thenReturn(headers);

    request.addHeader(HttpHeaders.CONTENT_TYPE, contentType);

    Assertions.assertEquals("ce", request.getCharacterEncoding());
  }
}
