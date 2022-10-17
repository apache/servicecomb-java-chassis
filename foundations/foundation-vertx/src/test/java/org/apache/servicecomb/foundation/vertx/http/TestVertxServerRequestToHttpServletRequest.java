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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.ws.rs.core.HttpHeaders;

import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.impl.RoutingContextInternal;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.http.HttpUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public class TestVertxServerRequestToHttpServletRequest {
  @Mocked
  RoutingContext context;

  @Mocked
  HttpServerRequest vertxRequest;

  @Mocked
  SocketAddress socketAddress;

  RequestBody mockRequestBody;

  RoutingContextInternal mockContext;

  HttpServerRequest mockHttpServerRequest;

  SocketAddress mockSocketAddress;

  VertxServerRequestToHttpServletRequest request;

  @Before
  public void setup() {
    new Expectations() {
      {
        context.request();
        result = vertxRequest;
        vertxRequest.remoteAddress();
        result = socketAddress;
      }
    };

    mockRequestBody = Mockito.mock(RequestBody.class);
    mockContext = Mockito.mock(RoutingContextInternal.class);
    mockHttpServerRequest = Mockito.mock(HttpServerRequest.class);
    mockSocketAddress = Mockito.mock(SocketAddress.class);
    // init mocks
    Mockito.when(mockHttpServerRequest.remoteAddress()).thenReturn(mockSocketAddress);
    Mockito.when(mockContext.body()).thenReturn(mockRequestBody);
    Mockito.when(mockContext.request()).thenReturn(mockHttpServerRequest);

    request = new VertxServerRequestToHttpServletRequest(context);
  }

  @After
  public void clean() {
    Mockito.reset(mockRequestBody);
    Mockito.reset(mockContext);
  }

  @Test
  public void constructWithPath() {
    request = new VertxServerRequestToHttpServletRequest(context, "/path");

    Assertions.assertEquals("/path", request.getRequestURI());
  }

  @Test
  public void setBodyBuffer() {
    Holder<Buffer> bodyHolder = new Holder<>();
    Mockito.doAnswer(invocation -> {
      bodyHolder.value = invocation.getArgument(0);
      return null;
    }).when(mockContext).setBody(Mockito.any());
    request = new VertxServerRequestToHttpServletRequest(mockContext);

    Buffer bodyBuffer = Buffer.buffer();
    request.setBodyBuffer(bodyBuffer);

    Assertions.assertSame(bodyBuffer, bodyHolder.value);
    Assertions.assertSame(bodyBuffer, request.getBodyBuffer());
  }

  @Test
  public void testGetContentType() {
    VertxServerRequestToHttpServletRequest request = new VertxServerRequestToHttpServletRequest(mockContext);
    Mockito.when(mockHttpServerRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("json");
    Assertions.assertEquals("json", request.getContentType());
  }

  @Test
  public void testGetCookies() {
    Set<io.vertx.core.http.Cookie> vertxCookies = new HashSet<>();
    vertxCookies.add(io.vertx.core.http.Cookie.cookie("c1", "c1v"));
    vertxCookies.add(io.vertx.core.http.Cookie.cookie("c2", "c2v"));
    new Expectations() {
      {
        context.request().cookies();
        result = vertxCookies;
      }
    };

    Cookie[] cookies = request.getCookies();
    // we can't ensure the sequence when set to list
    if (cookies[0].getName().equals("c1")) {
      Assertions.assertEquals("c1", cookies[0].getName());
      Assertions.assertEquals("c1v", cookies[0].getValue());
      Assertions.assertEquals("c2", cookies[1].getName());
      Assertions.assertEquals("c2v", cookies[1].getValue());
    } else {
      Assertions.assertEquals("c2", cookies[0].getName());
      Assertions.assertEquals("c2v", cookies[0].getValue());
      Assertions.assertEquals("c1", cookies[1].getName());
      Assertions.assertEquals("c1v", cookies[1].getValue());
    }
  }

  @Test
  public void testGetParameter() {
    new Expectations() {
      {
        vertxRequest.getParam("name");
        result = "value";
      }
    };

    Assertions.assertEquals("value", request.getParameter("name"));
  }

  @Test
  public void testGetParameterValuesNull() {
    Assertions.assertEquals(0, request.getParameterValues("name").length);
  }

  @Test
  public void testGetParameterValuesNormal() {
    MultiMap params = MultiMap.caseInsensitiveMultiMap();
    params.add("name", "value");

    new Expectations() {
      {
        vertxRequest.params();
        result = params;
      }
    };

    MatcherAssert.assertThat(request.getParameterValues("name"), Matchers.arrayContaining("value"));
  }

  @Test
  public void testGetParameterMap() {
    MultiMap params = MultiMap.caseInsensitiveMultiMap();
    params.add("name", "value");

    new Expectations() {
      {
        vertxRequest.params();
        result = params;
      }
    };

    Map<String, String[]> result = request.getParameterMap();
    MatcherAssert.assertThat(result.keySet(), Matchers.contains("name"));
    MatcherAssert.assertThat(result.get("name"), Matchers.arrayContaining("value"));
    Assertions.assertSame(result, request.getParameterMap());
  }

  @Test
  public void testScheme() {
    new Expectations() {
      {
        vertxRequest.scheme();
        result = "abc";
      }
    };

    Assertions.assertEquals("abc", request.getScheme());
  }

  @Test
  public void testGetRemoteAddr() {
    new Expectations() {
      {
        socketAddress.host();
        result = "host";
      }
    };

    Assertions.assertEquals("host", request.getRemoteAddr());
  }

  @Test
  public void testGetRemoteAddrNull() {
    new Expectations() {
      {
        socketAddress.host();
        result = null;
      }
    };
    Assertions.assertNull(request.getRemoteAddr());
  }

  @Test
  public void testGetRemoteHost() {
    new Expectations() {
      {
        socketAddress.host();
        result = "host";
      }
    };

    Assertions.assertEquals("host", request.getRemoteHost());
  }

  @Test
  public void testGetRemotePort() {
    new Expectations() {
      {
        socketAddress.port();
        result = 1234;
      }
    };

    Assertions.assertEquals(1234, request.getRemotePort());
  }

  @Test
  public void testGetgetLocalAddr(@Mocked SocketAddress sa) {
    new Expectations() {
      {
        sa.host();
        result = "host";
        vertxRequest.localAddress();
        result = sa;
      }
    };

    Assertions.assertEquals("host", request.getLocalAddr());
  }

  @Test
  public void testGetLocalPort(@Mocked SocketAddress sa) {
    new Expectations() {
      {
        sa.port();
        result = 1234;
        vertxRequest.localAddress();
        result = sa;
      }
    };

    Assertions.assertEquals(1234, request.getLocalPort());
  }

  @Test
  public void testGetHeader() {
    new Expectations() {
      {
        vertxRequest.getHeader("key");
        result = "value";
      }
    };

    Assertions.assertEquals("value", request.getHeader("key"));
  }

  @Test
  public void testGetHeaders() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("name", "value");
    new Expectations() {
      {
        vertxRequest.headers();
        result = headers;
      }
    };

    MatcherAssert.assertThat(Collections.list(request.getHeaders("name")), Matchers.contains("value"));
  }

  @Test
  public void testGetHeaderNames() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("name", "value");
    new Expectations() {
      {
        vertxRequest.headers();
        result = headers;
      }
    };

    MatcherAssert.assertThat(Collections.list(request.getHeaderNames()), Matchers.contains("name"));
  }

  @Test
  public void testGetIntHeaderNotExist() {
    Assertions.assertEquals(-1, request.getIntHeader("key"));
  }

  @Test
  public void testGetIntHeaderNotNumber() {
    new Expectations() {
      {
        vertxRequest.getHeader("key");
        result = "value";
      }
    };

    try {
      request.getIntHeader("key");
      Assertions.fail("must throw exception");
    } catch (NumberFormatException e) {
      Assertions.assertEquals("For input string: \"value\"", e.getMessage());
    }
  }

  @Test
  public void testGetIntHeaderNormal() {
    new Expectations() {
      {
        vertxRequest.getHeader("key");
        result = "1";
      }
    };

    Assertions.assertEquals(1, request.getIntHeader("key"));
  }

  @Test
  public void testGetMethod() {
    new Expectations() {
      {
        vertxRequest.method();
        result = HttpMethod.GET;
      }
    };

    Assertions.assertEquals("GET", request.getMethod());
  }

  @Test
  public void testGetPathInfo() {
    new Expectations() {
      {
        vertxRequest.path();
        result = "/path";
      }
    };

    Assertions.assertEquals("/path", request.getPathInfo());
  }

  @Test
  public void testGetQueryString() {
    new Expectations() {
      {
        vertxRequest.query();
        result = "k1=v1&k2=v2";
      }
    };

    Assertions.assertEquals("k1=v1&k2=v2", request.getQueryString());
  }

  @Test
  public void testGetRequestURI() {
    new Expectations() {
      {
        vertxRequest.path();
        result = "/path";
      }
    };

    Assertions.assertEquals("/path", request.getRequestURI());
  }

  @Test
  public void testGetServletPath() {
    new Expectations() {
      {
        vertxRequest.path();
        result = "/path";
      }
    };

    Assertions.assertEquals("/path", request.getServletPath());
  }

  @Test
  public void testGetContextPath() {
    Assertions.assertEquals("", request.getContextPath());
  }

  @Test
  public void testGetInputStream() throws IOException {
    Buffer body = Buffer.buffer();
    body.appendByte((byte) 1);

    Mockito.when(mockRequestBody.buffer()).thenReturn(body);
    Mockito.when(mockContext.request()).thenReturn(vertxRequest);
    Mockito.when(mockContext.body()).thenReturn(mockRequestBody);

    VertxServerRequestToHttpServletRequest request = new VertxServerRequestToHttpServletRequest(mockContext);
    ServletInputStream is1 = request.getInputStream();
    Assertions.assertSame(is1, request.getInputStream());
    int value = is1.read();
    is1.close();
    Assertions.assertEquals(1, value);
    Assertions.assertSame(is1, request.getInputStream());

    request.setBodyBuffer(Buffer.buffer().appendByte((byte) 2));
    ServletInputStream is2 = request.getInputStream();
    Assertions.assertNotSame(is1, is2);
  }

  @Test
  public void testGetAsyncContext() {
    AsyncContext asyncContext =
        Deencapsulation.getField(VertxServerRequestToHttpServletRequest.class, "EMPTY_ASYNC_CONTEXT");

    Assertions.assertSame(asyncContext, request.getAsyncContext());
  }

  @Test
  public void getCharacterEncoding() {
    new Expectations(HttpUtils.class) {
      {
        vertxRequest.getHeader(HttpHeaders.CONTENT_TYPE);
        result = "ct";
        HttpUtils.getCharsetFromContentType("ct");
        result = "ce";
      }
    };

    Assertions.assertEquals("ce", request.getCharacterEncoding());
  }


  @Test
  public void setParameter() {
    Map<String, String[]> parameterMap = new HashMap<>();
    Deencapsulation.setField(request, "parameterMap", parameterMap);

    request.setParameter("k1", "v1");
    request.setParameter("k2", "v2");

    Assertions.assertEquals("v1", request.getParameter("k1"));
    Assertions.assertEquals("v2", request.getParameter("k2"));

    Assertions.assertSame(parameterMap, request.getParameterMap());

    MatcherAssert.assertThat(Collections.list(request.getParameterNames()), Matchers.containsInAnyOrder("k1", "k2"));
  }
}
