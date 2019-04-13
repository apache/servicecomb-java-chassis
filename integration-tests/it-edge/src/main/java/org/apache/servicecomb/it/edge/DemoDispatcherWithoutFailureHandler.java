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

import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.transport.rest.vertx.VertxHttpDispatcher;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class DemoDispatcherWithoutFailureHandler implements VertxHttpDispatcher {
  @Override
  public int getOrder() {
    return 1;
  }

  @Override
  public void init(Router router) {
    router.routeWithRegex("/dispatcherWithoutFailureHandler/.*").handler(this::onRequest);
  }

  private void onRequest(RoutingContext routingContext) {
    InvocationException invocationException = new InvocationException(461, "TestFailureHandlerInSomewhereElse",
        "TestFailureHandlerInSomewhereElse as expected");
    routingContext.fail(invocationException);
  }
}
