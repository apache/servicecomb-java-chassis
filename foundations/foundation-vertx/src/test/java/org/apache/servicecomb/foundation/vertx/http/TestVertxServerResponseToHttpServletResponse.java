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

import java.io.IOException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.foundation.common.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestVertxServerResponseToHttpServletResponse {
  MultiMap headers = MultiMap.caseInsensitiveMultiMap();

  HttpStatus httpStatus = new HttpStatus(123, "default");

  HttpServerResponse serverResponse;

  VertxServerResponseToHttpServletResponse response;

  boolean flushWithBody;

  @Before
  public void setup() {
    serverResponse = new MockUp<HttpServerResponse>() {
      @Mock
      HttpServerResponse setStatusCode(int statusCode) {
        Deencapsulation.setField(httpStatus, "statusCode", statusCode);
        return serverResponse;
      }

      @Mock
      HttpServerResponse setStatusMessage(String statusMessage) {
        Deencapsulation.setField(httpStatus, "reason", statusMessage);
        return serverResponse;
      }

      @Mock
      int getStatusCode() {
        return httpStatus.getStatusCode();
      }

      @Mock
      String getStatusMessage() {
        return httpStatus.getReasonPhrase();
      }

      @Mock
      MultiMap headers() {
        return headers;
      }

      @Mock
      void end() {
        flushWithBody = false;
      }

      @Mock
      void end(Buffer chunk) {
        flushWithBody = true;
      }
    }.getMockInstance();

    response = new VertxServerResponseToHttpServletResponse(serverResponse);
  }

  @Test
  public void setContentType() {
    response.setContentType("json");
    Assert.assertEquals("json", headers.get(HttpHeaders.CONTENT_TYPE));
  }

  @Test
  public void setStatus() {
    response.setStatus(222, "test");
    Assert.assertEquals(222, httpStatus.getStatusCode());
    Assert.assertEquals("test", httpStatus.getReasonPhrase());
  }

  @Test
  public void getStatusType() {
    StatusType status = response.getStatusType();

    Assert.assertSame(status, response.getStatusType());
    Assert.assertEquals(123, httpStatus.getStatusCode());
    Assert.assertEquals("default", httpStatus.getReasonPhrase());
  }

  @Test
  public void addHeader() {
    response.addHeader("n1", "v1_1");
    response.addHeader("n1", "v1_2");
    response.addHeader("n2", "v2");

    Assert.assertEquals(2, headers.size());
    Assert.assertThat(headers.getAll("n1"), Matchers.contains("v1_1", "v1_2"));
    Assert.assertThat(headers.getAll("n2"), Matchers.contains("v2"));
  }

  @Test
  public void setHeader() {
    response.setHeader("n1", "v1_1");
    response.setHeader("n1", "v1_2");
    response.setHeader("n2", "v2");

    Assert.assertEquals(2, headers.size());
    Assert.assertThat(headers.getAll("n1"), Matchers.contains("v1_2"));
    Assert.assertThat(headers.getAll("n2"), Matchers.contains("v2"));
  }

  @Test
  public void getStatus() {
    Assert.assertEquals(123, response.getStatus());
  }

  @Test
  public void getContentType() {
    headers.set(HttpHeaders.CONTENT_TYPE, "json");
    Assert.assertEquals("json", response.getContentType());
  }

  @Test
  public void getHeader() {
    headers.set(HttpHeaders.CONTENT_TYPE, "json");
    Assert.assertEquals("json", response.getHeader(HttpHeaders.CONTENT_TYPE));
  }

  @Test
  public void getHeaders() {
    headers.add("h1", "h1_1");
    headers.add("h1", "h1_2");

    Assert.assertThat(response.getHeaders("h1"), Matchers.contains("h1_1", "h1_2"));
  }

  @Test
  public void getHeaderNames() {
    headers.add("h1", "h1");
    headers.add("h2", "h2");

    Assert.assertThat(response.getHeaderNames(), Matchers.contains("h1", "h2"));
  }

  @Test
  public void flushBufferNoBody() throws IOException {
    response.flushBuffer();

    Assert.assertFalse(flushWithBody);
  }

  @Test
  public void flushBufferWithBody() throws IOException {
    response.setBodyBuffer(Buffer.buffer());
    response.flushBuffer();

    Assert.assertTrue(flushWithBody);
  }
}
