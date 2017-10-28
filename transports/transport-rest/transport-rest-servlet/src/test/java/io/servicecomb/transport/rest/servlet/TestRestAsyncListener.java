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

package io.servicecomb.transport.rest.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.StandardHttpServletRequestEx;
import io.servicecomb.swagger.invocation.exception.ExceptionFactory;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRestAsyncListener {
  HttpServletRequest request = new AbstractHttpServletRequest() {
    public String getMethod() {
      return "GET";
    };

    public String getRequestURI() {
      return "path";
    };
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
  public void onTimeoutExecuting() throws IOException {
    request.setAttribute(RestConst.REST_STATE_EXECUTING, true);

    listener.onTimeout(event);

    Assert.assertSame(requestEx, request.getAttribute(RestConst.REST_REQUEST));
  }

  @Test
  public void onTimeoutCommitted() throws IOException {
    committed = true;
    listener.onTimeout(event);

    Assert.assertNull(request.getAttribute(RestConst.REST_REQUEST));
    Assert.assertFalse(flushed);
  }

  @Test
  public void onTimeoutNotCommitted() throws IOException {
    committed = false;
    listener.onTimeout(event);

    Assert.assertNull(request.getAttribute(RestConst.REST_REQUEST));
    Assert.assertEquals(MediaType.APPLICATION_JSON, contentType);
    Assert.assertEquals(ExceptionFactory.PRODUCER_INNER_STATUS_CODE, statusCode);
    Assert.assertTrue(flushed);
    Assert.assertEquals("{\"message\":\"TimeOut in Processing\"}", writer.toString());
  }
}
