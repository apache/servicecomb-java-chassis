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

import org.apache.servicecomb.common.rest.AbstractRestInvocation;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.RestProducerInvocation;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import org.apache.servicecomb.foundation.vertx.http.VertxServerResponseToHttpServletResponse;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;

public class VertxRestDispatcher extends AbstractVertxHttpDispatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(VertxRestDispatcher.class);

  private Transport transport;

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }

  @Override
  public void init(Router router) {
    router.route().handler(CookieHandler.create());
    router.route().handler(createBodyHandler());
    router.route().failureHandler(this::failureHandler).handler(this::onRequest);
  }

  private void failureHandler(RoutingContext context) {
    LOGGER.error("http server failed.", context.failure());

    AbstractRestInvocation restProducerInvocation = context.get(RestConst.REST_PRODUCER_INVOCATION);
    Throwable e = context.failure();
    if (ErrorDataDecoderException.class.isInstance(e)) {
      Throwable cause = e.getCause();
      if (InvocationException.class.isInstance(cause)) {
        e = cause;
      }
    }
    restProducerInvocation.sendFailResponse(e);

    // 走到这里，应该都是不可控制的异常，直接关闭连接
    context.response().close();
  }

  private void onRequest(RoutingContext context) {
    if (transport == null) {
      transport = CseContext.getInstance().getTransportManager().findTransport(Const.RESTFUL);
    }
    HttpServletRequestEx requestEx = new VertxServerRequestToHttpServletRequest(context);
    HttpServletResponseEx responseEx = new VertxServerResponseToHttpServletResponse(context.response());

    RestProducerInvocation restProducerInvocation = new RestProducerInvocation();
    context.put(RestConst.REST_PRODUCER_INVOCATION, restProducerInvocation);
    restProducerInvocation.invoke(transport, requestEx, responseEx, httpServerFilters);
  }
}
