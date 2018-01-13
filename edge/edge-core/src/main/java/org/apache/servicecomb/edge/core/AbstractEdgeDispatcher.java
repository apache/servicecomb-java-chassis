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

package org.apache.servicecomb.edge.core;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.transport.rest.vertx.AbstractVertxHttpDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractEdgeDispatcher extends AbstractVertxHttpDispatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEdgeDispatcher.class);

  protected void onFailure(RoutingContext context) {
    LOGGER.error("edge server failed.", context.failure());
    HttpServerResponse response = context.response();
    response.setStatusCode(Status.BAD_GATEWAY.getStatusCode());
    response.setStatusMessage(Status.BAD_GATEWAY.getReasonPhrase());
    response.end();
  }
}
