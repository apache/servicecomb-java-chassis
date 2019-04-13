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

package org.apache.servicecomb.it.edge;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.transport.rest.vertx.VertxHttpDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class DemoDispatcherWithFailureHandler implements VertxHttpDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(DemoDispatcherWithFailureHandler.class);

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public void init(Router router) {
    router.routeWithRegex("/dispatcherWithFailureHandler/.*").failureHandler(this::onFailure).handler(this::onRequest);
  }

  private void onRequest(RoutingContext routingContext) {
    // we set an InvocationException here but it's not expected to be written into response
    InvocationException throwable = new InvocationException(Status.NOT_FOUND, "TestData");
    routingContext.fail(throwable);
  }

  private void onFailure(RoutingContext routingContext) {
    // if the priority of FailureHandler works as we expected, this FailureHandler takes effect instead of GlobalRestFailureHandler
    if (routingContext.response().closed() || routingContext.response().ended()) {
      LOGGER.error("get an ended response unexpectedly");
    }

    routingContext.response()
        .setStatusCode(460)
        .setStatusMessage("TestFailureHandlerInDispatcher")
        .putHeader("test-header", "test-header-value0")
        .putHeader("Content-Type", "text/plain")
        .end("TestFailureHandlerInDispatcher as expected");
  }
}
