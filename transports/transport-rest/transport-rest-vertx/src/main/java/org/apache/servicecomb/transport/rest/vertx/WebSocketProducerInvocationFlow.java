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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.invocation.InvocationCreator;
import org.apache.servicecomb.core.invocation.ProducerInvocationFlow;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.ServerWebSocket;

public class WebSocketProducerInvocationFlow extends ProducerInvocationFlow {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketProducerInvocationFlow.class);

  private final ServerWebSocket websocket;

  public WebSocketProducerInvocationFlow(InvocationCreator invocationCreator, ServerWebSocket webSocket) {
    super(invocationCreator);
    this.websocket = webSocket;
  }

  @Override
  protected Invocation sendCreateInvocationException(Throwable throwable) {
    LOGGER.error("Web socket create invocation error.", throwable);
    websocket.writeTextMessage("Web socket create invocation error " + throwable.getMessage());
    websocket.close();
    return null;
  }

  @Override
  protected void endResponse(Invocation invocation, Response response) {

  }
}
