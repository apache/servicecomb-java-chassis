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

package org.apache.servicecomb.transport.rest.vertx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.RestProducerInvocation;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestVertxRestDispatcher {
  @Mocked
  Router mainRouter;

  @Mocked
  TransportManager transportManager;

  VertxRestDispatcher dispatcher;

  Throwable throwable;

  boolean invoked;

  @Before
  public void setUp() throws Exception {
    dispatcher = new VertxRestDispatcher();
    dispatcher.init(mainRouter);

    new MockUp<RestProducerInvocation>() {
      @Mock
      void sendFailResponse(Throwable throwable) {
        TestVertxRestDispatcher.this.throwable = throwable;
      }

      @Mock
      void invoke(Transport transport, HttpServletRequestEx requestEx, HttpServletResponseEx responseEx,
          List<HttpServerFilter> httpServerFilters) {
        invoked = true;
      }
    };

    CseContext.getInstance().setTransportManager(transportManager);
  }

  @After
  public void teardown() {
    CseContext.getInstance().setTransportManager(null);
  }

  @Test
  public void getOrder() {
    Assert.assertEquals(Integer.MAX_VALUE, dispatcher.getOrder());
  }

  @Test
  public void failureHandlerNormal(@Mocked RoutingContext context) {
    RestProducerInvocation restProducerInvocation = new RestProducerInvocation();

    Exception e = new Exception();
    new Expectations() {
      {
        context.get(RestConst.REST_PRODUCER_INVOCATION);
        result = restProducerInvocation;
        context.failure();
        returns(e, e);
      }
    };

    Deencapsulation.invoke(dispatcher, "failureHandler", context);

    Assert.assertSame(e, this.throwable);
  }

  @Test
  public void failureHandlerErrorDataWithInvocation(@Mocked RoutingContext context, @Mocked InvocationException e) {
    RestProducerInvocation restProducerInvocation = new RestProducerInvocation();

    ErrorDataDecoderException edde = new ErrorDataDecoderException(e);
    new Expectations() {
      {
        context.get(RestConst.REST_PRODUCER_INVOCATION);
        result = restProducerInvocation;
        context.failure();
        returns(edde, edde);
      }
    };

    Deencapsulation.invoke(dispatcher, "failureHandler", context);

    Assert.assertSame(e, this.throwable);
  }

  @Test
  public void failureHandlerErrorDataWithNormal(@Mocked RoutingContext context) {
    RestProducerInvocation restProducerInvocation = new RestProducerInvocation();

    Exception e = new Exception();
    ErrorDataDecoderException edde = new ErrorDataDecoderException(e);
    new Expectations() {
      {
        context.get(RestConst.REST_PRODUCER_INVOCATION);
        result = restProducerInvocation;
        context.failure();
        returns(edde, edde);
      }
    };

    Deencapsulation.invoke(dispatcher, "failureHandler", context);

    Assert.assertSame(edde, this.throwable);
  }

  @Test
  public void onRequest() {
    Map<String, Object> map = new HashMap<>();
    RoutingContext context = new MockUp<RoutingContext>() {
      @Mock
      RoutingContext put(String key, Object obj) {
        map.put(key, obj);
        return null;
      }
    }.getMockInstance();
    Deencapsulation.invoke(dispatcher, "onRequest", context);

    Assert.assertEquals(RestProducerInvocation.class, map.get(RestConst.REST_PRODUCER_INVOCATION).getClass());
    Assert.assertTrue(invoked);
  }
}
