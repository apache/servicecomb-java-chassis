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

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.servicecomb.core.tracing.TraceIdLogger;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRestClientInvocation {

  private static final String TARGET_MICROSERVICE_NAME = "TargetMS";

  Handler<Buffer> bodyHandler;

  MultiMap headers = new HeadersMultiMap();

  HttpClientRequest request = mock(HttpClientRequest.class);

  HttpClientResponse httpClientResponse = mock(HttpClientResponse.class);

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

  @SuppressWarnings({"unchecked"})
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
    when(invocation.getTraceIdLogger()).thenReturn(new TraceIdLogger(invocation));
    when(endpoint.getAddress()).thenReturn(address);
    when(invocation.getHandlerContext()).then(answer -> handlerContext);
    when(invocation.getInvocationStageTrace()).thenReturn(invocationStageTrace);
    when(httpClient.request(ArgumentMatchers.any()))
        .thenReturn(Future.succeededFuture(request));
    when(request.headers()).thenReturn(headers);
    when(request.response()).thenReturn(Future.succeededFuture(httpClientResponse));
    doAnswer(a -> {
      headers.add(a.getArgument(0, String.class), a.getArgument(1, String.class));
      return request;
    }).when(request).putHeader(ArgumentMatchers.any(), ArgumentMatchers.anyString());
    doAnswer(a -> {
      ((Handler<Void>) a.getArguments()[0]).handle(null);
      return null;
    }).when(context).runOnContext(ArgumentMatchers.any());
  }

  @Test
  public void invoke(@Mocked Response resp) throws Exception {
    doAnswer(a -> {
      asyncResp.complete(resp);
      return null;
    }).when(request).end();
    when(request.send()).thenReturn(Future.succeededFuture(mock(HttpClientResponse.class)));
    restClientInvocation.invoke(invocation, asyncResp);

    Assertions.assertSame(resp, response);
    MatcherAssert.assertThat(headers.names(),
        Matchers.containsInAnyOrder(org.apache.servicecomb.core.Const.TARGET_MICROSERVICE,
            org.apache.servicecomb.core.Const.CSE_CONTEXT));
    Assertions.assertEquals(TARGET_MICROSERVICE_NAME, headers.get(org.apache.servicecomb.core.Const.TARGET_MICROSERVICE));
    Assertions.assertEquals("{}", headers.get(org.apache.servicecomb.core.Const.CSE_CONTEXT));
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersRequest());
  }

  @Test
  public void invoke_3rdPartyService(@Mocked Response resp) throws Exception {
    doAnswer(a -> {
      asyncResp.complete(resp);
      return null;
    }).when(request).end();
    when(invocation.isThirdPartyInvocation()).thenReturn(true);
    when(request.send()).thenReturn(Future.succeededFuture(mock(HttpClientResponse.class)));

    restClientInvocation.invoke(invocation, asyncResp);

    Assertions.assertSame(resp, response);
    MatcherAssert.assertThat(headers.names(), Matchers.empty());
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersRequest());
  }

  @Test
  public void invoke_3rdPartyServiceExposeServiceCombHeaders(@Mocked Response resp) throws Exception {
    doAnswer(a -> {
      asyncResp.complete(resp);
      return null;
    }).when(request).end();
    when(request.send()).thenReturn(Future.succeededFuture(mock(HttpClientResponse.class)));
    when(invocation.isThirdPartyInvocation()).thenReturn(true);
    operationConfig.setClientRequestHeaderFilterEnabled(false);

    restClientInvocation.invoke(invocation, asyncResp);

    Assertions.assertSame(resp, response);
    MatcherAssert.assertThat(headers.names(),
        Matchers.containsInAnyOrder(org.apache.servicecomb.core.Const.TARGET_MICROSERVICE,
            org.apache.servicecomb.core.Const.CSE_CONTEXT));
    Assertions.assertEquals(TARGET_MICROSERVICE_NAME, headers.get(org.apache.servicecomb.core.Const.TARGET_MICROSERVICE));
    Assertions.assertEquals("{}", headers.get(org.apache.servicecomb.core.Const.CSE_CONTEXT));
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersRequest());
    operationConfig.setClientRequestHeaderFilterEnabled(true);
  }

  @Test
  public void invoke_endThrow() throws Exception {
    Mockito.doThrow(Error.class).when(request).end();
    when(request.send()).thenReturn(Future.succeededFuture(mock(HttpClientResponse.class)));
    restClientInvocation.invoke(invocation, asyncResp);

    MatcherAssert.assertThat(((InvocationException) response.getResult()).getCause(), Matchers.instanceOf(Error.class));
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersRequest());
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishClientFiltersResponse());
  }

  @Test
  public void invoke_requestThrow() throws Exception {
    Throwable t = new RuntimeExceptionWithoutStackTrace();
    doAnswer(a -> {
      throw t;
    }).when(request).end();
    when(request.send()).thenReturn(Future.succeededFuture(mock(HttpClientResponse.class)));
    restClientInvocation.invoke(invocation, asyncResp);
    restClientInvocation.invoke(invocation, asyncResp);

    MatcherAssert.assertThat(((InvocationException) response.getResult()).getCause(), Matchers.sameInstance(t));
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersRequest());
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishClientFiltersResponse());
  }

  @Test
  public void testSetCseContext() {
    Map<String, String> contextMap = Collections.singletonMap("k", "v");
    when(invocation.getContext()).thenReturn(contextMap);

    restClientInvocation.setCseContext();

    Assertions.assertEquals("x-cse-context={\"k\":\"v\"}\n", headers.toString());
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

    Assertions.assertEquals(
        "Failed to encode and set cseContext, message=cause:RuntimeExceptionWithoutStackTrace,message:null.",
        logCollector.getEvents().get(0).getMessage());
    logCollector.teardown();
  }

  @Test
  public void testSetCseContext_enable_unicode() throws Exception {
    Map<String, String> contextMap = new HashMap<>();
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

    vertxRestInvocation.setContext();

    Assertions.assertEquals("测试", invocation.getContext().get("key"));
    Assertions.assertEquals(StringEscapeUtils.escapeJson("测试"), invocation.getContext().get("encodedKey"));
  }


  @Test
  public void testSetCseContext_disable_unicode() throws Exception {
    Map<String, String> contextMap = new HashMap<>();
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

    vertxRestInvocation.setContext();
    Assertions.assertEquals("测试", invocation.getContext().get("key"));
    Assertions.assertEquals(StringEscapeUtils.escapeJson("测试"), invocation.getContext().get("encodedKey"));
  }


  @SuppressWarnings("unchecked")
  @Test
  public void handleResponse() {
    HttpClientResponse httpClientResponse = mock(HttpClientResponse.class);
    doAnswer(a -> {
      bodyHandler = (Handler<Buffer>) a.getArguments()[0];
      return httpClientResponse;
    }).when(httpClientResponse).bodyHandler(ArgumentMatchers.any());

    Buffer buf = Buffer.buffer();
    new MockUp<RestClientInvocation>(restClientInvocation) {
      @Mock
      void processResponseBody(Buffer responseBuf) {
        asyncResp.success(buf);
      }
    };

    restClientInvocation.handleResponse(httpClientResponse);
    bodyHandler.handle(buf);

    Assertions.assertSame(buf, response.getResult());
  }

  @Test
  public void processResponseBody() {
    Response resp = Response.ok(null);

    HttpClientResponse httpClientResponse = mock(HttpClientResponse.class);
    Deencapsulation.setField(restClientInvocation, "clientResponse", httpClientResponse);

    {
      HttpClientFilter filter = mock(HttpClientFilter.class);
      when(filter.afterReceiveResponse(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(null);
      when(filter.enabled()).thenReturn(true);
      httpClientFilters.add(filter);
    }
    {
      HttpClientFilter filter = mock(HttpClientFilter.class);
      when(filter.afterReceiveResponse(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(resp);
      when(filter.enabled()).thenReturn(true);
      httpClientFilters.add(filter);
    }

    when(invocation.getResponseExecutor()).thenReturn(new ReactiveExecutor());

    restClientInvocation.processResponseBody(null);

    Assertions.assertSame(resp, response);
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersResponse());
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishClientFiltersResponse());
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishReceiveResponse());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void processResponseBody_throw() {
    HttpClientResponse httpClientResponse = mock(HttpClientResponse.class);
    Deencapsulation.setField(restClientInvocation, "clientResponse", httpClientResponse);

    {
      HttpClientFilter filter = mock(HttpClientFilter.class);
      when(filter.afterReceiveResponse(ArgumentMatchers.any(), ArgumentMatchers.any())).thenThrow(Error.class);
      when(filter.enabled()).thenReturn(true);
      httpClientFilters.add(filter);
    }

    when(invocation.getResponseExecutor()).thenReturn(new ReactiveExecutor());
    restClientInvocation.processResponseBody(null);

    MatcherAssert.assertThat(((InvocationException) response.getResult()).getCause(), Matchers.instanceOf(Error.class));
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartClientFiltersResponse());
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishClientFiltersResponse());
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishReceiveResponse());
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishWriteToBuffer());
  }

  @Test
  public void createRequestPath_NoUrlPrefixNoPath() throws Exception {
    when(address.getFirst(DefinitionConst.URL_PREFIX)).thenReturn(null);

    when(urlPathBuilder.createRequestPath(ArgumentMatchers.any())).thenReturn("/path");

    String path = restClientInvocation.createRequestPath(swaggerRestOperation);
    Assertions.assertEquals("/path", path);
  }

  @Test
  public void createRequestPath_noUrlPrefixHavePath() throws Exception {
    handlerContext.put(RestConst.REST_CLIENT_REQUEST_PATH, "/client/path");
    when(address.getFirst(DefinitionConst.URL_PREFIX)).thenReturn(null);

    String path = restClientInvocation.createRequestPath(swaggerRestOperation);
    Assertions.assertEquals("/client/path", path);
  }

  @Test
  public void createRequestPath_haveUrlPrefixNoPath() throws Exception {
    when(address.getFirst(DefinitionConst.URL_PREFIX)).thenReturn("/prefix");

    when(urlPathBuilder.createRequestPath(ArgumentMatchers.any())).thenReturn("/path");

    String path = restClientInvocation.createRequestPath(swaggerRestOperation);
    Assertions.assertEquals("/prefix/path", path);
  }

  @Test
  public void createRequestPath_haveUrlPrefixHavePath() throws Exception {
    when(address.getFirst(DefinitionConst.URL_PREFIX)).thenReturn("/prefix");
    handlerContext.put(RestConst.REST_CLIENT_REQUEST_PATH, "/client/path");

    String path = restClientInvocation.createRequestPath(swaggerRestOperation);
    Assertions.assertEquals("/prefix/client/path", path);
  }

  @Test
  public void createRequestPath_haveUrlPrefixHavePathAndStartWith() throws Exception {
    when(address.getFirst(DefinitionConst.URL_PREFIX)).thenReturn("/prefix");
    handlerContext.put(RestConst.REST_CLIENT_REQUEST_PATH, "/prefix/client/path");

    String path = restClientInvocation.createRequestPath(swaggerRestOperation);
    Assertions.assertEquals("/prefix/client/path", path);
  }
}
