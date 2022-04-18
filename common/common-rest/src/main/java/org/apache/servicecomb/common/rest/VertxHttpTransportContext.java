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
package org.apache.servicecomb.common.rest;

import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.context.VertxTransportContext;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class VertxHttpTransportContext extends HttpTransportContext implements VertxTransportContext {
  private final RoutingContext routingContext;

  private final Context vertxContext;

  public VertxHttpTransportContext(RoutingContext routingContext, HttpServletRequestEx requestEx,
      HttpServletResponseEx responseEx, ProduceProcessor produceProcessor) {
    super(requestEx, responseEx, produceProcessor);

    this.routingContext = routingContext;
    this.vertxContext = Vertx.currentContext();
  }

  public RoutingContext getRoutingContext() {
    return routingContext;
  }

  @Override
  public Context getVertxContext() {
    return vertxContext;
  }
}
