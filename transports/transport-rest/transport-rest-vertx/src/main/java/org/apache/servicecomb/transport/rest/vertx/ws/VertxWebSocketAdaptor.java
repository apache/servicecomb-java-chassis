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

package org.apache.servicecomb.transport.rest.vertx.ws;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.apache.servicecomb.swagger.invocation.ws.AbstractBaseWebSocket;
import org.apache.servicecomb.swagger.invocation.ws.BinaryBytesWebSocketMessage;
import org.apache.servicecomb.swagger.invocation.ws.TextWebSocketMessage;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketAdapter;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketFrame;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketMessage;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocketBase;

/**
 * VertxWebSocketAdaptor
 */
public class VertxWebSocketAdaptor implements WebSocketAdapter {
  private final Executor executor;

  private final AbstractBaseWebSocket delegatedWebSocket;

  private final WebSocketBase vertxWebSocket;

  public VertxWebSocketAdaptor(Executor executor, AbstractBaseWebSocket delegatedWebSocket,
      WebSocketBase vertxWebSocket) {
    Objects.requireNonNull(executor, "VertxWebSocketAdaptor executor is null");
    Objects.requireNonNull(delegatedWebSocket, "VertxWebSocketAdaptor delegatedWebSocket is null");
    Objects.requireNonNull(vertxWebSocket, "VertxWebSocketAdaptor vertxWebSocket is null");
    this.executor = executor;
    this.delegatedWebSocket = delegatedWebSocket;
    this.vertxWebSocket = vertxWebSocket;

    link();
    delegatedWebSocket.setWebSocketAdapter(this);
    startWorking();
  }

  private void link() {
    linkVertxDrainHandler();
    linkVertxTextMessageHandler();
    linkVertxBinaryMessageHandler();
    linkVertxFrameHandler();
    linkVertxExceptionHandler();
    linkVertxCloseHandler();
  }

  private void linkVertxCloseHandler() {
    vertxWebSocket.closeHandler(v ->
        executor.execute(
            () -> delegatedWebSocket.onClose(vertxWebSocket.closeStatusCode(), vertxWebSocket.closeReason())));
  }

  private void linkVertxExceptionHandler() {
    vertxWebSocket.exceptionHandler(t -> executor.execute(() -> delegatedWebSocket.onError(t)));
  }

  private void linkVertxFrameHandler() {
    // not support for now
    // if this feature is added, should notice that the controlling frame may not be forwarded in EdgeService case
  }

  private void linkVertxBinaryMessageHandler() {
    vertxWebSocket.binaryMessageHandler(buffer -> {
      final byte[] bytes = buffer.getBytes();
      executor.execute(
          () -> delegatedWebSocket.onMessage(new BinaryBytesWebSocketMessage(bytes)));
    });
  }

  private void linkVertxTextMessageHandler() {
    vertxWebSocket.textMessageHandler(s ->
        executor.execute(
            () -> delegatedWebSocket.onMessage(new TextWebSocketMessage(s))));
  }

  private void linkVertxDrainHandler() {
    vertxWebSocket.drainHandler(v ->
        executor.execute(delegatedWebSocket::onDrain));
  }

  private void startWorking() {
    executor.execute(
        delegatedWebSocket::onConnectionReady);
  }

  @Override
  public CompletableFuture<Void> sendMessage(WebSocketMessage<?> message) {
    if (message instanceof TextWebSocketMessage) {
      return vertxWebSocket.writeTextMessage(((TextWebSocketMessage) message).getPayload())
          .toCompletionStage()
          .toCompletableFuture();
    }

    if (message instanceof BinaryBytesWebSocketMessage) {
      return vertxWebSocket.writeBinaryMessage(Buffer.buffer(
              ((BinaryBytesWebSocketMessage) message).getPayload()))
          .toCompletionStage()
          .toCompletableFuture();
    }

    throw new IllegalStateException("impossible case, unrecognized WebSocketMessage type!");
  }

  @Override
  public CompletableFuture<Void> sendFrame(WebSocketFrame frame) {
    // not supply this feature yet
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> close(short statusCode, String reason) {
    return vertxWebSocket.close(statusCode, reason)
        .toCompletionStage()
        .toCompletableFuture();
  }

  @Override
  public void pause() {
    vertxWebSocket.pause();
  }

  @Override
  public void resume() {
    vertxWebSocket.resume();
  }

  @Override
  public boolean writeQueueFull() {
    return vertxWebSocket.writeQueueFull();
  }
}
