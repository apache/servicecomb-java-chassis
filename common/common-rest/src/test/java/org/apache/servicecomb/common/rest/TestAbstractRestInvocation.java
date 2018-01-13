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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.ws.rs.core.Response.Status;
import javax.xml.ws.Holder;

import org.apache.servicecomb.common.rest.codec.RestCodec;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.MicroserviceMetaManager;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.apache.servicecomb.core.metrics.InvocationStartedEvent;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.metrics.performance.QueueMetrics;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.swagger.invocation.response.Headers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
  HttpServletResponseEx responseEx;

  @Mocked
  ReferenceConfig endpoint;

  @Mocked
  MicroserviceMetaManager microserviceMetaManager;

  @Mocked
  SchemaMeta schemaMeta;

  @Mocked
  OperationMeta operationMeta;

  @Mocked
  RestOperationMeta restOperation;

  @Mocked
  Object[] swaggerArguments;

  Invocation invocation;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  class AbstractRestInvocationForTest extends AbstractRestInvocation {
    @Override
    protected OperationLocator locateOperation(ServicePathManager servicePathManager) {
      return null;
    }

    @Override
    protected void createInvocation(Object[] args) {
      this.invocation = TestAbstractRestInvocation.this.invocation;
    }
  }

  AbstractRestInvocation restInvocation = new AbstractRestInvocationForTest();

  @Before
  public void setup() {
    invocation = new Invocation(endpoint, operationMeta, swaggerArguments);

    initRestInvocation();
  }

  private void initRestInvocation() {
    restInvocation.produceProcessor = ProduceProcessorManager.JSON_PROCESSOR;
    restInvocation.requestEx = requestEx;
    restInvocation.responseEx = responseEx;
    restInvocation.invocation = invocation;
    restInvocation.restOperationMeta = restOperation;
  }

  @Test
  public void setHttpServerFilters(@Mocked List<HttpServerFilter> httpServerFilters) {
    restInvocation.setHttpServerFilters(httpServerFilters);

    Assert.assertSame(httpServerFilters, restInvocation.httpServerFilters);
  }

  @Test
  public void initProduceProcessorNull() {
    new Expectations() {
      {
        restOperation.ensureFindProduceProcessor(requestEx);
        result = null;
      }
    };

    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      public void sendFailResponse(Throwable throwable) {
      }
    };
    initRestInvocation();

    try {
      restInvocation.initProduceProcessor();
      Assert.fail("must throw exception");
    } catch (InvocationException e) {
      Assert.assertEquals(Status.NOT_ACCEPTABLE, e.getStatus());
      Assert.assertEquals("Accept null is not supported", ((CommonExceptionData) e.getErrorData()).getMessage());
    }
  }

  @Test
  public void initProduceProcessorNormal() {
    new Expectations() {
      {
        restOperation.ensureFindProduceProcessor(requestEx);
        result = ProduceProcessorManager.JSON_PROCESSOR;
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
    Assert.assertSame(context, invocation.getContext());
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
    Assert.assertSame(context, invocation.getContext());
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
    Assert.assertThat(invocation.getContext().size(), Matchers.is(1));
    Assert.assertThat(invocation.getContext(), Matchers.hasEntry("name", "value"));
  }

  @Test
  public void invokeFilterHaveResponse(@Mocked HttpServerFilter filter) {
    Response response = Response.ok("");
    new Expectations() {
      {
        filter.afterReceiveRequest(invocation, requestEx);
        result = response;
      }
    };

    Holder<Response> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() throws Throwable {
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

    Assert.assertSame(response, result.value);
  }

  @Test
  public void invokeFilterNoResponse(@Mocked HttpServerFilter filter) {
    new Expectations() {
      {
        filter.afterReceiveRequest(invocation, requestEx);
        result = null;
      }
    };

    Holder<Boolean> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() throws Throwable {
        result.value = true;
      }
    };
    initRestInvocation();
    restInvocation.httpServerFilters = Arrays.asList(filter);

    restInvocation.invoke();

    Assert.assertTrue(result.value);
  }

  @Test
  public void invokeFilterException(@Mocked HttpServerFilter filter) {
    Error error = new Error();
    new Expectations() {
      {
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
      protected void doInvoke() throws Throwable {

      }
    };
    initRestInvocation();
    restInvocation.httpServerFilters = Arrays.asList(filter);

    restInvocation.invoke();

    Assert.assertSame(error, result.value);
  }

  @Test
  public void invokeNormal(@Mocked HttpServerFilter filter) {
    new Expectations() {
      {
        filter.afterReceiveRequest(invocation, requestEx);
        result = null;
      }
    };

    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() throws Throwable {
      }

      @Override
      public void sendFailResponse(Throwable throwable) {
        Assert.fail("must not fail");
      }
    };
    initRestInvocation();
    restInvocation.httpServerFilters = Arrays.asList(filter);

    restInvocation.invoke();
  }

  @Test
  public void sendFailResponseNoProduceProcessor() {
    restInvocation.produceProcessor = null;
    restInvocation.sendFailResponse(new Error());

    Assert.assertSame(ProduceProcessorManager.JSON_PROCESSOR, restInvocation.produceProcessor);
  }

  @Test
  public void sendFailResponseHaveProduceProcessor() {
    Holder<Response> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() throws Throwable {
      }

      @Override
      protected void sendResponseQuietly(Response response) {
        result.value = response;
      }
    };
    initRestInvocation();
    restInvocation.produceProcessor = ProduceProcessorManager.PLAIN_PROCESSOR;

    Throwable e = new InvocationException(Status.BAD_GATEWAY, "");
    restInvocation.sendFailResponse(e);
    Assert.assertSame(e, result.value.getResult());
    Assert.assertSame(ProduceProcessorManager.PLAIN_PROCESSOR, restInvocation.produceProcessor);
  }

  @Test
  public void sendResponseQuietlyNormal(@Mocked Response response) {
    Holder<Response> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() throws Throwable {
      }

      @Override
      protected void sendResponse(Response response) throws Exception {
        result.value = response;
      }
    };
    initRestInvocation();

    restInvocation.sendResponseQuietly(response);

    Assert.assertSame(response, result.value);
  }

  @Test
  public void sendResponseQuietlyException(@Mocked Response response) {
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void doInvoke() throws Throwable {
      }

      @Override
      protected void sendResponse(Response response) throws Exception {
        throw new Error("");
      }
    };
    initRestInvocation();

    restInvocation.sendResponseQuietly(response);

    // just log, check nothing
  }

  @Test
  public void sendResponseStatusAndContentTypeAndHeader(@Mocked Response response) throws Exception {
    new Expectations() {
      {
        response.getStatusCode();
        result = 123;
        response.getReasonPhrase();
        result = "reason";
        response.getResult();
        result = new Error("stop");
      }
    };

    Map<String, Object> result = new HashMap<>();
    responseEx = new MockUp<HttpServletResponseEx>() {
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

    initRestInvocation();

    try {
      restInvocation.sendResponse(response);
      Assert.fail("must throw exception");
    } catch (Error e) {
      Assert.assertEquals(expected, result);
    }
  }

  @Test
  public void testDoSendResponseHeaderNull(@Mocked Response response) throws Exception {
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
    responseEx = new MockUp<HttpServletResponseEx>() {
      @Mock
      void addHeader(String name, String value) {
        resultHeaders.addHeader(name, value);
      }
    }.getMockInstance();

    initRestInvocation();

    try {
      restInvocation.sendResponse(response);
      Assert.fail("must throw exception");
    } catch (Error e) {
      Assert.assertEquals(null, resultHeaders.getHeaderMap());
    }
  }

  @Test
  public void testDoSendResponseHeaderNormal(@Mocked Response response) throws Exception {
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
    responseEx = new MockUp<HttpServletResponseEx>() {
      @Mock
      void addHeader(String name, String value) {
        resultHeaders.addHeader(name, value);
      }
    }.getMockInstance();
    initRestInvocation();

    try {
      restInvocation.sendResponse(response);
      Assert.fail("must throw exception");
    } catch (Error e) {
      Assert.assertEquals(headers.getHeaderMap(), resultHeaders.getHeaderMap());
    }
  }

  @Test
  public void testDoSendResponseResultOK(@Mocked Response response) throws Exception {
    new Expectations() {
      {
        response.getResult();
        result = "ok";
      }
    };

    Buffer buffer = Buffer.buffer();
    responseEx = new MockUp<HttpServletResponseEx>() {
      @Mock
      void setBodyBuffer(Buffer bodyBuffer) {
        buffer.appendBuffer(bodyBuffer);
      }
    }.getMockInstance();
    initRestInvocation();

    restInvocation.sendResponse(response);
    Assert.assertEquals("\"ok\"", buffer.toString());
  }

  @Test
  public void testDoSendResponseResultOKFilter(@Mocked Response response)
      throws Exception {
    new Expectations() {
      {
        response.getResult();
        result = "ok";
      }
    };

    Buffer buffer = Buffer.buffer();
    responseEx = new MockUp<HttpServletResponseEx>() {
      @Mock
      void setBodyBuffer(Buffer bodyBuffer) {
        buffer.appendBuffer(bodyBuffer);
      }
    }.getMockInstance();

    HttpServerFilter filter = new MockUp<HttpServerFilter>() {
      @Mock
      void beforeSendResponse(Invocation invocation, HttpServletResponseEx responseEx) {
        buffer.appendString("-filter");
      }
    }.getMockInstance();

    initRestInvocation();
    restInvocation.setHttpServerFilters(Arrays.asList(filter));

    restInvocation.sendResponse(response);
    Assert.assertEquals("\"ok\"-filter", buffer.toString());
  }

  @Test
  public void findRestOperationServicePathManagerNull(@Mocked MicroserviceMeta microserviceMeta) {
    new Expectations(ServicePathManager.class) {
      {
        requestEx.getHeader(Const.TARGET_MICROSERVICE);
        result = "ms";
        microserviceMetaManager.ensureFindValue("ms");
        result = microserviceMeta;
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = null;
      }
    };

    expectedException.expect(InvocationException.class);
    expectedException.expectMessage("CommonExceptionData [message=Not Found]");
    restInvocation.findRestOperation(microserviceMeta);
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
    Assert.assertSame(restOperation, restInvocation.restOperationMeta);
    Assert.assertSame(pathVars, requestEx.getAttribute(RestConst.PATH_PARAMETERS));
  }

  @Test
  public void scheduleInvocationException(@Mocked OperationMeta operationMeta) {
    Executor executor = new ReactiveExecutor();
    requestEx = new AbstractHttpServletRequest() {
    };
    requestEx.setAttribute(RestConst.REST_REQUEST, requestEx);
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

    Holder<Throwable> result = new Holder<>();
    Error error = new Error("run on executor");
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void runOnExecutor(QueueMetrics metricsData, InvocationStartedEvent startedEvent) {
        throw error;
      }

      @Override
      public void sendFailResponse(Throwable throwable) {
        result.value = throwable;
      }
    };
    restInvocation.requestEx = requestEx;
    restInvocation.restOperationMeta = restOperation;

    restInvocation.scheduleInvocation();

    Assert.assertSame(error, result.value);
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

    requestEx = new AbstractHttpServletRequest() {
    };

    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void runOnExecutor(QueueMetrics metricsData, InvocationStartedEvent startedEvent) {
        throw new Error("run on executor");
      }

      @Override
      public void sendFailResponse(Throwable throwable) {
        throw (Error) throwable;
      }
    };
    restInvocation.requestEx = requestEx;
    restInvocation.restOperationMeta = restOperation;

    // will not throw exception
    restInvocation.scheduleInvocation();
  }

  @Test
  public void scheduleInvocationNormal(@Mocked OperationMeta operationMeta) {
    Executor executor = new ReactiveExecutor();
    requestEx = new AbstractHttpServletRequest() {
    };
    requestEx.setAttribute(RestConst.REST_REQUEST, requestEx);
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

    Holder<Boolean> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void runOnExecutor(QueueMetrics metricsData, InvocationStartedEvent startedEvent) {
        result.value = true;
      }
    };
    restInvocation.requestEx = requestEx;
    restInvocation.restOperationMeta = restOperation;

    restInvocation.scheduleInvocation();

    Assert.assertTrue(result.value);
  }

  @Test
  public void runOnExecutor() {
    new Expectations(RestCodec.class) {
      {
        RestCodec.restToArgs(requestEx, restOperation);
        result = null;
      }
    };

    Holder<Boolean> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      public void invoke() {
        result.value = true;
      }
    };
    restInvocation.requestEx = requestEx;
    restInvocation.restOperationMeta = restOperation;

    restInvocation.runOnExecutor(null, new InvocationStartedEvent("", InvocationType.PRODUCER, System.nanoTime()));
    Assert.assertTrue(result.value);
    Assert.assertSame(invocation, restInvocation.invocation);
  }

  @Test
  public void doInvoke(@Mocked Endpoint endpoint, @Mocked OperationMeta operationMeta,
      @Mocked Object[] swaggerArguments, @Mocked SchemaMeta schemaMeta) throws Throwable {
    Response response = Response.ok("ok");
    Handler handler = new Handler() {
      @Override
      public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
        asyncResp.complete(response);
      }
    };
    List<Handler> handlerChain = Arrays.asList(handler);
    Deencapsulation.setField(invocation, "handlerList", handlerChain);

    Holder<Response> result = new Holder<>();
    restInvocation = new AbstractRestInvocationForTest() {
      @Override
      protected void sendResponseQuietly(Response response) {
        result.value = response;
      }
    };
    restInvocation.invocation = invocation;

    restInvocation.doInvoke();

    Assert.assertSame(response, result.value);
  }
}
