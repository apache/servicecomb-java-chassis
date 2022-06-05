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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.event.EventManager;
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
import org.apache.servicecomb.swagger.invocation.context.HttpStatus;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.google.common.eventbus.Subscribe;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestAbstractRestInvocation {
  @Mocked
  HttpServletRequestEx requestEx;

  @Mocked
  HttpServletResponse servletResponse;

  HttpServletResponseEx responseEx;

  @Mocked
  ReferenceConfig endpoint;

  @Mocked
  Map<String, Object> arguments;

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

  static long nanoTime = 123;

  @Before
  public void setup() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new TestPathSchema())
        .run();
    operationMeta = scbEngine.getProducerMicroserviceMeta().operationMetas().get("test.sid1.dynamicId");
    restOperation = RestMetaUtils.getRestOperationMeta(operationMeta);

    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return nanoTime;
      }
    };

    if (responseEx == null) {
      responseEx = new StandardHttpServletResponseEx(servletResponse);
    }

    invocation = new Invocation(endpoint, operationMeta, operationMeta.buildBaseConsumerRuntimeType(), arguments);

    initRestInvocation();
    List<HttpServerFilter> httpServerFilters = SPIServiceUtils.getSortedService(HttpServerFilter.class);
    restInvocation.setHttpServerFilters(httpServerFilters);
  }

  @After
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
  public void setHttpServerFilters(@Mocked List<HttpServerFilter> httpServerFilters) {
    restInvocation.setHttpServerFilters(httpServerFilters);

    Assertions.assertSame(httpServerFilters, restInvocation.httpServerFilters);
  }

  @Test
  public void initProduceProcessorNull() {
    new Expectations() {
      {
        requestEx.getHeader(HttpHeaders.ACCEPT);
        result = "notExistType";
      }
    };
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      public void sendFailResponse(Throwable throwable) {
      }
    };
    initRestInvocation();

    InvocationException exception = Assertions.assertThrows(InvocationException.class,
            () -> restInvocation.initProduceProcessor());
    Assertions.assertEquals("InvocationException: code=406;msg=CommonExceptionData [message=Accept notExistType is not supported]", exception.getMessage());
  }

  @Test
  public void initProduceProcessorNormal() {
    new Expectations() {
      {
        requestEx.getHeader(HttpHeaders.ACCEPT);
        result = MediaType.APPLICATION_JSON;
      }
    };
    // not throw exception
    restInvocation.initProduceProcessor();
  }

  @Test
  public void setContextNull() throws Exception {
    new Expectations() {
      {
        requestEx.getHeader(Const.CSE_CONTEXT);
        result = null;
      }
    };

    Map<String, String> context = invocation.getContext();
    restInvocation.setContext();
    Assertions.assertSame(context, invocation.getContext());
  }

  @Test
  public void setContextEmpty() throws Exception {
    new Expectations() {
      {
        requestEx.getHeader(Const.CSE_CONTEXT);
        result = "";
      }
    };

    Map<String, String> context = invocation.getContext();
    restInvocation.setContext();
    Assertions.assertSame(context, invocation.getContext());
  }

  @Test
  public void setContextNormal() throws Exception {
    Map<String, String> context = new HashMap<>();
    context.put("name", "value");
    new Expectations() {
      {
        requestEx.getHeader(Const.CSE_CONTEXT);
        result = JsonUtils.writeValueAsString(context);
      }
    };

    restInvocation.setContext();
    MatcherAssert.assertThat(invocation.getContext().size(), Matchers.is(1));
    MatcherAssert.assertThat(invocation.getContext(), Matchers.hasEntry("name", "value"));
  }

  @Test
  public void setContextTraceId() throws Exception {
    Map<String, String> context = new HashMap<>();
    new Expectations() {
      {
        requestEx.getHeader(Const.CSE_CONTEXT);
        result = JsonUtils.writeValueAsString(context);
      }
    };
    invocation.addContext("X-B3-traceId", "value1");
    //if request has no traceId, use invocation's traceId
    restInvocation.setContext();
    MatcherAssert.assertThat(invocation.getContext().size(), Matchers.is(1));
    MatcherAssert.assertThat(invocation.getContext(), Matchers.hasEntry("X-B3-traceId", "value1"));

    context.put("X-B3-traceId", "value2");
    new Expectations() {
      {
        requestEx.getHeader(Const.CSE_CONTEXT);
        result = JsonUtils.writeValueAsString(context);
      }
    };
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
  public void invokeFilterHaveResponse(@Mocked HttpServerFilter filter) {
    Response response = Response.ok("");
    new Expectations() {
      {
        filter.enabled();
        result = true;
        filter.afterReceiveRequest(invocation, requestEx);
        result = response;
      }
    };

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
  public void invokeFilterNoResponse(@Mocked HttpServerFilter filter) {
    new Expectations() {
      {
        filter.enabled();
        result = true;
        filter.afterReceiveRequest(invocation, requestEx);
        result = null;
      }
    };

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
  public void invokeFilterNoResponseDisableFilter(@Mocked HttpServerFilter filter) {
    new Expectations() {
      {
        filter.enabled();
        result = false;
      }
    };

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
  public void invokeFilterException(@Mocked HttpServerFilter filter) {
    Exception error = new RuntimeExceptionWithoutStackTrace();
    new Expectations() {
      {
        filter.enabled();
        result = true;
        filter.afterReceiveRequest(invocation, requestEx);
        result = error;
      }
    };

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
  public void invokeNormal(@Mocked HttpServerFilter filter) {
    new Expectations() {
      {
        filter.enabled();
        result = true;
        filter.afterReceiveRequest(invocation, requestEx);
        result = null;
      }
    };

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

    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartServerFiltersRequest());
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
  public void sendResponseQuietlyNormal(@Mocked Response response) {
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
  public void sendResponseQuietlyException(@Mocked Response response) {
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
  public void sendResponseQuietlyExceptionOnNullInvocation(@Mocked Response response) {
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
  public void executeHttpServerFiltersNullInvocation(@Mocked Response response) {
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

  @Test
  public void sendResponseStatusAndContentTypeAndHeader(@Mocked Response response) {
    new Expectations() {
      {
        response.getStatusCode();
        result = 123;
        response.getReasonPhrase();
        result = "reason";
        response.getResult();
        result = "result";
      }
    };

    Map<String, Object> result = new HashMap<>();
    responseEx = new MockUp<HttpServletResponseEx>() {
      private final Map<String, Object> attributes = new HashMap<>();

      @Mock
      public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
      }

      @Mock
      public Object getAttribute(String key) {
        return this.attributes.get(key);
      }

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
    expected.put("contentType", "application/json; charset=utf-8");

    invocation.onStart(0);
    initRestInvocation();

    restInvocation.sendResponse(response);
    Assertions.assertEquals(expected, result);
  }

  @Test
  public void should_ignore_content_length_and_transfer_encoding_when_copy_header_to_http_response(
      @Mocked Response response) {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap()
        .set(CONTENT_LENGTH, "10")
        .set(TRANSFER_ENCODING, "encoding");

    new Expectations() {
      {
        response.getResult();
        result = new RuntimeExceptionWithoutStackTrace("stop");
        response.getHeaders();
        result = headers;
      }
    };

    MultiMap resultHeaders = MultiMap.caseInsensitiveMultiMap();
    responseEx = new MockUp<HttpServletResponseEx>() {
      private final Map<String, Object> attributes = new HashMap<>();

      @Mock
      public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
      }

      @Mock
      public Object getAttribute(String key) {
        return this.attributes.get(key);
      }

      @Mock
      void addHeader(String name, String value) {
        resultHeaders.add(name, value);
      }
    }.getMockInstance();

    invocation.onStart(0);
    initRestInvocation();

    restInvocation.sendResponse(response);
    assertThat(headers).isEmpty();
    assertThat(resultHeaders).isEmpty();
  }

  @Test
  public void testDoSendResponseHeaderNormal(@Mocked Response response) {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("h1", "h1v1");
    headers.add("h1", "h1v2");
    headers.add("h2", "h2v");

    new Expectations() {
      {
        response.getResult();
        result = new RuntimeExceptionWithoutStackTrace("stop");
        response.getHeaders();
        result = headers;
      }
    };

    MultiMap resultHeaders = MultiMap.caseInsensitiveMultiMap();
    responseEx = new MockUp<HttpServletResponseEx>() {
      private final Map<String, Object> attributes = new HashMap<>();

      @Mock
      public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
      }

      @Mock
      public Object getAttribute(String key) {
        return this.attributes.get(key);
      }

      @Mock
      void addHeader(String name, String value) {
        resultHeaders.add(name, value);
      }
    }.getMockInstance();

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
  public void testDoSendResponseResultOK(@Mocked Response response) {
    new Expectations() {
      {
        response.getResult();
        result = "ok";
      }
    };

    Buffer buffer = Buffer.buffer();
    responseEx = new MockUp<HttpServletResponseEx>() {
      private final Map<String, Object> attributes = new HashMap<>();

      @Mock
      public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
      }

      @Mock
      public Object getAttribute(String key) {
        return this.attributes.get(key);
      }

      @Mock
      void setBodyBuffer(Buffer bodyBuffer) {
        buffer.appendBuffer(bodyBuffer);
      }
    }.getMockInstance();

    invocation.onStart(0);
    initRestInvocation();

    restInvocation.sendResponse(response);
    Assertions.assertEquals("\"ok\"", buffer.toString());
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishServerFiltersResponse());
  }

  @Test
  public void testDoSendResponseResultOKFilter(@Mocked Response response) {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.set("Content-Type", "application/json");
    new Expectations() {
      {
        response.getHeaders();
        result = headers;
        response.getStatusCode();
        result = 123;
        response.getReasonPhrase();
        result = "reason";
        response.getResult();
        result = "ok";
      }
    };

    Buffer buffer = Buffer.buffer();
    responseEx = new MockUp<HttpServletResponseEx>() {
      private final Map<String, Object> attributes = new HashMap<>();

      @Mock
      public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
      }

      @Mock
      public Object getAttribute(String key) {
        return this.attributes.get(key);
      }

      @Mock
      void setBodyBuffer(Buffer bodyBuffer) {
        buffer.appendBuffer(bodyBuffer);
      }
    }.getMockInstance();

    HttpServerFilter filter = new HttpServerFilterBaseForTest() {
      @Override
      public void beforeSendResponse(Invocation invocation, HttpServletResponseEx responseEx) {
        buffer.appendString("-filter");
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
  public void findRestOperationServicePathManagerNull(@Mocked MicroserviceMeta microserviceMeta) {
    new Expectations(ServicePathManager.class) {
      {
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = null;
      }
    };

    InvocationException exception = Assertions.assertThrows(InvocationException.class,
            () -> restInvocation.findRestOperation(microserviceMeta));
    Assertions.assertEquals("InvocationException: code=404;msg=CommonExceptionData [message=Not Found]",
            exception.getMessage());
  }

  @Test
  public void findRestOperationNormal(@Mocked MicroserviceMeta microserviceMeta,
      @Mocked ServicePathManager servicePathManager, @Mocked OperationLocator locator) {
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
    new Expectations(ServicePathManager.class) {
      {
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = servicePathManager;
        locator.getPathVarMap();
        result = pathVars;
        locator.getOperation();
        result = restOperation;
      }
    };

    restInvocation.findRestOperation(microserviceMeta);
    Assertions.assertSame(restOperation, restInvocation.restOperationMeta);
    Assertions.assertSame(pathVars, requestEx.getAttribute(RestConst.PATH_PARAMETERS));
  }

  @Test
  public void scheduleInvocationException(@Mocked OperationMeta operationMeta) {
    Executor executor = new ReactiveExecutor();
    requestEx = new AbstractHttpServletRequestForTest();
    requestEx.setAttribute(RestConst.REST_REQUEST, requestEx);
    new Expectations() {
      {
        restOperation.getOperationMeta();
        result = operationMeta;
        operationMeta.getExecutor();
        result = executor;
      }
    };

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
  public void scheduleInvocationTimeout(@Mocked OperationMeta operationMeta) {
    Executor executor = Runnable::run;

    new Expectations() {
      {
        restOperation.getOperationMeta();
        result = operationMeta;
        operationMeta.getExecutor();
        result = executor;
        operationMeta.getMicroserviceQualifiedName();
        result = "sayHi";
      }
    };

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
  public void threadPoolReject(@Mocked OperationMeta operationMeta) {
    RejectedExecutionException rejectedExecutionException = new RejectedExecutionException("reject");
    Executor executor = (task) -> {
      throw rejectedExecutionException;
    };

    new Expectations() {
      {
        restOperation.getOperationMeta();
        result = operationMeta;
        operationMeta.getExecutor();
        result = executor;
      }
    };

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
  public void scheduleInvocationNormal(@Mocked OperationMeta operationMeta) {
    Holder<InvocationStartEvent> eventHolder = new Holder<>();
    Object subscriber = new ScheduleInvocationEventHandler(eventHolder);
    EventManager.register(subscriber);

    Executor executor = new ReactiveExecutor();
    requestEx = new AbstractHttpServletRequestForTest();
    requestEx.setAttribute(RestConst.REST_REQUEST, requestEx);
    new Expectations(requestEx) {
      {
        restOperation.getOperationMeta();
        result = operationMeta;
        operationMeta.getExecutor();
        result = executor;
        requestEx.getHeader(Const.TRACE_ID_NAME);
        result = "tid";
      }
    };

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
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStart());
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartSchedule());
    Assertions.assertSame(invocation, eventHolder.value.getInvocation());
    Assertions.assertEquals("tid", invocation.getTraceId());
  }

  @Test
  public void runOnExecutor() {
    long time = 123;
    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return time;
      }
    };

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
    Assertions.assertEquals(time, invocation.getInvocationStageTrace().getStartExecution());
  }

  @Test
  public void doInvoke(@Mocked Endpoint endpoint, @Mocked OperationMeta operationMeta,
      @Mocked Object[] swaggerArguments, @Mocked SchemaMeta schemaMeta) throws Throwable {
    Response response = Response.ok("ok");
    Handler handler = (invocation, asyncResp) -> asyncResp.complete(response);
    List<Handler> handlerChain = Arrays.asList(handler);
    Deencapsulation.setField(invocation, "handlerList", handlerChain);

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
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartHandlersRequest());
    Assertions.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishHandlersResponse());
  }

  @Test
  public void scheduleInvocation_invocationContextDeserializeError(@Mocked AsyncContext asyncContext) {
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
    new Expectations(operationMeta) {
      {
        operationMeta.getProviderQpsFlowControlHandler();
        result = (Handler) (invocation, asyncResp) -> asyncResp.producerFail(new InvocationException(
            new HttpStatus(429, "Too Many Requests"),
            new CommonExceptionData("rejected by qps flowcontrol")));
      }
    };
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
