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

package org.apache.servicecomb.transport.rest.client.http;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Part;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Level;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.VertxRestInvocation;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder;
import org.apache.servicecomb.common.rest.filter.HttpClientFilter;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationConfig;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.core.tracing.ScbMarker;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.ReadStreamPart;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultEndpointMetric;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultHttpSocketMetric;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.Http1xConnectionBaseEx;
import io.vertx.core.http.impl.WebSocketImpl;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRestClientInvocation {

  private static final String TARGET_MICROSERVICE_NAME = "TargetMS";

  Handler<Throwable> exceptionHandler;

  Handler<Buffer> bodyHandler;

  MultiMap headers = new VertxHttpHeaders();

  HttpClientRequest request = mock(HttpClientRequest.class);

  HttpClient httpClient = mock(HttpClient.class);

  Context context = mock(Context.class);

  HttpClientWithContext httpClientWithContext = new HttpClientWithContext(httpClient, context);

  Invocation invocation = mock(Invocation.class);

  InvocationStageTrace invocationStageTrace = new InvocationStageTrace(invocation);

  Response response;

  AsyncResponse asyncResp = resp -> response = resp;

  OperationMeta operationMeta = mock(OperationMeta.class);

  Endpoint endpoint = mock(Endpoint.class);

  RestOperationMeta swaggerRestOperation = mock(RestOperationMeta.class);

  URLPathBuilder urlPathBuilder = mock(URLPathBuilder.class);

  URIEndpointObject address = mock(URIEndpointObject.class);

  List<HttpClientFilter> httpClientFilters = new ArrayList<>();

  RestClientInvocation restClientInvocation = new RestClientInvocation(httpClientWithContext, httpClientFilters);

  Map<String, Object> handlerContext = new HashMap<>();

  static long nanoTime = 123;

  @Mocked
  Http1xConnectionBaseEx<WebSocketImpl> connectionBase;

  OperationConfig operationConfig = new OperationConfig();

  @BeforeClass
  public static void classSetup() {
    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return nanoTime;
      }
    };
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  @Before
  public void setup() {
    Deencapsulation.setField(restClientInvocation, "clientRequest", request);
    Deencapsulation.setField(restClientInvocation, "invocation", invocation);
    Deencapsulation.setField(restClientInvocation, "asyncResp", asyncResp);

    when(invocation.getMicroserviceName()).thenReturn(TARGET_MICROSERVICE_NAME);
    when(invocation.getOperationMeta()).thenReturn(operationMeta);
    when(swaggerRestOperation.getPathBuilder()).thenReturn(urlPathBuilder);
    when(swaggerRestOperation.getHttpMethod()).thenReturn("GET");
    when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(swaggerRestOperation);
    when(operationMeta.getConfig()).thenReturn(operationConfig);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(invocation.getMarker()).thenReturn(new ScbMarker(invocation));
    when(endpoint.getAddress()).thenReturn(address);
    when(invocation.getHandlerContext()).then(answer -> handlerContext);
    when(invocation.getInvocationStageTrace()).thenReturn(invocationStageTrace);
    when(httpClient.request(Mockito.any(), (RequestOptions) Mockito.any(), Mockito.any()))
        .thenReturn(request);
    when(request.headers()).thenReturn(headers);

    doAnswer(a -> {
      exceptionHandler = (Handler<Throwable>) a.getArguments()[0];
      return request;
    }).when(request).exceptionHandler(any());
    doAnswer(a -> {
      headers.add(a.getArgumentAt(0, String.class), a.getArgumentAt(1, String.class));
      return request;
    }).when(request).putHeader(any(), (String) any());
    doAnswer(a -> {
      ((Handler<Void>) a.getArguments()[0]).handle(null);
      return null;
    }).when(context).runOnContext(any());
  }

  @Test
  public void invoke(@Mocked Response resp) throws Exception {
    doAnswer(a -> {
      asyncResp.complete(resp);
      return null;
    }).when(request).end();
    restClientInvocation.invoke(invocation, asyncResp);

    Assert.assertSame(resp, response);
    Assert.assertThat(headers.names(),
        Matchers.containsInAnyOrder(org.apache.servicecomb.core.Const.TARGET_MICROSERVICE,
            org.apache.servicecomb.core.Const.CSE_CONTEXT));
    Assert.assertEquals(TARGET_MICROSERVICE_NAME, headers.get(org.apache.servicecomb.core.Const.TARGET_MICROSERVICE));
    Assert.assertEquals("{}", headers.get(org.apache.servicecomb.core.Const.CSE_CONTEXT));
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersRequest());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartSend());
  }

  @Test
  public void invoke_3rdPartyService(@Mocked Response resp) throws Exception {
    doAnswer(a -> {
      asyncResp.complete(resp);
      return null;
    }).when(request).end();
    when(invocation.isThirdPartyInvocation()).thenReturn(true);

    restClientInvocation.invoke(invocation, asyncResp);

    Assert.assertSame(resp, response);
    Assert.assertThat(headers.names(), Matchers.empty());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersRequest());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartSend());
  }

  @Test
  public void invoke_3rdPartyServiceExposeServiceCombHeaders(@Mocked Response resp) throws Exception {
    doAnswer(a -> {
      asyncResp.complete(resp);
      return null;
    }).when(request).end();
    when(invocation.isThirdPartyInvocation()).thenReturn(true);
    operationConfig.setClientRequestHeaderFilterEnabled(false);

    restClientInvocation.invoke(invocation, asyncResp);

    Assert.assertSame(resp, response);
    Assert.assertThat(headers.names(),
        Matchers.containsInAnyOrder(org.apache.servicecomb.core.Const.TARGET_MICROSERVICE,
            org.apache.servicecomb.core.Const.CSE_CONTEXT));
    Assert.assertEquals(TARGET_MICROSERVICE_NAME, headers.get(org.apache.servicecomb.core.Const.TARGET_MICROSERVICE));
    Assert.assertEquals("{}", headers.get(org.apache.servicecomb.core.Const.CSE_CONTEXT));
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersRequest());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartSend());
    operationConfig.setClientRequestHeaderFilterEnabled(true);
  }

  @Test
  public void invoke_endThrow() throws Exception {
    Mockito.doThrow(Error.class).when(request).end();
    restClientInvocation.invoke(invocation, asyncResp);

    Assert.assertThat(((InvocationException) response.getResult()).getCause(), Matchers.instanceOf(Error.class));
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersRequest());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishClientFiltersResponse());
  }

  @Test
  public void invoke_requestThrow() throws Exception {
    Throwable t = new RuntimeExceptionWithoutStackTrace();
    doAnswer(a -> {
      exceptionHandler.handle(t);
      return null;
    }).when(request).end();
    restClientInvocation.invoke(invocation, asyncResp);
    restClientInvocation.invoke(invocation, asyncResp);

    Assert.assertThat(((InvocationException) response.getResult()).getCause(), Matchers.sameInstance(t));
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersRequest());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishClientFiltersResponse());
  }

  @Test
  public void testSetCseContext() {
    Map<String, String> contextMap = Collections.singletonMap("k", "v");
    when(invocation.getContext()).thenReturn(contextMap);

    restClientInvocation.setCseContext();

    Assert.assertEquals("x-cse-context: {\"k\":\"v\"}\n", headers.toString());
  }

  @Test
  public void testSetCseContext_failed() throws JsonProcessingException {
    LogCollector logCollector = new LogCollector();
    logCollector.setLogLevel(RestClientInvocation.class.getName(), Level.DEBUG);

    new Expectations(JsonUtils.class) {
      {
        JsonUtils.writeUnicodeValueAsString(any);
        result = new RuntimeExceptionWithoutStackTrace();
      }
    };

    restClientInvocation.setCseContext();

    Assert.assertEquals("Failed to encode and set cseContext.", logCollector.getEvents().get(0).getMessage());
    logCollector.teardown();
  }

  @Test
  public void testSetCseContext_enable_unicode() {
    Map<String, String> contextMap = new HashMap<String, String>();
    contextMap.put("key", "测试");
    contextMap.put("encodedKey", StringEscapeUtils.escapeJson("测试"));
    when(invocation.getContext()).thenReturn(contextMap);
    restClientInvocation.setCseContext();

    String context =  headers.get(org.apache.servicecomb.core.Const.CSE_CONTEXT);
    HttpServletRequestEx requestEx = new MockUp<HttpServletRequestEx>(){
      @Mock
      public String getHeader(String name){
        if (StringUtils.equals(name, org.apache.servicecomb.core.Const.CSE_CONTEXT)){
          return context;
        } else {
          return null;
        }
      }
    }.getMockInstance();

    VertxRestInvocation vertxRestInvocation = new VertxRestInvocation();
    Deencapsulation.setField(vertxRestInvocation, "requestEx", requestEx);
    Deencapsulation.setField(vertxRestInvocation, "invocation", invocation);

    Deencapsulation.invoke(vertxRestInvocation, "setContext");

    Assert.assertEquals("测试", invocation.getContext().get("key"));
    Assert.assertEquals(StringEscapeUtils.escapeJson("测试"), invocation.getContext().get("encodedKey"));
  }


  @Test
  public void testSetCseContext_disable_unicode() throws JsonProcessingException {
    Map<String, String> contextMap = new HashMap<String, String>();
    contextMap.put("key", "测试");
    contextMap.put("encodedKey", StringEscapeUtils.escapeJson("测试"));
    when(invocation.getContext()).thenReturn(contextMap);

    new MockUp<JsonUtils>() {
      @Mock
      public String writeUnicodeValueAsString(Object value) throws JsonProcessingException {
        return JsonUtils.writeValueAsString(value);
      }
    };
    
    restClientInvocation.setCseContext();
    String context =  headers.get(org.apache.servicecomb.core.Const.CSE_CONTEXT);
    HttpServletRequestEx requestEx = new MockUp<HttpServletRequestEx>(){
      @Mock
      public String getHeader(String name){
        if (StringUtils.equals(name, org.apache.servicecomb.core.Const.CSE_CONTEXT)){
          return context;
        } else {
          return null;
        }
      }
    }.getMockInstance();

    VertxRestInvocation vertxRestInvocation = new VertxRestInvocation();
    Deencapsulation.setField(vertxRestInvocation, "requestEx", requestEx);
    Deencapsulation.setField(vertxRestInvocation, "invocation", invocation);

    Deencapsulation.invoke(vertxRestInvocation, "setContext");

    Assert.assertEquals("测试", invocation.getContext().get("key"));
    Assert.assertEquals(StringEscapeUtils.escapeJson("测试"), invocation.getContext().get("encodedKey"));
  }


  @SuppressWarnings("unchecked")
  @Test
  public void handleResponse() {
    HttpClientResponse httpClientResponse = mock(HttpClientResponse.class);
    doAnswer(a -> {
      bodyHandler = (Handler<Buffer>) a.getArguments()[0];
      return httpClientResponse;
    }).when(httpClientResponse).bodyHandler(any());

    Buffer buf = Buffer.buffer();
    new MockUp<RestClientInvocation>(restClientInvocation) {
      @Mock
      void processResponseBody(Buffer responseBuf) {
        asyncResp.success(buf);
      }
    };

    restClientInvocation.handleResponse(httpClientResponse);
    bodyHandler.handle(buf);

    Assert.assertSame(buf, response.getResult());
  }

  public Part returnPart() {
    return null;
  }

  @Test
  public void handleResponse_readStreamPart() {
    HttpClientResponse httpClientResponse = mock(HttpClientResponse.class);
    when(httpClientResponse.statusCode()).thenReturn(200);
    Method method = ReflectUtils.findMethod(this.getClass(), "returnPart");
    when(operationMeta.getMethod()).thenReturn(method);
    new MockUp<RestClientInvocation>(restClientInvocation) {
      @Mock
      void processResponseBody(Buffer responseBuf) {
      }
    };

    restClientInvocation.handleResponse(httpClientResponse);

    Assert.assertThat(handlerContext.get(RestConst.READ_STREAM_PART), Matchers.instanceOf(ReadStreamPart.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void handleResponse_responseException() {
    HttpClientResponse httpClientResponse = mock(HttpClientResponse.class);

    NetSocket netSocket = mock(NetSocket.class);
    when(httpClientResponse.netSocket()).thenReturn(netSocket);
    when(netSocket.remoteAddress()).thenReturn(mock(SocketAddress.class));

    doAnswer(a -> {
      exceptionHandler = (Handler<Throwable>) a.getArguments()[0];
      return httpClientResponse;
    }).when(httpClientResponse).exceptionHandler(any());

    restClientInvocation.handleResponse(httpClientResponse);
    RuntimeException error = new RuntimeExceptionWithoutStackTrace();
    exceptionHandler.handle(error);

    Assert.assertThat(((InvocationException) response.getResult()).getCause(), Matchers.sameInstance(error));
  }

  @Test
  public void processResponseBody() {
    Response resp = Response.ok(null);

    HttpClientResponse httpClientResponse = mock(HttpClientResponse.class);
    Deencapsulation.setField(restClientInvocation, "clientResponse", httpClientResponse);

    {
      HttpClientFilter filter = mock(HttpClientFilter.class);
      when(filter.afterReceiveResponse(any(), any())).thenReturn(null);
      when(filter.enabled()).thenReturn(true);
      httpClientFilters.add(filter);
    }
    {
      HttpClientFilter filter = mock(HttpClientFilter.class);
      when(filter.afterReceiveResponse(any(), any())).thenReturn(resp);
      when(filter.enabled()).thenReturn(true);
      httpClientFilters.add(filter);
    }

    when(invocation.getResponseExecutor()).thenReturn(new ReactiveExecutor());

    new Expectations() {
      {
        connectionBase.metric();
        result = Mockito.mock(DefaultHttpSocketMetric.class);
      }
    };

    DefaultHttpSocketMetric httpSocketMetric = new DefaultHttpSocketMetric(Mockito.mock(DefaultEndpointMetric.class));
    httpSocketMetric.requestBegin();
    httpSocketMetric.requestEnd();
    new Expectations() {
      {
        connectionBase.metric();
        result = httpSocketMetric;
      }
    };
    when(request.connection()).thenReturn(connectionBase);
    restClientInvocation.processResponseBody(null);

    Assert.assertSame(resp, response);
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersResponse());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishClientFiltersResponse());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishReceiveResponse());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishGetConnection());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishWriteToBuffer());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void processResponseBody_throw() {
    HttpClientResponse httpClientResponse = mock(HttpClientResponse.class);
    Deencapsulation.setField(restClientInvocation, "clientResponse", httpClientResponse);

    {
      HttpClientFilter filter = mock(HttpClientFilter.class);
      when(filter.afterReceiveResponse(any(), any())).thenThrow(Error.class);
      when(filter.enabled()).thenReturn(true);
      httpClientFilters.add(filter);
    }

    when(invocation.getResponseExecutor()).thenReturn(new ReactiveExecutor());
    new Expectations() {
      {
        connectionBase.metric();
        result = Mockito.mock(DefaultHttpSocketMetric.class);
      }
    };

    DefaultHttpSocketMetric httpSocketMetric = new DefaultHttpSocketMetric(Mockito.mock(DefaultEndpointMetric.class));
    httpSocketMetric.requestBegin();
    httpSocketMetric.requestEnd();
    new Expectations() {
      {
        connectionBase.metric();
        result = httpSocketMetric;
      }
    };
    when(request.connection()).thenReturn(connectionBase);
    restClientInvocation.processResponseBody(null);

    Assert.assertThat(((InvocationException) response.getResult()).getCause(), Matchers.instanceOf(Error.class));
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersResponse());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishClientFiltersResponse());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishReceiveResponse());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishGetConnection());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishWriteToBuffer());
  }

  @Test
  public void createRequestPath_NoUrlPrefixNoPath() throws Exception {
    when(address.getFirst(Const.URL_PREFIX)).thenReturn(null);

    when(urlPathBuilder.createRequestPath(any())).thenReturn("/path");

    String path = restClientInvocation.createRequestPath(swaggerRestOperation);
    Assert.assertEquals("/path", path);
  }

  @Test
  public void createRequestPath_noUrlPrefixHavePath() throws Exception {
    handlerContext.put(RestConst.REST_CLIENT_REQUEST_PATH, "/client/path");
    when(address.getFirst(Const.URL_PREFIX)).thenReturn(null);

    String path = restClientInvocation.createRequestPath(swaggerRestOperation);
    Assert.assertEquals("/client/path", path);
  }

  @Test
  public void createRequestPath_haveUrlPrefixNoPath() throws Exception {
    when(address.getFirst(Const.URL_PREFIX)).thenReturn("/prefix");

    when(urlPathBuilder.createRequestPath(any())).thenReturn("/path");

    String path = restClientInvocation.createRequestPath(swaggerRestOperation);
    Assert.assertEquals("/prefix/path", path);
  }

  @Test
  public void createRequestPath_haveUrlPrefixHavePath() throws Exception {
    when(address.getFirst(Const.URL_PREFIX)).thenReturn("/prefix");
    handlerContext.put(RestConst.REST_CLIENT_REQUEST_PATH, "/client/path");

    String path = restClientInvocation.createRequestPath(swaggerRestOperation);
    Assert.assertEquals("/prefix/client/path", path);
  }

  @Test
  public void createRequestPath_haveUrlPrefixHavePathAndStartWith() throws Exception {
    when(address.getFirst(Const.URL_PREFIX)).thenReturn("/prefix");
    handlerContext.put(RestConst.REST_CLIENT_REQUEST_PATH, "/prefix/client/path");

    String path = restClientInvocation.createRequestPath(swaggerRestOperation);
    Assert.assertEquals("/prefix/client/path", path);
  }
}
