/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.common.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.ws.Holder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.servicecomb.common.rest.codec.RestCodec;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.common.rest.locator.OperationLocator;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.Const;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.Endpoint;
import io.servicecomb.core.Handler;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Transport;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.executor.ReactiveExecutor;
import io.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.InvocationException;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRestProducerInvocation {
  @Mocked
  Transport transport;

  @Mocked
  HttpServletRequestEx requestEx;

  @Mocked
  HttpServletResponseEx responseEx;

  @Mocked
  RestOperationMeta restOperationMeta;

  @Mocked
  MicroserviceMetaManager microserviceMetaManager;

  List<HttpServerFilter> httpServerFilters = Collections.emptyList();

  RestProducerInvocation restProducerInvocation;

  Throwable throwableOfSendFailResponse;

  boolean scheduleInvocation;

  boolean runOnExecutor;

  boolean invokeNoParam;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setup() {
    CseContext.getInstance().setMicroserviceMetaManager(microserviceMetaManager);
  }

  private void initRestProducerInvocation() {
    restProducerInvocation.transport = transport;
    restProducerInvocation.requestEx = requestEx;
    restProducerInvocation.responseEx = responseEx;
    restProducerInvocation.restOperationMeta = restOperationMeta;
    restProducerInvocation.httpServerFilters = httpServerFilters;
  }

  @After
  public void teardown() {
    CseContext.getInstance().setMicroserviceMetaManager(null);
  }

  @Test
  public void invokeSendFail(@Mocked InvocationException expected) {
    restProducerInvocation = new MockUp<RestProducerInvocation>() {
      @Mock
      void sendFailResponse(Throwable throwable) {
        throwableOfSendFailResponse = throwable;
      }

      @Mock
      RestOperationMeta findRestOperation() {
        throw expected;
      }

      @Mock
      void scheduleInvocation() {
        throw new IllegalStateException("must not invoke scheduleInvocation");
      }
    }.getMockInstance();

    restProducerInvocation.invoke(transport, requestEx, responseEx, httpServerFilters);

    Assert.assertSame(expected, throwableOfSendFailResponse);
  }

  @Test
  public void invokeNormal() {
    restProducerInvocation = new MockUp<RestProducerInvocation>() {
      @Mock
      RestOperationMeta findRestOperation() {
        return restOperationMeta;
      }

      @Mock
      void scheduleInvocation() {
        scheduleInvocation = true;
      }
    }.getMockInstance();

    requestEx = new AbstractHttpServletRequest() {
    };
    restProducerInvocation.invoke(transport, requestEx, responseEx, httpServerFilters);

    Assert.assertTrue(scheduleInvocation);
    Assert.assertSame(requestEx, requestEx.getAttribute(RestConst.REST_REQUEST));
  }

  @Test
  public void scheduleInvocationNormal(@Mocked OperationMeta operationMeta) {
    Executor executor = cmd -> {
      cmd.run();
    };
    requestEx = new AbstractHttpServletRequest() {
    };
    requestEx.setAttribute(RestConst.REST_REQUEST, requestEx);
    new Expectations() {
      {
        restOperationMeta.getOperationMeta();
        result = operationMeta;
        operationMeta.getExecutor();
        result = executor;
      }
    };

    restProducerInvocation = new MockUp<RestProducerInvocation>() {
      @Mock
      void runOnExecutor() {
        runOnExecutor = true;
      }
    }.getMockInstance();
    initRestProducerInvocation();

    restProducerInvocation.scheduleInvocation();

    Assert.assertTrue(runOnExecutor);
    Assert.assertTrue((boolean) requestEx.getAttribute(RestConst.REST_STATE_EXECUTING));
  }

  @Test
  public void scheduleInvocationTimeout(@Mocked OperationMeta operationMeta) {
    Executor executor = cmd -> {
      cmd.run();
    };

    new Expectations() {
      {
        restOperationMeta.getOperationMeta();
        result = operationMeta;
        operationMeta.getExecutor();
        result = executor;
      }
    };

    requestEx = new AbstractHttpServletRequest() {
    };

    restProducerInvocation = new MockUp<RestProducerInvocation>() {
      @Mock
      void runOnExecutor() {
        runOnExecutor = true;
      }
    }.getMockInstance();
    initRestProducerInvocation();

    restProducerInvocation.scheduleInvocation();

    Assert.assertFalse(runOnExecutor);
    Assert.assertNull(requestEx.getAttribute(RestConst.REST_STATE_EXECUTING));
  }

  @Test
  public void scheduleInvocationException(@Mocked OperationMeta operationMeta) {
    Executor executor = new ReactiveExecutor();
    Throwable e = new Exception("Param error");
    new Expectations(RestCodec.class) {
      {
        restOperationMeta.getOperationMeta();
        result = operationMeta;
        operationMeta.getExecutor();
        result = executor;
        requestEx.getAttribute(RestConst.REST_REQUEST);
        result = requestEx;
        RestCodec.restToArgs(requestEx, restOperationMeta);
        result = e;
      }
    };

    Holder<Throwable> result = new Holder<>();
    restProducerInvocation = new MockUp<RestProducerInvocation>() {
      @Mock
      void sendFailResponse(Throwable throwable) {
        result.value = throwable;
      }
    }.getMockInstance();

    initRestProducerInvocation();
    restProducerInvocation.scheduleInvocation();

    Assert.assertSame(e, result.value);
  }

  @Test
  public void runOnExecutor() {
    Object[] args = new Object[] {};
    new Expectations(RestCodec.class) {
      {
        RestCodec.restToArgs(requestEx, restOperationMeta);
        result = args;
      }
    };
    restProducerInvocation = new MockUp<RestProducerInvocation>() {
      @Mock
      void invoke() {
        invokeNoParam = true;
      }
    }.getMockInstance();
    initRestProducerInvocation();

    restProducerInvocation.runOnExecutor();

    Assert.assertTrue(invokeNoParam);
    Assert.assertSame(args, restProducerInvocation.invocation.getSwaggerArguments());
  }

  @Test
  public void findRestOperationNameFromRegistry() {
    Microservice microservice = new Microservice();
    microservice.setServiceName("ms");

    Exception e = new Exception("stop");
    new Expectations(RegistryUtils.class) {
      {
        requestEx.getHeader(Const.TARGET_MICROSERVICE);
        result = null;
        RegistryUtils.getMicroservice();
        result = microservice;
        microserviceMetaManager.ensureFindValue("ms");
        result = e;
      }
    };
    restProducerInvocation = new RestProducerInvocation();
    initRestProducerInvocation();

    expectedException.expect(Exception.class);
    expectedException.expectMessage("stop");
    restProducerInvocation.findRestOperation();
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
    restProducerInvocation = new RestProducerInvocation();
    initRestProducerInvocation();

    expectedException.expect(InvocationException.class);
    expectedException.expectMessage("CommonExceptionData [message=Not Found]");
    restProducerInvocation.findRestOperation();
  }

  @Test
  public void findRestOperationNormal(@Mocked MicroserviceMeta microserviceMeta,
      @Mocked ServicePathManager servicePathManager, @Mocked OperationLocator locator) {
    requestEx = new AbstractHttpServletRequest() {
      @Override
      public String getRequestURI() {
        return "/path";
      }

      @Override
      public String getMethod() {
        return "GET";
      }

      @Override
      public String getHeader(String name) {
        return "ms";
      }
    };
    Map<String, String> pathVars = new HashMap<>();
    new Expectations(ServicePathManager.class) {
      {
        microserviceMetaManager.ensureFindValue("ms");
        result = microserviceMeta;
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = servicePathManager;
        servicePathManager.producerLocateOperation(anyString, anyString);
        result = locator;
        locator.getPathVarMap();
        result = pathVars;
        locator.getOperation();
        result = restOperationMeta;
      }
    };
    restProducerInvocation = new RestProducerInvocation();
    initRestProducerInvocation();

    Assert.assertSame(restOperationMeta, restProducerInvocation.findRestOperation());
    Assert.assertSame(pathVars, requestEx.getAttribute(RestConst.PATH_PARAMETERS));
  }

  @Test
  public void doInvoke(@Mocked Endpoint endpoint, @Mocked OperationMeta operationMeta,
      @Mocked Object[] swaggerArguments, @Mocked SchemaMeta schemaMeta) throws Throwable {
    Response response = Response.ok("ok");
    Handler handler = new MockUp<Handler>() {
      @Mock
      void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
        asyncResp.complete(response);
      }
    }.getMockInstance();
    List<Handler> handlerChain = Arrays.asList(handler);

    new Expectations() {
      {
        operationMeta.getSchemaMeta();
        result = schemaMeta;
        schemaMeta.getProviderHandlerChain();
        result = handlerChain;
      }
    };

    Invocation invocation = new Invocation(endpoint, operationMeta, swaggerArguments);

    Holder<Response> result = new Holder<>();
    restProducerInvocation = new RestProducerInvocation() {
      protected void sendResponseQuietly(Response response) {
        result.value = response;
      }
    };
    initRestProducerInvocation();
    restProducerInvocation.invocation = invocation;

    restProducerInvocation.doInvoke();

    Assert.assertSame(response, result.value);
  }
}
