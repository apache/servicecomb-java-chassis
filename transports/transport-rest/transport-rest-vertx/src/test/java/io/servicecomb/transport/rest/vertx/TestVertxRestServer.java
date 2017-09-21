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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.servicecomb.common.rest.RestConst;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import io.servicecomb.foundation.vertx.http.VertxServerResponseToHttpServletResponse;
import io.servicecomb.swagger.invocation.exception.InvocationException;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestVertxRestServer {

  private VertxRestServer instance = null;

  Throwable throwable;

  @Before
  public void setUp() throws Exception {
    Router mainRouter = Router.router(null);
    mainRouter.route().handler(BodyHandler.create());
    instance = new VertxRestServer(mainRouter) {
      @Override
      public void sendFailResponse(Invocation invocation, HttpServletRequestEx requestEx,
          HttpServletResponseEx responseEx,
          Throwable throwable) {
        TestVertxRestServer.this.throwable = throwable;
      }

      @Override
      protected void handleRequest(HttpServletRequestEx requestEx, HttpServletResponseEx responseEx) {

      }
    };
  }

  @Test
  public void testFailureHandler(@Mocked RoutingContext context, @Mocked HttpServletRequestEx request,
      @Mocked HttpServletResponseEx response) {
    Exception e = new Exception();
    new Expectations() {
      {
        context.get(RestConst.REST_REQUEST);
        result = request;
        context.get(RestConst.REST_RESPONSE);
        result = response;
        context.failure();
        returns(e, e);
      }
    };

    Deencapsulation.invoke(instance, "failureHandler", context);

    Assert.assertSame(e, this.throwable);
  }

  @Test
  public void testFailureHandlerErrorDataWithInvocation(@Mocked RoutingContext context,
      @Mocked HttpServletRequestEx request,
      @Mocked HttpServletResponseEx response, @Mocked InvocationException e) {
    ErrorDataDecoderException edde = new ErrorDataDecoderException(e);
    new Expectations() {
      {
        context.get(RestConst.REST_REQUEST);
        result = request;
        context.get(RestConst.REST_RESPONSE);
        result = response;
        context.failure();
        returns(edde, edde);
      }
    };

    Deencapsulation.invoke(instance, "failureHandler", context);

    Assert.assertSame(e, this.throwable);
  }

  @Test
  public void testFailureHandlerErrorDataWithNormal(@Mocked RoutingContext context,
      @Mocked HttpServletRequestEx request,
      @Mocked HttpServletResponseEx response) {
    Exception e = new Exception();
    ErrorDataDecoderException edde = new ErrorDataDecoderException(e);
    new Expectations() {
      {
        context.get(RestConst.REST_REQUEST);
        result = request;
        context.get(RestConst.REST_RESPONSE);
        result = response;
        context.failure();
        returns(edde, edde);
      }
    };

    Deencapsulation.invoke(instance, "failureHandler", context);

    Assert.assertSame(edde, this.throwable);
  }

  @Test
  public void testOnRequest() {
    Map<String, Object> map = new HashMap<>();
    RoutingContext context = new MockUp<RoutingContext>() {
      @Mock
      RoutingContext put(String key, Object obj) {
        map.put(key, obj);
        return null;
      }
    }.getMockInstance();
    Deencapsulation.invoke(instance, "onRequest", context);

    Assert.assertEquals(VertxServerRequestToHttpServletRequest.class, map.get(RestConst.REST_REQUEST).getClass());
    Assert.assertEquals(VertxServerResponseToHttpServletResponse.class, map.get(RestConst.REST_RESPONSE).getClass());
  }
}
