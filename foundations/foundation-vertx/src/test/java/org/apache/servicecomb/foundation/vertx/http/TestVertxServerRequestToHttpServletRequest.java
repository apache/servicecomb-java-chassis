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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.xml.ws.Holder;

import org.hamcrest.Matchers;
import org.junit.Assert;
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
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestVertxServerRequestToHttpServletRequest {
  @Mocked
  RoutingContext context;

  @Mocked
  HttpServerRequest vertxRequest;

  @Mocked
  SocketAddress socketAddress;

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

    request = new VertxServerRequestToHttpServletRequest(context);
  }

  @Test
  public void constructWithPath() {
    request = new VertxServerRequestToHttpServletRequest(context, "/path");

    Assert.assertEquals("/path", request.getRequestURI());
  }

  @Test
  public void setBodyBuffer() {
    Holder<Buffer> bodyHolder = new Holder<>();
    context = new MockUp<RoutingContext>() {
      @Mock
      HttpServerRequest request() {
        return vertxRequest;
      }

      @Mock
      void setBody(Buffer body) {
        bodyHolder.value = body;
      }
    }.getMockInstance();
    request = new VertxServerRequestToHttpServletRequest(context);

    Buffer bodyBuffer = Buffer.buffer();
    request.setBodyBuffer(bodyBuffer);

    Assert.assertSame(bodyBuffer, bodyHolder.value);
    Assert.assertSame(bodyBuffer, request.getBodyBuffer());
  }

  @Test
  public void testGetContentType() {
    new Expectations() {
      {
        vertxRequest.getHeader(HttpHeaders.CONTENT_TYPE);
        result = "json";
      }
    };

    Assert.assertEquals("json", request.getContentType());
  }

  @Test
  public void testGetCookies() {
    Set<io.vertx.ext.web.Cookie> vertxCookies = new LinkedHashSet<>();
    vertxCookies.add(io.vertx.ext.web.Cookie.cookie("c1", "c1v"));
    vertxCookies.add(io.vertx.ext.web.Cookie.cookie("c2", "c2v"));
    new Expectations() {
      {
        context.cookies();
        result = vertxCookies;
      }
    };

    Cookie[] cookies = request.getCookies();
    Assert.assertEquals("c1", cookies[0].getName());
    Assert.assertEquals("c1v", cookies[0].getValue());
    Assert.assertEquals("c2", cookies[1].getName());
    Assert.assertEquals("c2v", cookies[1].getValue());

    Assert.assertSame(cookies, request.getCookies());
  }

  @Test
  public void testGetParameter() {
    new Expectations() {
      {
        vertxRequest.getParam("name");
        result = "value";
      }
    };

    Assert.assertEquals("value", request.getParameter("name"));
  }

  @Test
  public void testGetParameterValuesNull() {
    Assert.assertEquals(0, request.getParameterValues("name").length);
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

    Assert.assertThat(request.getParameterValues("name"), Matchers.arrayContaining("value"));
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
    Assert.assertThat(result.keySet(), Matchers.contains("name"));
    Assert.assertThat(result.get("name"), Matchers.arrayContaining("value"));
  }

  @Test
  public void testScheme() {
    new Expectations() {
      {
        vertxRequest.scheme();
        result = "abc";
      }
    };

    Assert.assertEquals("abc", request.getScheme());
  }

  @Test
  public void testGetRemoteAddr() {
    new Expectations() {
      {
        socketAddress.host();
        result = "host";
      }
    };

    Assert.assertEquals("host", request.getRemoteAddr());
  }

  @Test
  public void testGetRemoteAddrNull() {
    new Expectations() {
      {
        socketAddress.host();
        result = null;
      }
    };
    Assert.assertEquals(null, request.getRemoteAddr());
  }

  @Test
  public void testGetRemoteHost() {
    new Expectations() {
      {
        socketAddress.host();
        result = "host";
      }
    };

    Assert.assertEquals("host", request.getRemoteHost());
  }

  @Test
  public void testGetRemotePort() {
    new Expectations() {
      {
        socketAddress.port();
        result = 1234;
      }
    };

    Assert.assertEquals(1234, request.getRemotePort());
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

    Assert.assertEquals("host", request.getLocalAddr());
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

    Assert.assertEquals(1234, request.getLocalPort());
  }

  @Test
  public void testGetHeader() {
    new Expectations() {
      {
        vertxRequest.getHeader("key");
        result = "value";
      }
    };

    Assert.assertEquals("value", request.getHeader("key"));
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

    Assert.assertThat(Collections.list(request.getHeaders("name")), Matchers.contains("value"));
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

    Assert.assertThat(Collections.list(request.getHeaderNames()), Matchers.contains("name"));

  }

  @Test
  public void testGetIntHeaderNotExist() {
    Assert.assertEquals(-1, request.getIntHeader("key"));
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
      Assert.fail("must throw exception");
    } catch (NumberFormatException e) {
      Assert.assertEquals("For input string: \"value\"", e.getMessage());
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

    Assert.assertEquals(1, request.getIntHeader("key"));
  }

  @Test
  public void testGetMethod() {
    new Expectations() {
      {
        vertxRequest.method();
        result = HttpMethod.GET;
      }
    };

    Assert.assertEquals("GET", request.getMethod());
  }

  @Test
  public void testGetPathInfo() {
    new Expectations() {
      {
        vertxRequest.path();
        result = "/path";
      }
    };

    Assert.assertEquals("/path", request.getPathInfo());
  }

  @Test
  public void testGetQueryString() {
    new Expectations() {
      {
        vertxRequest.query();
        result = "k1=v1&k2=v2";
      }
    };

    Assert.assertEquals("k1=v1&k2=v2", request.getQueryString());
  }

  @Test
  public void testGetRequestURI() {
    new Expectations() {
      {
        vertxRequest.path();
        result = "/path";
      }
    };

    Assert.assertEquals("/path", request.getRequestURI());
  }

  @Test
  public void testGetServletPath() {
    new Expectations() {
      {
        vertxRequest.path();
        result = "/path";
      }
    };

    Assert.assertEquals("/path", request.getServletPath());
  }

  @Test
  public void testGetContextPath() {
    Assert.assertEquals("", request.getContextPath());
  }

  @Test
  public void testGetInputStream() throws IOException {
    Buffer body = Buffer.buffer();
    body.appendByte((byte) 1);

    new Expectations() {
      {
        context.getBody();
        result = body;
      }
    };

    ServletInputStream is = request.getInputStream();
    Assert.assertSame(is, request.getInputStream());
    int value = is.read();
    is.close();
    Assert.assertEquals(1, value);
  }

  @Test
  public void testGetAsyncContext() {
    AsyncContext asyncContext =
        Deencapsulation.getField(VertxServerRequestToHttpServletRequest.class, "EMPTY_ASYNC_CONTEXT");

    Assert.assertSame(asyncContext, request.getAsyncContext());
  }
}
