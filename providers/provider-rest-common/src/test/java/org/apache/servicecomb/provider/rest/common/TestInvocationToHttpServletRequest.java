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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import io.vertx.core.net.SocketAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestInvocationToHttpServletRequest {
  Invocation invocation = Mockito.mock(Invocation.class);

  OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

  RestOperationMeta swaggerOperation = Mockito.mock(RestOperationMeta.class);

  Map<String, Object> args;

  SocketAddress socketAddress = Mockito.mock(SocketAddress.class);

  Map<String, Object> handlerContext = new HashMap<>();

  HttpServletRequest request;

  @BeforeEach
  public void setup() {
    handlerContext.put(Const.REMOTE_ADDRESS, socketAddress);
    args = new HashMap<>();

   Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
   Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(swaggerOperation);

    request = new InvocationToHttpServletRequest(invocation);
  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void testGetParameterNotFound() {
    Mockito.when(swaggerOperation.getParamByName("name")).thenReturn(null);

    Assertions.assertNull(request.getParameter("name"));
  }

  @Test
  public void testGetParameterNull() {
    RestParam restParam = Mockito.mock(RestParam.class);
    Mockito.when(swaggerOperation.getParamByName("name")).thenReturn(restParam);
    Mockito.when(restParam.getValue(args)).thenReturn(null);

    Assertions.assertNull(request.getParameter("name"));
  }

  @Test
  public void testGetParameterNormal() {
    RestParam restParam = Mockito.mock(RestParam.class);
    Mockito.when(swaggerOperation.getParamByName("name")).thenReturn(restParam);
    Mockito.when(restParam.getValue(args)).thenReturn("value");

    Assertions.assertEquals("value", request.getParameter("name"));
  }

  @Test
  public void testGetParameterValuesNotFound() {
    Mockito.when(swaggerOperation.getParamByName("name")).thenReturn(null);

    Assertions.assertNull(request.getParameterValues("name"));
  }

  @Test
  public void testGetParameterValuesNormal() {
    RestParam restParam = Mockito.mock(RestParam.class);
    Mockito.when(swaggerOperation.getParamByName("name")).thenReturn(restParam);
    Mockito.when(restParam.getValueAsStrings(args)).thenReturn(new String[] {"value"});

    MatcherAssert.assertThat(request.getParameterValues("name"), Matchers.arrayContaining("value"));
  }

  @Test
  public void testGetParameterMap() {
    RestParam p1 = Mockito.mock(RestParam.class);
    RestParam p2 = Mockito.mock(RestParam.class);
    Mockito.when(swaggerOperation.getParamList()).thenReturn(Arrays.asList(p1, p2));
    Mockito.when(p1.getValueAsStrings(args)).thenReturn(new String[] {"v1"});
    Mockito.when(p1.getParamName()).thenReturn("p1");
    Mockito.when(p2.getValueAsStrings(args)).thenReturn(new String[] {"v2"});
    Mockito.when(p2.getParamName()).thenReturn("p2");

    Map<String, String[]> params = request.getParameterMap();
    MatcherAssert.assertThat(params.size(), Matchers.is(2));
    MatcherAssert.assertThat(params, Matchers.hasEntry("p1", new String[] {"v1"}));
    MatcherAssert.assertThat(params, Matchers.hasEntry("p2", new String[] {"v2"}));
  }

  @Test
  public void testGetHeader() {
    RestParam restParam = Mockito.mock(RestParam.class);
    Mockito.when(swaggerOperation.getParamByName("name")).thenReturn(restParam);
    Mockito.when(restParam.getValue(args)).thenReturn("value");

    Assertions.assertEquals("value", request.getHeader("name"));
  }

  @Test
  public void testGetIntHeaderNotFound() {
    RestParam restParam = Mockito.mock(RestParam.class);
    Mockito.when(swaggerOperation.getParamByName("name")).thenReturn(restParam);
    Mockito.when(restParam.getValue(args)).thenReturn(null);

    Assertions.assertEquals(-1, request.getIntHeader("name"));
  }

  @Test
  public void testGetIntHeaderNotNumber() {
    RestParam restParam = Mockito.mock(RestParam.class);
    Mockito.when(swaggerOperation.getParamByName("name")).thenReturn(restParam);
    Mockito.when(restParam.getValue(args)).thenReturn("value");

    try {
      request.getIntHeader("name");
      Assertions.fail("must throw exception");
    } catch (NumberFormatException e) {
      Assertions.assertEquals("For input string: \"value\"", e.getMessage());
    }
  }

  @Test
  public void testGetIntHeaderNormal() {
    RestParam restParam = Mockito.mock(RestParam.class);
    Mockito.when(swaggerOperation.getParamByName("name")).thenReturn(restParam);
    Mockito.when(restParam.getValue(args)).thenReturn("1");

    Assertions.assertEquals(1, request.getIntHeader("name"));
  }

  @Test
  public void testGetMethod() {
    Mockito.when(swaggerOperation.getHttpMethod()).thenReturn("GET");

    Assertions.assertEquals("GET", request.getMethod());
  }

  @Test
  public void testGetPathInfoNormal() throws Exception {
    URLPathBuilder builder = Mockito.mock(URLPathBuilder.class);
    Mockito.when(swaggerOperation.getPathBuilder()).thenReturn(builder);
    Mockito.when(builder.createPathString(args)).thenReturn("/path");

    Assertions.assertEquals("/path", request.getPathInfo());
  }

  @Test
  public void testGetPathInfoException() throws Exception {
    URLPathBuilder builder = Mockito.mock(URLPathBuilder.class);
    Mockito.when(swaggerOperation.getPathBuilder()).thenReturn(builder);
    Mockito.when(builder.createPathString(args)).thenThrow(new Exception("error"));

    try {
      request.getPathInfo();
      Assertions.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assertions.assertEquals("Failed to get path info.", e.getMessage());
      Assertions.assertEquals("error", e.getCause().getMessage());
    }
  }

  @Test
  public void testGetRemoteAddress() throws Exception {
    Mockito.when(socketAddress.host()).thenReturn("127.0.0.2");
    Mockito.when(socketAddress.port()).thenReturn(8088);
    Mockito.when(invocation.getHandlerContext()).thenReturn(handlerContext);
    String addr = request.getRemoteAddr();
    String host = request.getRemoteHost();
    int port = request.getRemotePort();
    Assertions.assertEquals(addr, "127.0.0.2");
    Assertions.assertEquals(host, "127.0.0.2");
    Assertions.assertEquals(port, 8088);
  }

  @Test
  public void testGetRemoteAddressEmpty() throws Exception {
    Invocation invocation = Mockito.mock(Invocation.class);

    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(swaggerOperation);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    handlerContext.remove(Const.REMOTE_ADDRESS);
    Mockito.when(invocation.getHandlerContext()).thenReturn(handlerContext);
    InvocationToHttpServletRequest request = new InvocationToHttpServletRequest(invocation);
    String addr = request.getRemoteAddr();
    String host = request.getRemoteHost();
    int port = request.getRemotePort();
    Assertions.assertEquals(addr, "");
    Assertions.assertEquals(host, "");
    Assertions.assertEquals(port, 0);
  }

  @Test
  public void testGetContextPath() {
    InvocationToHttpServletRequest request = new InvocationToHttpServletRequest(invocation);
    Assertions.assertEquals("", request.getContextPath());
  }

  @Test
  public void getContentType() {
    Assertions.assertNull(request.getContentType());
  }

  @Test
  public void getCharacterEncoding() {
    Assertions.assertNull(request.getCharacterEncoding());
  }
}
