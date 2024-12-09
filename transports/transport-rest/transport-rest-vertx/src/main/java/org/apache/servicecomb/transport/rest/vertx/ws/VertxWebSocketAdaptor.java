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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.ws.AbstractBaseWebSocket;
import org.apache.servicecomb.swagger.invocation.ws.BinaryBytesWebSocketMessage;
import org.apache.servicecomb.swagger.invocation.ws.SerialExecutorWrapper;
import org.apache.servicecomb.swagger.invocation.ws.TextWebSocketMessage;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketAdapter;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketFrame;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocketBase;

/**
 * VertxWebSocketAdaptor
 */
public class VertxWebSocketAdaptor implements WebSocketAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(VertxWebSocketAdaptor.class);

  private static final int DEFAULT_ADAPTER_QUEUE_MAX_SIZE = 100;

  private static final int DEFAULT_ADAPTER_QUEUE_MAX_CONTINUE_TIMES = 10;

  private final Executor executor;

  private final AbstractBaseWebSocket delegatedWebSocket;

  private final AtomicBoolean inPauseStatus;

  private final WebSocketBase vertxWebSocket;

  private final String websocketSessionId;

  private final InvocationType invocationType;

  public VertxWebSocketAdaptor(
      InvocationType invocationType,
      String websocketSessionId,
      Executor workerPool,
      AbstractBaseWebSocket delegatedWebSocket,
      WebSocketBase vertxWebSocket) {
    Objects.requireNonNull(invocationType, "VertxWebSocketAdaptor invocationType is null");
    Objects.requireNonNull(websocketSessionId, "VertxWebSocketAdaptor websocketSessionId is null");
    Objects.requireNonNull(workerPool, "VertxWebSocketAdaptor workerPool is null");
    Objects.requireNonNull(delegatedWebSocket, "VertxWebSocketAdaptor delegatedWebSocket is null");
    Objects.requireNonNull(vertxWebSocket, "VertxWebSocketAdaptor vertxWebSocket is null");
    this.invocationType = invocationType;
    this.websocketSessionId = websocketSessionId;
    this.executor = workerPool instanceof ReactiveExecutor ?
        workerPool // for reactive case, no need to wrap it into a serial queue model
        : prepareSerialExecutorWrapper(workerPool);
    this.delegatedWebSocket = delegatedWebSocket;
    this.vertxWebSocket = vertxWebSocket;
    inPauseStatus = new AtomicBoolean(true);
    vertxWebSocket.pause(); // make sure the vert.x WebSocket pause status keep consistent with inPauseStatus flag

    prepare();
    delegatedWebSocket.setWebSocketAdapter(this);
    startWorking();
  }

  private SerialExecutorWrapper prepareSerialExecutorWrapper(Executor workerPool) {
    final SerialExecutorWrapper wrapper = new SerialExecutorWrapper(
        invocationType,
        websocketSessionId,
        workerPool,
        DynamicPropertyFactory.getInstance()
            .getIntProperty("servicecomb.websocket.adapter.queue.maxSize",
                DEFAULT_ADAPTER_QUEUE_MAX_SIZE)
            .get(),
        DynamicPropertyFactory.getInstance()
            .getIntProperty("servicecomb.websocket.adapter.queue.maxContinueTimes",
                DEFAULT_ADAPTER_QUEUE_MAX_CONTINUE_TIMES)
            .get());
    wrapper.subscribeQueueDrainEvent(this::resume);
    wrapper.subscribeQueueFullEvent(this::pause);
    return wrapper;
  }

  private void prepare() {
    linkVertxDrainHandler();
    linkVertxTextMessageHandler();
    linkVertxBinaryMessageHandler();
    linkVertxFrameHandler();
    linkVertxExceptionHandler();
    linkVertxCloseHandler();
  }

  private void linkVertxCloseHandler() {
    vertxWebSocket.closeHandler(v ->
        scheduleTask(() -> delegatedWebSocket.onClose(vertxWebSocket.closeStatusCode(), vertxWebSocket.closeReason())));
  }

  private void linkVertxExceptionHandler() {
    vertxWebSocket.exceptionHandler(t -> scheduleTask(() -> delegatedWebSocket.onError(t)));
  }

  private void linkVertxFrameHandler() {
    // not support for now
    // if this feature is added, should notice that the controlling frame may not be forwarded in EdgeService case
  }

  private void linkVertxBinaryMessageHandler() {
    vertxWebSocket.binaryMessageHandler(buffer -> {
      final byte[] bytes = buffer.getBytes();
      scheduleTask(
          () -> delegatedWebSocket.onMessage(new BinaryBytesWebSocketMessage(bytes)));
    });
  }

  private void linkVertxTextMessageHandler() {
    vertxWebSocket.textMessageHandler(s ->
        scheduleTask(
            () -> delegatedWebSocket.onMessage(new TextWebSocketMessage(s))));
  }

  private void linkVertxDrainHandler() {
    vertxWebSocket.drainHandler(v ->
        scheduleTask(delegatedWebSocket::onWriteQueueDrain));
  }

  private void startWorking() {
    scheduleTask(
        delegatedWebSocket::onConnectionReady);
  }

  private void scheduleTask(Runnable task) {
    try {
      executor.execute(task);
    } catch (Throwable e) {
      LOGGER.error("[{}]-[{}] error occurs in scheduleTask", invocationType, websocketSessionId, e);
    }
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
    if (!inPauseStatus.compareAndSet(false, true)) {
      return;
    }
    LOGGER.info("[{}]-[{}] pause websocket", invocationType, websocketSessionId);
    synchronized (this) {
      vertxWebSocket.pause();
      inPauseStatus.set(true);
    }
  }

  @Override
  public void resume() {
    if (!inPauseStatus.compareAndSet(true, false)) {
      return;
    }
    LOGGER.info("[{}]-[{}] resume websocket", invocationType, websocketSessionId);
    synchronized (this) {
      vertxWebSocket.resume();
      inPauseStatus.set(false);
    }
  }

  @Override
  public boolean writeQueueFull() {
    return vertxWebSocket.writeQueueFull();
  }
}
