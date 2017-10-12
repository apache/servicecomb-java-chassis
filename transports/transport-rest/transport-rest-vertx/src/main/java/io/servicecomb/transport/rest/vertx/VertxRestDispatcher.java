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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.RestProducerInvocation;
import io.servicecomb.core.Const;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.Transport;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import io.servicecomb.foundation.vertx.http.VertxServerResponseToHttpServletResponse;
import io.servicecomb.swagger.invocation.exception.InvocationException;
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

    RestProducerInvocation restProducerInvocation = context.get(RestConst.REST_PRODUCER_INVOCATION);
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
