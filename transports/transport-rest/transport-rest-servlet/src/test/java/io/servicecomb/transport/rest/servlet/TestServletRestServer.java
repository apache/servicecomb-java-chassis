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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Holder;

import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import io.servicecomb.core.Invocation;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.response.Headers;
import io.vertx.core.buffer.Buffer;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestServletRestServer {
  ServletRestServer server = new ServletRestServer();

  HttpServletResponse httpServerResponse;

  @Mocked
  Invocation invocation;

  ProduceProcessor produceProcessor = ProduceProcessorManager.JSON_PROCESSOR;

  @Mocked
  Response response;

  @Test
  public void testDoSendResponseStatusAndContentType() throws Exception {
    new Expectations() {
      {
        response.getStatusCode();
        result = 123;
        response.getReasonPhrase();
        result = "reason";
        response.getHeaders();
        result = new Error("stop");
      }
    };

    Map<String, Object> result = new HashMap<>();
    httpServerResponse = new MockUp<HttpServletResponse>() {
      @Mock
      void setStatus(int sc, String sm) {
        result.put("statusCode", sc);
        result.put("reasonPhrase", sm);
      }

      @Mock
      void setContentType(String type) {
        result.put("contentType", type);
      }
    }.getMockInstance();

    Map<String, Object> expected = new HashMap<>();
    expected.put("statusCode", 123);
    expected.put("reasonPhrase", "reason");
    expected.put("contentType", "application/json");

    try {
      server.doSendResponse(invocation, httpServerResponse, produceProcessor, response);
      Assert.fail("must throw exception");
    } catch (Error e) {
      Assert.assertEquals(expected, result);
    }
  }

  @Test
  public void testDoSendResponseHeaderNull() throws Exception {
    Headers headers = new Headers();

    new Expectations() {
      {
        response.getResult();
        result = new Error("stop");
        response.getHeaders();
        result = headers;
      }
    };

    Headers resultHeaders = new Headers();
    httpServerResponse = new MockUp<HttpServletResponse>() {
      @Mock
      void addHeader(String name, String value) {
        resultHeaders.addHeader(name, value);
      }
    }.getMockInstance();

    try {
      server.doSendResponse(invocation, httpServerResponse, produceProcessor, response);
      Assert.fail("must throw exception");
    } catch (Error e) {
      Assert.assertEquals(null, resultHeaders.getHeaderMap());
    }
  }

  @Test
  public void testDoSendResponseHeaderNormal() throws Exception {
    Headers headers = new Headers();
    headers.addHeader("h1", "h1v1");
    headers.addHeader("h1", "h1v2");
    headers.addHeader("h2", "h2v");

    new Expectations() {
      {
        response.getResult();
        result = new Error("stop");
        response.getHeaders();
        result = headers;
      }
    };

    Headers resultHeaders = new Headers();
    httpServerResponse = new MockUp<HttpServletResponse>() {
      @Mock
      void addHeader(String name, String value) {
        resultHeaders.addHeader(name, value);
      }
    }.getMockInstance();

    try {
      server.doSendResponse(invocation, httpServerResponse, produceProcessor, response);
      Assert.fail("must throw exception");
    } catch (Error e) {
      Assert.assertEquals(headers.getHeaderMap(), resultHeaders.getHeaderMap());
    }
  }

  @Test
  public void testDoSendResponseResultOK() throws Exception {
    new Expectations() {
      {
        response.getResult();
        result = "ok";
      }
    };

    Buffer buffer = Buffer.buffer();
    ServletOutputStream output = new ServletOutputStream() {
      public void write(byte b[], int off, int len) throws IOException {
        buffer.appendBytes(b, off, len);
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {
      }

      @Override
      public void write(int b) throws IOException {
      }
    };

    httpServerResponse = new MockUp<HttpServletResponse>() {
      @Mock
      ServletOutputStream getOutputStream() throws IOException {
        return output;
      }
    }.getMockInstance();

    server.doSendResponse(invocation, httpServerResponse, produceProcessor, response);
    Assert.assertEquals("\"ok\"", buffer.toString());
  }

  @Test
  public void testCopyRequest(@Mocked HttpServletRequest request, @Mocked HttpServletResponse response) {
    Holder<HttpServletRequest> holder = new Holder<>();
    ServletRestServer servletRestServer = new ServletRestServer() {
      @Override
      protected void handleRequest(HttpServletRequest request, HttpServletResponse httpResponse) {
        holder.value = request;
      }
    };
    servletRestServer.service(request, response);
    Assert.assertSame(request, holder.value);

    Configuration cfg = (Configuration) DynamicPropertyFactory.getBackingConfigurationSource();;
    cfg.addProperty(RestConst.CONFIG_COPY_REQUEST, true);

    servletRestServer.service(request, response);
    Assert.assertEquals(CachedHttpServletRequest.class, holder.value.getClass());

    cfg.clearProperty(RestConst.CONFIG_COPY_REQUEST);
  }
}
