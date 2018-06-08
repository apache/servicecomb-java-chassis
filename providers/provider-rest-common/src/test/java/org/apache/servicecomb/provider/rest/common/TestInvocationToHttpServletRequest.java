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

package org.apache.servicecomb.provider.rest.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.definition.RestParam;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.net.SocketAddress;
import mockit.Expectations;
import mockit.Mocked;

public class TestInvocationToHttpServletRequest {
  @Mocked
  Invocation invocation;

  @Mocked
  OperationMeta operationMeta;

  @Mocked
  RestOperationMeta swaggerOperation;

  @Mocked
  Object[] args;

  @Mocked
  SocketAddress socketAddress;

  Map<String, Object> handlerContext = new HashMap<>();

  HttpServletRequest request;

  @Before
  public void setup() {
    handlerContext.put(Const.REMOTE_ADDRESS, socketAddress);
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = swaggerOperation;
        invocation.getArgs();
        result = args;
        invocation.getHandlerContext();
        result = handlerContext;
      }
    };

    request = new InvocationToHttpServletRequest(invocation);
  }

  @Test
  public void testGetParameterNotFound() {
    new Expectations() {
      {
        swaggerOperation.getParamByName("name");
        result = null;
      }
    };

    Assert.assertNull(request.getParameter("name"));
  }

  @Test
  public void testGetParameterNull(@Mocked RestParam restParam) {
    new Expectations() {
      {
        swaggerOperation.getParamByName("name");
        result = restParam;
        restParam.getValue(args);
        result = null;
      }
    };

    Assert.assertNull(request.getParameter("name"));
  }

  @Test
  public void testGetParameterNormal(@Mocked RestParam restParam) {
    new Expectations() {
      {
        swaggerOperation.getParamByName("name");
        result = restParam;
        restParam.getValue(args);
        result = "value";
      }
    };

    Assert.assertEquals("value", request.getParameter("name"));
  }

  @Test
  public void testGetParameterValuesNotFound() {
    new Expectations() {
      {
        swaggerOperation.getParamByName("name");
        result = null;
      }
    };

    Assert.assertNull(request.getParameterValues("name"));
  }

  @Test
  public void testGetParameterValuesNormal(@Mocked RestParam restParam) {
    new Expectations() {
      {
        swaggerOperation.getParamByName("name");
        result = restParam;
        restParam.getValueAsStrings(args);
        result = new String[] {"value"};
      }
    };

    Assert.assertThat(request.getParameterValues("name"), Matchers.arrayContaining("value"));
  }

  @Test
  public void testGetParameterMap(@Mocked RestParam p1, @Mocked RestParam p2) {
    new Expectations() {
      {
        swaggerOperation.getParamList();
        result = Arrays.asList(p1, p2);
        p1.getValueAsStrings(args);
        result = new String[] {"v1"};
        p1.getParamName();
        result = "p1";
        p2.getValueAsStrings(args);
        result = new String[] {"v2"};
        p2.getParamName();
        result = "p2";
      }
    };

    Map<String, String[]> params = request.getParameterMap();
    Assert.assertThat(params.size(), Matchers.is(2));
    Assert.assertThat(params, Matchers.hasEntry("p1", new String[] {"v1"}));
    Assert.assertThat(params, Matchers.hasEntry("p2", new String[] {"v2"}));
  }

  @Test
  public void testGetHeader(@Mocked RestParam restParam) {
    new Expectations() {
      {
        swaggerOperation.getParamByName("name");
        result = restParam;
        restParam.getValue(args);
        result = "value";
      }
    };

    Assert.assertEquals("value", request.getHeader("name"));
  }

  @Test
  public void testGetIntHeaderNotFound(@Mocked RestParam restParam) {
    new Expectations() {
      {
        swaggerOperation.getParamByName("name");
        result = restParam;
        restParam.getValue(args);
        result = null;
      }
    };

    Assert.assertEquals(-1, request.getIntHeader("name"));
  }

  @Test
  public void testGetIntHeaderNotNumber(@Mocked RestParam restParam) {
    new Expectations() {
      {
        swaggerOperation.getParamByName("name");
        result = restParam;
        restParam.getValue(args);
        result = "value";
      }
    };

    try {
      request.getIntHeader("name");
      Assert.fail("must throw exception");
    } catch (NumberFormatException e) {
      Assert.assertEquals("For input string: \"value\"", e.getMessage());
    }
  }

  @Test
  public void testGetIntHeaderNormal(@Mocked RestParam restParam) {
    new Expectations() {
      {
        swaggerOperation.getParamByName("name");
        result = restParam;
        restParam.getValue(args);
        result = "1";
      }
    };

    Assert.assertEquals(1, request.getIntHeader("name"));
  }

  @Test
  public void testGetMethod() {
    new Expectations() {
      {
        swaggerOperation.getHttpMethod();
        result = "GET";
      }
    };

    Assert.assertEquals("GET", request.getMethod());
  }

  @Test
  public void testGetPathInfoNormal(@Mocked URLPathBuilder builder) throws Exception {
    new Expectations() {
      {
        swaggerOperation.getPathBuilder();
        result = builder;
        builder.createPathString(args);
        result = "/path";
      }
    };

    Assert.assertEquals("/path", request.getPathInfo());
  }

  @Test
  public void testGetPathInfoException(@Mocked URLPathBuilder builder) throws Exception {
    new Expectations() {
      {
        swaggerOperation.getPathBuilder();
        result = builder;
        builder.createPathString(args);
        result = new Exception("error");
      }
    };

    try {
      request.getPathInfo();
      Assert.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assert.assertEquals("Failed to get path info.", e.getMessage());
      Assert.assertEquals("error", e.getCause().getMessage());
    }
  }

  @Test
  public void testGetRemoteAddress() throws Exception {
    new Expectations() {
      {
        socketAddress.host();
        result = "127.0.0.2";
        socketAddress.port();
        result = 8088;
      }
    };
    String addr = request.getRemoteAddr();
    String host = request.getRemoteHost();
    int port = request.getRemotePort();
    Assert.assertEquals(addr, "127.0.0.2");
    Assert.assertEquals(host, "127.0.0.2");
    Assert.assertEquals(port, 8088);
  }

  @Test
  public void testGetRemoteAddressEmpty(@Mocked Invocation invocation) throws Exception {
    handlerContext.remove(Const.REMOTE_ADDRESS);
    new Expectations() {
      {
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = swaggerOperation;
        invocation.getArgs();
        result = args;
        invocation.getHandlerContext();
        result = handlerContext;
      }
    };
    InvocationToHttpServletRequest request = new InvocationToHttpServletRequest(invocation);
    String addr = request.getRemoteAddr();
    String host = request.getRemoteHost();
    int port = request.getRemotePort();
    Assert.assertEquals(addr, "");
    Assert.assertEquals(host, "");
    Assert.assertEquals(port, 0);
  }

  @Test
  public void testGetContextPath(@Mocked Invocation invocation) throws Exception {
    InvocationToHttpServletRequest request = new InvocationToHttpServletRequest(invocation);
    Assert.assertEquals("", request.getContextPath());
  }

  @Test
  public void getContentType() {
    Assert.assertNull(request.getContentType());
  }

  @Test
  public void getCharacterEncoding() {
    Assert.assertNull(request.getCharacterEncoding());
  }
}
