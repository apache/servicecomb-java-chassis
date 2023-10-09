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

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;

import io.vertx.ext.web.RoutingContext;

public class RestVertxProducerInvocationCreator extends RestProducerInvocationCreator {
  private final RoutingContext routingContext;

  public RestVertxProducerInvocationCreator(RoutingContext routingContext,
      MicroserviceMeta microserviceMeta, Endpoint endpoint,
      HttpServletRequestEx requestEx, HttpServletResponseEx responseEx) {
    super(microserviceMeta, endpoint, requestEx, responseEx);
    this.routingContext = routingContext;
  }

  @Override
  protected void initTransportContext(Invocation invocation) {
    VertxHttpTransportContext transportContext = new VertxHttpTransportContext(routingContext, requestEx, responseEx);
    invocation.setTransportContext(transportContext);
    routingContext.put(RestConst.REST_INVOCATION_CONTEXT, invocation);
  }
}
