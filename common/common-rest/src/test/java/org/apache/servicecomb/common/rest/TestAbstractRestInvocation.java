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

package org.apache.servicecomb.common.rest;

import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;
import static com.google.common.net.HttpHeaders.TRANSFER_ENCODING;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.apache.servicecomb.common.rest.definition.RestMetaUtils;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.common.rest.filter.HttpServerFilterBaseForTest;
import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.common.rest.locator.TestPathSchema;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.http.HttpStatus;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletResponse;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.http.StandardHttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.common.eventbus.Subscribe;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;

@SuppressWarnings("deprecation")
public class TestAbstractRestInvocation {

  HttpServletRequestEx requestEx = Mockito.mock(HttpServletRequestEx.class);

  final HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);

  HttpServletResponseEx responseEx;

  final ReferenceConfig endpoint = Mockito.mock(ReferenceConfig.class);

  Map<String, Object> arguments = new HashMap<>();

  Invocation invocation;

  static SCBEngine scbEngine;

  static OperationMeta operationMeta;

  static RestOperationMeta restOperation;

  static class AbstractHttpServletRequestForTest extends AbstractHttpServletRequest {
    @Override
    public String getHeader(String name) {
      return null;
    }
  }

  class AbstractRestInvocationForTest extends AbstractRestInvocation {
    @Override
    protected OperationLocator locateOperation(ServicePathManager servicePathManager) {
      return null;
    }

    @Override
    protected void createInvocation() {
      this.invocation = TestAbstractRestInvocation.this.invocation;
    }
  }

  AbstractRestInvocation restInvocation = new AbstractRestInvocationForTest();

  @BeforeEach
  public void setup() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new TestPathSchema())
        .run();
    operationMeta = scbEngine.getProducerMicroserviceMeta().operationMetas().get("test.sid1.dynamicId");
    restOperation = RestMetaUtils.getRestOperationMeta(operationMeta);

    if (responseEx == null) {
      responseEx = new StandardHttpServletResponseEx(servletResponse);
    }
    responseEx = Mockito.spy(responseEx);
    arguments = Mockito.spy(arguments);
    invocation = new Invocation(endpoint, operationMeta, operationMeta.buildBaseConsumerRuntimeType(), arguments);

    initRestInvocation();
    List<HttpServerFilter> httpServerFilters = SPIServiceUtils.getSortedService(HttpServerFilter.class);
    restInvocation.setHttpServerFilters(httpServerFilters);
  }

  @AfterEach
  public void teardown() {
    ArchaiusUtils.resetConfig();
    scbEngine.destroy();
  }

  private void initRestInvocation() {
    restInvocation.produceProcessor = ProduceProcessorManager.INSTANCE.findDefaultJsonProcessor();
    restInvocation.requestEx = requestEx;
    restInvocation.responseEx = responseEx;
    restInvocation.invocation = invocation;
    restInvocation.restOperationMeta = restOperation;
  }

  @Test
  public void setHttpServerFilters() {
    List<HttpServerFilter> httpServerFilters = new ArrayList<>();
    httpServerFilters = Mockito.spy(httpServerFilters);
    restInvocation.setHttpServerFilters(httpServerFilters);

    Assertions.assertSame(httpServerFilters, restInvocation.httpServerFilters);
  }

  @Test
  public void initProduceProcessorNull() {
    Mockito.when(requestEx.getHeader(HttpHeaders.ACCEPT)).thenReturn("notExistType");
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      public void sendFailResponse(Throwable throwable) {
      }
    };
    initRestInvocation();

    InvocationException exception = Assertions.assertThrows(InvocationException.class,
        () -> restInvocation.initProduceProcessor());
    Assertions.assertEquals(
        "InvocationException: code=406;msg=CommonExceptionData [message=Accept notExistType is not supported]",
        exception.getMessage());
  }

  @Test
  public void initProduceProcessorNormal() {
    Mockito.when(requestEx.getHeader(HttpHeaders.ACCEPT)).thenReturn(MediaType.APPLICATION_JSON);
    // not throw exception
    restInvocation.initProduceProcessor();
  }

  @Test
  public void setContextNull() throws Exception {
    Mockito.when(requestEx.getHeader(Const.CSE_CONTEXT)).thenReturn(null);

    Map<String, String> context = invocation.getContext();
    restInvocation.setContext();
    Assertions.assertSame(context, invocation.getContext());
  }

  @Test
  public void setContextEmpty() throws Exception {
    Mockito.when(requestEx.getHeader(Const.CSE_CONTEXT)).thenReturn("");

    Map<String, String> context = invocation.getContext();
    restInvocation.setContext();
    Assertions.assertSame(context, invocation.getContext());
  }

  @Test
  public void setContextNormal() throws Exception {
    Map<String, String> context = new HashMap<>();
    context.put("name", "value");
    Mockito.when(requestEx.getHeader(Const.CSE_CONTEXT)).thenReturn(JsonUtils.writeValueAsString(context));

    restInvocation.setContext();
    MatcherAssert.assertThat(invocation.getContext().size(), Matchers.is(1));
    MatcherAssert.assertThat(invocation.getContext(), Matchers.hasEntry("name", "value"));
  }

  @Test
  public void setContextTraceId() throws Exception {
    Map<String, String> context = new HashMap<>();
    Mockito.when(requestEx.getHeader(Const.CSE_CONTEXT)).thenReturn(JsonUtils.writeValueAsString(context));
    invocation.addContext("X-B3-traceId", "value1");
    //if request has no traceId, use invocation's traceId
    restInvocation.setContext();
    MatcherAssert.assertThat(invocation.getContext().size(), Matchers.is(1));
    MatcherAssert.assertThat(invocation.getContext(), Matchers.hasEntry("X-B3-traceId", "value1"));

    context.put("X-B3-traceId", "value2");
    Mockito.when(requestEx.getHeader(Const.CSE_CONTEXT)).thenReturn(JsonUtils.writeValueAsString(context));
    //if request has traceId, use request's traceId
    restInvocation.setContext();
    MatcherAssert.assertThat(invocation.getContext().size(), Matchers.is(1));
    MatcherAssert.assertThat(invocation.getContext(), Matchers.hasEntry("X-B3-traceId", "value2"));
  }

  @Test
  public void getContext() {
    invocation.addContext("key", "test");
    Assertions.assertEquals("test", restInvocation.getContext("key"));
  }

  @Test
  public void getContextNull() {
    Assertions.assertNull(restInvocation.getContext("key"));
  }

  @Test
  public void invokeFilterHaveResponse() {
    HttpServerFilter filter = Mockito.mock(HttpServerFilter.class);
    Response response = Response.ok("");
    Mockito.when(filter.enabled()).thenReturn(true);
    Mockito.when(filter.afterReceiveRequest(invocation, requestEx)).thenReturn(response);

    Holder<Response> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() {
        result.value = Response.ok("not run to here");
      }

      @Override
      protected void sendResponseQuietly(Response response) {
        result.value = response;
      }
    };
    initRestInvocation();
    restInvocation.httpServerFilters = Arrays.asList(filter);

    restInvocation.invoke();

    Assertions.assertSame(response, result.value);
  }

  @Test
  public void invokeFilterNoResponse() {
    HttpServerFilter filter = Mockito.mock(HttpServerFilter.class);
    Mockito.when(filter.enabled()).thenReturn(true);
    Mockito.when(filter.afterReceiveRequest(invocation, requestEx)).thenReturn(null);

    Holder<Boolean> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() {
        result.value = true;
      }
    };
    initRestInvocation();
    restInvocation.httpServerFilters = Arrays.asList(filter);

    restInvocation.invoke();

    Assertions.assertTrue(result.value);
  }

  @Test
  public void invokeFilterNoResponseDisableFilter() {
    HttpServerFilter filter = Mockito.mock(HttpServerFilter.class);
    Mockito.when(filter.enabled()).thenReturn(false);

    Holder<Boolean> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() {
        result.value = true;
      }
    };
    initRestInvocation();
    restInvocation.httpServerFilters = Arrays.asList(filter);

    restInvocation.invoke();

    Assertions.assertTrue(result.value);
  }

  @Test
  public void invokeFilterException() {
    HttpServerFilter filter = Mockito.mock(HttpServerFilter.class);
    Exception error = new RuntimeExceptionWithoutStackTrace();
    Mockito.when(filter.enabled()).thenReturn(true);
    Mockito.when(filter.afterReceiveRequest(invocation, requestEx)).thenThrow(error);

    Holder<Throwable> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      public void sendFailResponse(Throwable throwable) {
        result.value = throwable;
      }

      @Override
      protected void doInvoke() {

      }
    };
    initRestInvocation();
    restInvocation.httpServerFilters = Arrays.asList(filter);

    restInvocation.invoke();

    Assertions.assertSame(error, result.value);
  }

  @Test
  public void invokeNormal() {
    HttpServerFilter filter = Mockito.mock(HttpServerFilter.class);
    Mockito.when(filter.enabled()).thenReturn(true);
    Mockito.when(filter.afterReceiveRequest(invocation, requestEx)).thenReturn(null);

    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() {
      }

      @Override
      public void sendFailResponse(Throwable throwable) {
        Assertions.fail("must not fail");
      }
    };
    initRestInvocation();
    restInvocation.httpServerFilters = Arrays.asList(filter);

    restInvocation.invoke();
  }

  @Test
  public void sendFailResponseNoProduceProcessor() {
    invocation.onStart(0);

    restInvocation.produceProcessor = null;
    restInvocation.sendFailResponse(new RuntimeExceptionWithoutStackTrace());

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultJsonProcessor(),
        restInvocation.produceProcessor);
  }

  @Test
  public void sendFailResponseHaveProduceProcessor() {
    Holder<Response> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() {
      }

      @Override
      protected void sendResponseQuietly(Response response) {
        result.value = response;
      }
    };
    initRestInvocation();
    restInvocation.produceProcessor = ProduceProcessorManager.INSTANCE.findDefaultPlainProcessor();

    Throwable e = new InvocationException(Status.BAD_GATEWAY, "");
    restInvocation.sendFailResponse(e);
    Assertions.assertSame(e, result.value.getResult());
    Assertions.assertSame(
        ProduceProcessorManager.INSTANCE.findDefaultPlainProcessor(), restInvocation.produceProcessor);
  }

  public static class SendResponseQuietlyNormalEventHandler {
    private final Holder<InvocationFinishEvent> eventHolder;

    public SendResponseQuietlyNormalEventHandler(Holder<InvocationFinishEvent> eventHolder) {
      this.eventHolder = eventHolder;
    }

    @Subscribe
    public void onFinished(InvocationFinishEvent event) {
      eventHolder.value = event;
    }
  }

  @Test
  public void sendResponseQuietlyNormal() {
    Response response = Mockito.mock(Response.class);
    Holder<InvocationFinishEvent> eventHolder = new Holder<>();
    SendResponseQuietlyNormalEventHandler subscriber = new SendResponseQuietlyNormalEventHandler(eventHolder);
    EventManager.register(subscriber);

    Holder<Response> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() {
      }

      @Override
      protected void sendResponse(Response response) {
        result.value = response;
        super.sendResponse(response);
      }
    };
    invocation.onStart(0);
    initRestInvocation();

    restInvocation.sendResponseQuietly(response);

    EventManager.unregister(subscriber);

    Assertions.assertSame(invocation, eventHolder.value.getInvocation());
    Assertions.assertSame(response, result.value);
  }

  @Test
  public void sendResponseQuietlyException() {
    Response response = Mockito.mock(Response.class);
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() {
      }

      @Override
      protected void sendResponse(Response response) {
        throw new RuntimeExceptionWithoutStackTrace();
      }
    };
    initRestInvocation();

    restInvocation.sendResponseQuietly(response);

    // just log, check nothing
  }

  @Test
  public void sendResponseQuietlyExceptionOnNullInvocation() {
    Response response = Mockito.mock(Response.class);
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() {
      }

      @Override
      protected void sendResponse(Response response) {
        throw new RuntimeExceptionWithoutStackTrace("");
      }
    };
    initRestInvocation();
    restInvocation.invocation = null;

    restInvocation.sendResponseQuietly(response);

    // just log, check nothing, and should not throw NPE
  }

  @Test
  public void executeHttpServerFiltersNullInvocation() {
    Response response = Mockito.mock(Response.class);
    Holder<Boolean> flag = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void onExecuteHttpServerFiltersFinish(Response response, Throwable e) {
        super.onExecuteHttpServerFiltersFinish(response, e);
        flag.value = true;
      }
    };
    initRestInvocation();
    restInvocation.invocation = null;

    restInvocation.executeHttpServerFilters(response);

    Assertions.assertTrue(flag.value);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void sendResponseStatusAndContentTypeAndHeader() {
    Response response = Mockito.mock(Response.class);
    Mockito.when(response.getStatusCode()).thenReturn(123);
    Mockito.when(response.getReasonPhrase()).thenReturn("reason");
    Mockito.when(response.getResult()).thenReturn("result");

    Map<String, Object> result = new HashMap<>();
    Map<String, Object> attributes = new HashMap<>();
    Mockito.doAnswer(invocationOnMock -> {
      attributes.put(RestConst.INVOCATION_HANDLER_RESPONSE, response);
      return null;
    }).when(responseEx).setAttribute(RestConst.INVOCATION_HANDLER_RESPONSE, response);
    Mockito.when(responseEx.getAttribute(RestConst.INVOCATION_HANDLER_RESPONSE)).thenReturn(response);
    Mockito.doAnswer(invocationOnMock -> {
      result.put("statusCode", 123);
      result.put("reasonPhrase", "reason");
      return null;
    }).when(responseEx).setStatus(123, "reason");
    Mockito.doAnswer(invocationOnMock -> {
      result.put("contentType", "application/json; charset=utf-8");
      return null;
    }).when(responseEx).setContentType("application/json; charset=utf-8");

    Map<String, Object> expected = new HashMap<>();
    expected.put("statusCode", 123);
    expected.put("reasonPhrase", "reason");
    expected.put("contentType", "application/json; charset=utf-8");

    invocation.onStart(0);
    initRestInvocation();

    restInvocation.sendResponse(response);
    Assertions.assertEquals(expected, result);
  }

  @Test
  public void should_ignore_content_length_and_transfer_encoding_when_copy_header_to_http_response() {
    Response response = Mockito.mock(Response.class);
    MultiMap headers = MultiMap.caseInsensitiveMultiMap()
        .set(CONTENT_LENGTH, "10")
        .set(TRANSFER_ENCODING, "encoding");

    Mockito.when(response.getResult()).thenThrow(new RuntimeExceptionWithoutStackTrace("stop"));
    Mockito.when(response.getHeaders()).thenReturn(headers);

    MultiMap resultHeaders = MultiMap.caseInsensitiveMultiMap();

    invocation.onStart(0);
    initRestInvocation();

    restInvocation.sendResponse(response);
    assertThat(headers).isEmpty();
    assertThat(resultHeaders).isEmpty();
  }

  @Test
  public void testDoSendResponseHeaderNormal() {
    Response response = Mockito.mock(Response.class);
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("h1", "h1v1");
    headers.add("h1", "h1v2");
    headers.add("h2", "h2v");

    Mockito.when(response.getResult()).thenThrow(new RuntimeExceptionWithoutStackTrace("stop"));
    Mockito.when(response.getHeaders()).thenReturn(headers);

    MultiMap resultHeaders = MultiMap.caseInsensitiveMultiMap();
    Map<String, Object> attributes = new HashMap<>();
    Mockito.doAnswer(invocationOnMock -> {
      attributes.put(RestConst.INVOCATION_HANDLER_RESPONSE, response);
      return null;
    }).when(responseEx).setAttribute(RestConst.INVOCATION_HANDLER_RESPONSE, response);
    Mockito.when(responseEx.getAttribute(RestConst.INVOCATION_HANDLER_RESPONSE)).thenReturn(response);
    Mockito.doAnswer(invocationOnMock -> {
      resultHeaders.add("h1", "h1v1");
      return null;
    }).when(responseEx).addHeader("h1", "h1v1");
    Mockito.doAnswer(invocationOnMock -> {
      resultHeaders.add("h1", "h1v2");
      return null;
    }).when(responseEx).addHeader("h1", "h1v2");
    Mockito.doAnswer(invocationOnMock -> {
      resultHeaders.add("h2", "h2v");
      return null;
    }).when(responseEx).addHeader("h2", "h2v");

    invocation.onStart(0);
    initRestInvocation();

    try {
      restInvocation.sendResponse(response);
      Assertions.fail("must throw exception");
    } catch (Error e) {
      Assertions.assertEquals(headers.toString(), resultHeaders.toString());
    }
  }

  @Test
  public void testDoSendResponseResultOK() {
    Response response = Mockito.mock(Response.class);
    Mockito.when(response.getResult()).thenReturn("ok");

    Buffer buffer = Buffer.buffer();
    Map<String, Object> attributes = new HashMap<>();
    Mockito.doAnswer(invocationOnMock -> {
      attributes.put(RestConst.INVOCATION_HANDLER_RESPONSE, response);
      return null;
    }).when(responseEx).setAttribute(RestConst.INVOCATION_HANDLER_RESPONSE, response);
    Mockito.when(responseEx.getAttribute(RestConst.INVOCATION_HANDLER_RESPONSE)).thenReturn(response);
    Mockito.doAnswer(invocationOnMock -> {
      buffer.appendBytes("\"ok\"".getBytes(StandardCharsets.UTF_8));
      return null;
    }).when(responseEx).setBodyBuffer(Mockito.any());

    invocation.onStart(0);
    initRestInvocation();

    restInvocation.sendResponse(response);
    Assertions.assertEquals("\"ok\"", buffer.toString());
  }

  @Test
  public void testDoSendResponseResultOKFilter() {
    Response response = Mockito.mock(Response.class);
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.set("Content-Type", "application/json");
    Mockito.when(response.getHeaders()).thenReturn(headers);
    Mockito.when(response.getStatusCode()).thenReturn(123);
    Mockito.when(response.getReasonPhrase()).thenReturn("reason");
    Mockito.when(response.getResult()).thenReturn("ok");

    Buffer buffer = Buffer.buffer();
    Map<String, Object> attributes = new HashMap<>();
    Mockito.doAnswer(invocationOnMock -> {
      attributes.put(RestConst.INVOCATION_HANDLER_RESPONSE, response);
      return null;
    }).when(responseEx).setAttribute(RestConst.INVOCATION_HANDLER_RESPONSE, response);
    Mockito.when(responseEx.getAttribute(RestConst.INVOCATION_HANDLER_RESPONSE)).thenReturn(response);
    Mockito.doAnswer(invocationOnMock -> {
      buffer.appendBytes("\"ok\"".getBytes(StandardCharsets.UTF_8));
      return null;
    }).when(responseEx).setBodyBuffer(Mockito.any());

    HttpServerFilter filter = new HttpServerFilterBaseForTest() {
      @Override
      public CompletableFuture<Void> beforeSendResponseAsync(Invocation invocation, HttpServletResponseEx responseEx) {
        buffer.appendString("-filter");
        return CompletableFuture.completedFuture(null);
      }
    };

    invocation.onStart(0);
    initRestInvocation();
    List<HttpServerFilter> httpServerFilters = SPIServiceUtils.loadSortedService(HttpServerFilter.class);
    httpServerFilters.add(filter);
    restInvocation.setHttpServerFilters(httpServerFilters);

    restInvocation.sendResponse(response);
    Assertions.assertEquals("\"ok\"-filter", buffer.toString());
  }

  @Test
  public void findRestOperationServicePathManagerNull() {
    MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);

    try (MockedStatic<ServicePathManager> mockedStatic = Mockito.mockStatic(ServicePathManager.class)) {
      mockedStatic.when(() -> ServicePathManager.getServicePathManager(microserviceMeta)).thenReturn(null);
      InvocationException exception = Assertions.assertThrows(InvocationException.class,
          () -> restInvocation.findRestOperation(microserviceMeta));
      Assertions.assertEquals("InvocationException: code=404;msg=CommonExceptionData [message=Not Found]",
          exception.getMessage());
    }
  }

  @Test
  public void findRestOperationNormal() {
    try (MockedStatic<ServicePathManager> mockedStatic = Mockito.mockStatic(ServicePathManager.class)) {
      ServicePathManager servicePathManager = Mockito.mock(ServicePathManager.class);
      MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
      OperationLocator locator = Mockito.mock(OperationLocator.class);

      restInvocation = new AbstractRestInvocationForTest() {
        @Override
        protected OperationLocator locateOperation(ServicePathManager servicePathManager) {
          return locator;
        }
      };

      requestEx = new AbstractHttpServletRequest() {
      };
      restInvocation.requestEx = requestEx;
      Map<String, String> pathVars = new HashMap<>();
      mockedStatic.when(() -> ServicePathManager.getServicePathManager(microserviceMeta))
          .thenReturn(servicePathManager);
      Mockito.when(locator.getPathVarMap()).thenReturn(pathVars);
      Mockito.when(locator.getOperation()).thenReturn(restOperation);

      restInvocation.findRestOperation(microserviceMeta);
      Assertions.assertSame(restOperation, restInvocation.restOperationMeta);
      Assertions.assertSame(pathVars, requestEx.getAttribute(RestConst.PATH_PARAMETERS));
    }
  }

  @Test
  public void scheduleInvocationException() {
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    restOperation = Mockito.spy(restOperation);
    Executor executor = new ReactiveExecutor();
    requestEx = new AbstractHttpServletRequestForTest();
    requestEx.setAttribute(RestConst.REST_REQUEST, requestEx);
    Mockito.when(restOperation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getExecutor()).thenReturn(executor);

    Holder<Throwable> result = new Holder<>();
    RuntimeException error = new RuntimeExceptionWithoutStackTrace("run on executor");
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void runOnExecutor() {
        throw error;
      }

      @Override
      public void sendFailResponse(Throwable throwable) {
        result.value = throwable;

        invocation.onFinish(Response.ok(null));
      }
    };
    restInvocation.requestEx = requestEx;
    restInvocation.restOperationMeta = restOperation;

    restInvocation.scheduleInvocation();

    Assertions.assertSame(error, result.value);
  }

  @Test
  public void scheduleInvocationTimeout() {
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    restOperation = Mockito.spy(restOperation);
    Executor executor = Runnable::run;

    Mockito.when(restOperation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getExecutor()).thenReturn(executor);
    Mockito.when(operationMeta.getMicroserviceQualifiedName()).thenReturn("sayHi");

    requestEx = new AbstractHttpServletRequestForTest();

    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void runOnExecutor() {
        throw new RuntimeExceptionWithoutStackTrace("run on executor");
      }
    };
    restInvocation.requestEx = requestEx;
    restInvocation.restOperationMeta = restOperation;

    // will not throw exception
    restInvocation.scheduleInvocation();

    invocation.onFinish(Response.ok(null));
  }

  @Test
  public void threadPoolReject() {
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    restOperation = Mockito.spy(restOperation);
    RejectedExecutionException rejectedExecutionException = new RejectedExecutionException("reject");
    Executor executor = (task) -> {
      throw rejectedExecutionException;
    };

    Mockito.when(restOperation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getExecutor()).thenReturn(executor);

    Holder<Throwable> holder = new Holder<>();
    requestEx = new AbstractHttpServletRequestForTest();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      public void sendFailResponse(Throwable throwable) {
        holder.value = throwable;

        invocation.onFinish(Response.ok(null));
      }
    };
    restInvocation.requestEx = requestEx;
    restInvocation.restOperationMeta = restOperation;

    restInvocation.scheduleInvocation();

    Assertions.assertSame(rejectedExecutionException, holder.value);
  }

  public static class ScheduleInvocationEventHandler {
    private final Holder<InvocationStartEvent> eventHolder;

    public ScheduleInvocationEventHandler(Holder<InvocationStartEvent> eventHolder) {
      this.eventHolder = eventHolder;
    }

    @Subscribe
    public void onFinished(InvocationStartEvent event) {
      eventHolder.value = event;
    }
  }

  @Test
  public void scheduleInvocationNormal() {
    Holder<InvocationStartEvent> eventHolder = new Holder<>();
    Object subscriber = new ScheduleInvocationEventHandler(eventHolder);
    EventManager.register(subscriber);

    Executor executor = new ReactiveExecutor();
    requestEx = new AbstractHttpServletRequestForTest();
    requestEx = Mockito.spy(requestEx);
    responseEx = Mockito.spy(responseEx);
    restOperation = Mockito.spy(restOperation);
    operationMeta = Mockito.spy(operationMeta);
    Mockito.when(restOperation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getExecutor()).thenReturn(executor);
    Mockito.when(requestEx.getHeader(Const.TRACE_ID_NAME)).thenReturn("tid");
    Mockito.when(requestEx.getAttribute(RestConst.REST_REQUEST)).thenReturn(requestEx);

    Holder<Boolean> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void runOnExecutor() {
        result.value = true;

        invocation.onFinish(Response.ok(null));
      }
    };
    restInvocation.requestEx = requestEx;
    restInvocation.restOperationMeta = restOperation;

    restInvocation.scheduleInvocation();
    EventManager.unregister(subscriber);

    Assertions.assertTrue(result.value);
    Assertions.assertSame(invocation, eventHolder.value.getInvocation());
    Assertions.assertEquals("tid", invocation.getTraceId());
  }

  @Test
  public void runOnExecutor() {

    Holder<Boolean> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      public void invoke() {
        result.value = true;
      }
    };
    restInvocation.createInvocation();
    restInvocation.requestEx = requestEx;
    restInvocation.restOperationMeta = restOperation;

    restInvocation.runOnExecutor();

    Assertions.assertTrue(result.value);
    Assertions.assertSame(invocation, restInvocation.invocation);
  }

  @Test
  public void doInvoke() throws Throwable {
    Response response = Response.ok("ok");
    Handler handler = (invocation, asyncResp) -> asyncResp.complete(response);
    invocation.setHandlerList(Arrays.asList(handler));

    Holder<Response> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void sendResponse(Response response) {
        result.value = response;
      }
    };
    restInvocation.invocation = invocation;

    restInvocation.doInvoke();

    Assertions.assertSame(response, result.value);
  }

  @Test
  public void scheduleInvocation_invocationContextDeserializeError() {
    AsyncContext asyncContext = Mockito.mock(AsyncContext.class);
    requestEx = new AbstractHttpServletRequest() {
      @Override
      public String getHeader(String name) {
        return "{\"x-cse-src-microservice\":'source\"}";
      }

      @Override
      public AsyncContext getAsyncContext() {
        return asyncContext;
      }
    };
    Holder<Integer> status = new Holder<>();
    Holder<String> reasonPhrase = new Holder<>();
    Holder<Integer> endCount = new Holder<>(0);
    responseEx = new AbstractHttpServletResponse() {
      @SuppressWarnings("deprecation")
      @Override
      public void setStatus(int sc, String sm) {
        status.value = sc;
        reasonPhrase.value = sm;
      }

      @Override
      public void flushBuffer() {
        endCount.value = endCount.value + 1;
      }

      @Override
      public void setContentType(String type) {
        Assertions.assertEquals("application/json; charset=utf-8", type);
      }
    };
    restInvocation.requestEx = requestEx;
    restInvocation.responseEx = responseEx;

    restInvocation.scheduleInvocation();

    Assertions.assertEquals(Integer.valueOf(590), status.value);
    Assertions.assertEquals("Unexpected producer error, please check logs for details", reasonPhrase.value);
    Assertions.assertEquals(Integer.valueOf(1), endCount.value);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void scheduleInvocation_flowControlReject() {
    operationMeta = Mockito.spy(operationMeta);
    Mockito.when(operationMeta.getProviderQpsFlowControlHandler())
        .thenReturn((invocation, asyncResp) -> asyncResp.producerFail(new InvocationException(
            new HttpStatus(429, "Too Many Requests"),
            new CommonExceptionData("rejected by qps flowcontrol"))));
    restOperation = Mockito.spy(restOperation);
    Mockito.when(restOperation.getOperationMeta()).thenReturn(operationMeta);
    Holder<Integer> status = new Holder<>();
    Holder<String> reasonPhrase = new Holder<>();
    Holder<Integer> endCount = new Holder<>(0);
    Holder<String> responseBody = new Holder<>();
    responseEx = new AbstractHttpServletResponse() {
      @SuppressWarnings("deprecation")
      @Override
      public void setStatus(int sc, String sm) {
        status.value = sc;
        reasonPhrase.value = sm;
      }

      @Override
      public void flushBuffer() {
        endCount.value = endCount.value + 1;
      }

      @Override
      public void setContentType(String type) {
        Assertions.assertEquals("application/json; charset=utf-8", type);
      }

      @Override
      public void setBodyBuffer(Buffer bodyBuffer) {
        responseBody.value = bodyBuffer.toString();
      }
    };
    initRestInvocation();
    restInvocation.scheduleInvocation();

    Assertions.assertEquals(Integer.valueOf(429), status.value);
    Assertions.assertEquals("Too Many Requests", reasonPhrase.value);
    Assertions.assertEquals("{\"message\":\"rejected by qps flowcontrol\"}", responseBody.value);
    Assertions.assertEquals(Integer.valueOf(1), endCount.value);
  }
}
