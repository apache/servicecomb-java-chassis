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

package org.apache.servicecomb.transport.rest.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.StandardHttpServletRequestEx;
import org.junit.Before;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestRestAsyncListener {
  HttpServletRequest request = new AbstractHttpServletRequest() {
    public String getMethod() {
      return "GET";
    }

    public String getRequestURI() {
      return "path";
    }
  };

  HttpServletRequestEx requestEx = new StandardHttpServletRequestEx(request);

  boolean committed;

  boolean flushed;

  int statusCode;

  String contentType;

  Writer writer = new StringWriter();

  PrintWriter printWriter = new PrintWriter(writer);

  @Mocked
  HttpServletResponse response;

  @Mocked
  AsyncContext context;

  AsyncEvent event;

  RestAsyncListener listener = new RestAsyncListener();


  @Before
  public void setup() {
    event = new AsyncEvent(context, requestEx, response);
    requestEx.setAttribute(RestConst.REST_REQUEST, requestEx);

    new MockUp<HttpServletResponse>(response) {
      @Mock
      void setContentType(String type) {
        contentType = type;
      }

      @Mock
      void setStatus(int sc) {
        statusCode = sc;
      }

      @Mock
      boolean isCommitted() {
        return committed;
      }

      @Mock
      PrintWriter getWriter() throws IOException {
        return printWriter;
      }

      @Mock
      void flushBuffer() throws IOException {
        flushed = true;
      }
    };
  }

  @Test
  public void onTimeoutCommitted() throws IOException {
    committed = true;
    listener.onTimeout(event);

    Assertions.assertNull(request.getAttribute(RestConst.REST_REQUEST));
    Assertions.assertFalse(flushed);
  }

  @Test
  public void onTimeoutNotCommitted() throws IOException {
    committed = false;
    listener.onTimeout(event);

    Assertions.assertNull(request.getAttribute(RestConst.REST_REQUEST));
    Assertions.assertEquals(MediaType.APPLICATION_JSON, contentType);
    Assertions.assertEquals(500, statusCode);
    Assertions.assertTrue(flushed);
    Assertions.assertEquals("{\"message\":\"Timeout when processing the request.\"}", writer.toString());
  }
}
