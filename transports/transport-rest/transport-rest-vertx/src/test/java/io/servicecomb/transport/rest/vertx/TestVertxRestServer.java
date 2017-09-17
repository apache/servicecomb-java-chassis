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

package io.servicecomb.transport.rest.vertx;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.core.Invocation;
import io.servicecomb.swagger.invocation.Response;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import mockit.Deencapsulation;

public class TestVertxRestServer {

  private VertxRestServer instance = null;

  @Before
  public void setUp() throws Exception {
    Router mainRouter = Router.router(null);
    mainRouter.route().handler(BodyHandler.create());
    instance = new VertxRestServer(mainRouter) {
      @Override
      protected RestOperationMeta findRestOperation(HttpServletRequest request) {
        return super.findRestOperation(request);
      }

      @Override
      public void sendFailResponse(Invocation invocation, HttpServletRequest request,
          HttpServerResponse httpResponse,
          Throwable throwable) {
      }
    };
  }

  @After
  public void tearDown() throws Exception {
    instance = null;
  }

  @Test
  public void testSetHttpRequestContext() {
    boolean status = false;
    try {
      Invocation invocation = Mockito.mock(Invocation.class);
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      instance.setHttpRequestContext(invocation, request);
      Assert.assertNotNull(instance);
    } catch (Exception e) {
      status = true;
    }
    Assert.assertFalse(status);
  }

  @Test
  public void testVertxRestServer() {
    Assert.assertNotNull(instance);
  }

  @Test
  public void testDoSend() {
    boolean status = false;
    try {
      HttpServerResponse httpServerResponse = Mockito.mock(HttpServerResponse.class);
      ProduceProcessor produceProcessor = Mockito.mock(ProduceProcessor.class);
      Response response = Response.create(0, "reasonPhrase", new Object());
      instance.doSendResponse(null, httpServerResponse, produceProcessor, response);
    } catch (Exception e) {
      status = true;
    }
    Assert.assertFalse(status);
  }

  @Test
  public void testFailureHandler() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    Mockito.when(context.response()).thenReturn(Mockito.mock(HttpServerResponse.class));
    boolean status = false;
    try {
      Deencapsulation.invoke(instance, "failureHandler", context);
    } catch (Exception e) {
      status = true;
    }
    Assert.assertFalse(status);
  }
}
