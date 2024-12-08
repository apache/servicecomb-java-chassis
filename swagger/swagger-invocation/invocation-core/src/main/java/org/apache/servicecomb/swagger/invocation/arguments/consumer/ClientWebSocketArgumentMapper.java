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

package org.apache.servicecomb.swagger.invocation.arguments.consumer;

import java.util.Map;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.ws.ClientWebSocket;

/**
 * Mapping {@link org.apache.servicecomb.swagger.invocation.ws.ClientWebSocket} as RPC parameter.
 */
public class ClientWebSocketArgumentMapper extends ConsumerArgumentMapper {
  public static final String SCB_CLIENT_WEBSOCKET_LOCAL_CONTEXT_KEY = "scb-client-websocket";

  private final String invocationArgumentName;

  public ClientWebSocketArgumentMapper(String invocationArgumentName) {
    this.invocationArgumentName = invocationArgumentName;
  }

  @Override
  public void invocationArgumentToSwaggerArguments(SwaggerInvocation swaggerInvocation,
      Map<String, Object> swaggerArguments, Map<String, Object> invocationArguments) {
    final ClientWebSocket clientWebSocket = (ClientWebSocket) invocationArguments.get(invocationArgumentName);
    swaggerInvocation.addLocalContext(SCB_CLIENT_WEBSOCKET_LOCAL_CONTEXT_KEY, clientWebSocket);
  }
}
