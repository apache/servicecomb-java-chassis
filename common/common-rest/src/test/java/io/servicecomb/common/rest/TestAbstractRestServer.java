/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.common.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
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
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.InvocationType;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.InvocationException;
import io.vertx.core.http.HttpServerResponse;
import mockit.Expectations;
import mockit.Mocked;

public class TestAbstractRestServer {
  private class RestServerForTest extends AbstractRestServer<HttpServerResponse> {
    @Override
    protected void doSendResponse(Invocation invocation, HttpServerResponse httpServerResponse,
        ProduceProcessor produceProcessor,
        Response response) throws Exception {
      invocationResponse = response;
    }
  };

  @Mocked
  Endpoint endpoint;

  @Mocked
  SchemaMeta schemaMeta;

  @Mocked
  OperationMeta operationMeta;

  @Mocked
  Object[] swaggerArguments;

  @Mocked
  HttpServletRequest request;

  @Mocked
  Transport transport;

  @Mocked
  HttpServerResponse httpResponse;

  @Mocked
  MicroserviceMetaManager microserviceMetaManager;

  @Mocked
  MicroserviceMeta microserviceMeta;

  @Mocked
  ServicePathManager servicePathManager;

  Invocation invocation;

  RestServerForTest restServer;

  Response invocationResponse;

  @Before
  public void before() throws Exception {
    new Expectations() {
      {
        operationMeta.getSchemaMeta();
        result = schemaMeta;
      }
    };
    invocation = new Invocation(endpoint, operationMeta, swaggerArguments);

    restServer = new RestServerForTest();
    restServer.setTransport(transport);

    CseContext.getInstance().setMicroserviceMetaManager(microserviceMetaManager);
  }

  @After
  public void teardown() {
    CseContext.getInstance().setMicroserviceMetaManager(null);
  }

  @Test
  public void testConstruct(@Mocked HttpServerFilter filter) {
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getSortedService(HttpServerFilter.class);
        result = Arrays.asList(filter);
      }
    };
    restServer = new RestServerForTest();

    Assert.assertThat(restServer.httpServerFilters, Matchers.contains(filter));
  }

  @Test
  public void testSetContextEmpty() throws Exception {
    new Expectations() {
      {
        request.getHeader(Const.CSE_CONTEXT);
        result = "";
      }
    };

    Map<String, String> context = invocation.getContext();
    restServer.setContext(invocation, request);
    Assert.assertSame(context, invocation.getContext());
  }

  @Test
  public void testSetContextNormal() throws Exception {
    Map<String, String> context = new HashMap<>();
    context.put("name", "value");
    new Expectations() {
      {
        request.getHeader(Const.CSE_CONTEXT);
        result = JsonUtils.writeValueAsString(context);
      }
    };

    restServer.setContext(invocation, request);
    Assert.assertThat(invocation.getContext().size(), Matchers.is(1));
    Assert.assertThat(invocation.getContext(), Matchers.hasEntry("name", "value"));
  }

  @Test
  public void testHandleRequestException() {
    new Expectations() {
      {
        request.getHeader(Const.TARGET_MICROSERVICE);
        result = new InvocationException(Status.BAD_REQUEST, (Object) "handle request failed");
      }
    };

    restServer.handleRequest(request, httpResponse);
    InvocationException exception = invocationResponse.getResult();
    Assert.assertEquals("handle request failed", exception.getErrorData());
  }

  // useless, but for coverage......
  @Test
  public void testHandleRequestNormal(@Mocked RestOperationMeta restOperation, @Mocked OperationMeta operationMeta) {
    Executor executor = new Executor() {
      @Override
      public void execute(Runnable command) {
        command.run();
      }
    };
    new Expectations() {
      {
        request.getHeader("Accept");
        result = "ms";
        restOperation.getOperationMeta();
        result = operationMeta;
        operationMeta.getExecutor();
        result = executor;
        schemaMeta.getProviderHandlerChain();
        result = new Handler() {
          @Override
          public void init(MicroserviceMeta microserviceMeta, InvocationType invocationType) {

          }

          @Override
          public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
            asyncResp.complete(Response.ok("ok"));
          }
        };
      }
    };

    restServer = new RestServerForTest() {
      @Override
      protected RestOperationMeta findRestOperation(HttpServletRequest request) {
        return restOperation;
      }
    };
    restServer.setTransport(transport);

    restServer.handleRequest(request, httpResponse);
    Assert.assertEquals("ok", invocationResponse.getResult());
  }

  @Test
  public void testFindRestOperation(@Mocked OperationLocator locator, @Mocked RestOperationMeta restOperation) {
    new Expectations(ServicePathManager.class) {
      {
        request.getHeader(Const.TARGET_MICROSERVICE);
        result = "ms";
        microserviceMetaManager.ensureFindValue("ms");
        result = microserviceMeta;
        ServicePathManager.getServicePathManager(microserviceMeta);
        result = servicePathManager;
        servicePathManager.producerLocateOperation(anyString, anyString);
        result = locator;
        locator.getOperation();
        result = restOperation;
      }
    };


    Assert.assertSame(restOperation, restServer.findRestOperation(request));
  }
}
