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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.servicecomb.common.rest.AbstractRestInvocation;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.VertxRestInvocation;
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

import com.netflix.config.DynamicPropertyFactory;

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;

public class VertxRestDispatcher extends AbstractVertxHttpDispatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(VertxRestDispatcher.class);

  private static final String KEY_ENABLED = "servicecomb.http.dispatcher.rest.enabled";

  private Transport transport;

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(KEY_ENABLED, true).get();
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

    // only when unexpected exception happens, it will run into here.
    // the connection should be closed.
    handleFailureAndClose(context, restProducerInvocation, e);
  }

  /**
   * Try to find out the failure information and send it in response.
   */
  private void handleFailureAndClose(RoutingContext context, AbstractRestInvocation restProducerInvocation,
      Throwable e) {
    if (null != restProducerInvocation) {
      // if there is restProducerInvocation, let it send exception in response. The exception is allowed to be null.
      sendFailResponseByInvocation(context, restProducerInvocation, e);
      return;
    }

    if (null != e) {
      // if there exists exception, try to send this exception by RoutingContext
      sendExceptionByRoutingContext(context, e);
      return;
    }

    // if there is no exception, the response is determined by status code.
    sendFailureRespDeterminedByStatus(context);
  }

  /**
   * Try to determine response by status code, and send response.
   */
  private void sendFailureRespDeterminedByStatus(RoutingContext context) {
    Family statusFamily = Family.familyOf(context.statusCode());
    if (Family.CLIENT_ERROR.equals(statusFamily) || Family.SERVER_ERROR.equals(statusFamily) || Family.OTHER
        .equals(statusFamily)) {
      context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.WILDCARD)
          .setStatusCode(context.statusCode()).end();
    } else {
      // it seems the status code is not set properly
      context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.WILDCARD)
          .setStatusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
          .setStatusMessage(Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
          .end(wrapResponseBody(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
    }
    context.response().close();
  }

  /**
   * Use routingContext to send failure information in throwable.
   */
  private void sendExceptionByRoutingContext(RoutingContext context, Throwable e) {
    if (InvocationException.class.isInstance(e)) {
      InvocationException invocationException = (InvocationException) e;
      context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.WILDCARD)
          .setStatusCode(invocationException.getStatusCode()).setStatusMessage(invocationException.getReasonPhrase())
          .end(wrapResponseBody(invocationException.getReasonPhrase()));
    } else {
      context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.WILDCARD)
          .setStatusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).end(wrapResponseBody(e.getMessage()));
    }
    context.response().close();
  }

  /**
   * Consumer will treat the response body as json by default, so it's necessary to wrap response body as Json string
   * to avoid deserialization error.
   *
   * @param message response body
   * @return response body wrapped as Json string
   */
  String wrapResponseBody(String message) {
    if (isValidJson(message)) {
      return message;
    }

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("message", message);

    return jsonObject.toString();
  }

  /**
   * Check if the message is a valid Json string.
   * @param message the message to be checked.
   * @return true if message is a valid Json string, otherwise false.
   */
  private boolean isValidJson(String message) {
    try {
      new JsonObject(message);
    } catch (Exception ignored) {
      return false;
    }
    return true;
  }

  /**
   * Use restProducerInvocation to send failure message. The throwable is allowed to be null.
   */
  private void sendFailResponseByInvocation(RoutingContext context, AbstractRestInvocation restProducerInvocation,
      Throwable e) {
    restProducerInvocation.sendFailResponse(e);
    context.response().close();
  }

  private void onRequest(RoutingContext context) {
    if (transport == null) {
      transport = CseContext.getInstance().getTransportManager().findTransport(Const.RESTFUL);
    }
    HttpServletRequestEx requestEx = new VertxServerRequestToHttpServletRequest(context);
    HttpServletResponseEx responseEx = new VertxServerResponseToHttpServletResponse(context.response());

    VertxRestInvocation vertxRestInvocation = new VertxRestInvocation();
    context.put(RestConst.REST_PRODUCER_INVOCATION, vertxRestInvocation);
    vertxRestInvocation.invoke(transport, requestEx, responseEx, httpServerFilters);
  }
}
