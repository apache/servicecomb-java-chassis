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

package org.apache.servicecomb.common.rest.filter.inner;

import static org.apache.servicecomb.core.exception.Exceptions.toProducerResponse;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.common.rest.WebSocketTransportContext;
import org.apache.servicecomb.common.rest.codec.produce.ProduceJsonProcessor;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.AbstractFilter;
import org.apache.servicecomb.core.filter.EdgeFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.foundation.vertx.stream.BufferOutputStream;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.TransportContext;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

public class WebSocketServerCodecFilter extends AbstractFilter implements ProviderFilter, EdgeFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServerCodecFilter.class);

  public static final String NAME = "websocket-codec";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int getOrder() {
    // almost time, should be the first filter.
    return Filter.PROVIDER_SCHEDULE_FILTER_ORDER - 2000;
  }

  @Override
  public boolean enabledForTransport(String transport) {
    return CoreConst.WEBSOCKET.equals(transport);
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    return CompletableFuture.completedFuture(invocation)
        .thenCompose(this::decodeRequest)
        .thenCompose(v -> invokeNext(invocation, nextNode))
        .exceptionally(exception -> toProducerResponse(invocation, exception))
        .thenCompose(response -> encodeResponse(invocation, response));
  }

  protected CompletableFuture<Response> invokeNext(Invocation invocation, FilterNode nextNode) {
    if (invocation.isEdge()) {
      TransportContext transportContext = invocation.getTransportContext();
      return nextNode.onFilter(invocation).whenComplete((r, e) -> invocation.setTransportContext(transportContext));
    }
    return nextNode.onFilter(invocation);
  }

  protected CompletableFuture<Void> decodeRequest(Invocation invocation) {
    invocation.getInvocationStageTrace().startProviderDecodeRequest();
    invocation.setSwaggerArguments(new HashMap<>()); // set context parameters and do nothing else.
    invocation.getInvocationStageTrace().finishProviderDecodeRequest();
    return CompletableFuture.completedFuture(null);
  }

  protected CompletableFuture<Response> encodeResponse(Invocation invocation, Response response) {
    invocation.onEncodeResponseStart(response);
    WebSocketTransportContext context = invocation.getTransportContext();

    return encodeResponse(response, context.getServerWebSocket())
        .whenComplete((r, e) -> invocation.onEncodeResponseFinish());
  }

  private static boolean isFailedResponse(Response response) {
    return response.getResult() instanceof InvocationException;
  }

  private static CompletableFuture<Response> writeResponse(
      ServerWebSocket webSocket, Object data, Response response) {
    try (BufferOutputStream output = new BufferOutputStream(Buffer.buffer())) {
      ProduceJsonProcessor produceProcessor = new ProduceJsonProcessor();
      produceProcessor.encodeResponse(output, data);
      CompletableFuture<Response> result = new CompletableFuture<>();
      webSocket.write(output.getBuffer()).onComplete(v ->
          result.complete(response), result::completeExceptionally);

      return result;
    } catch (Throwable e) {
      LOGGER.error("internal service error must be fixed.", e);
      return CompletableFuture.failedFuture(e);
    }
  }

  public static CompletableFuture<Response> encodeResponse(Response response, ServerWebSocket webSocket) {
    if (isFailedResponse(response)) {
      return writeResponse(webSocket, ((InvocationException) response.getResult()).getErrorData(),
          response);
    }
    return CompletableFuture.completedFuture(response);
  }
}
