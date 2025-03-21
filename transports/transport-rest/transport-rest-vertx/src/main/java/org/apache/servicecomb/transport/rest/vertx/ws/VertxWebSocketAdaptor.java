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

  /**
   * The WebSocket type provided by user(from business logic)
   */
  private final AbstractBaseWebSocket bizWebSocket;

  /**
   * The underlying WebSocket type provided by Vert.x which represents the real WebSocket network connection.
   */
  private final WebSocketBase vertxWebSocket;

  private final Object pauseLock = new Object();

  private boolean inPauseStatus;

  private final String websocketSessionId;

  private final InvocationType invocationType;

  public VertxWebSocketAdaptor(
      InvocationType invocationType,
      String websocketSessionId,
      Executor workerPool,
      AbstractBaseWebSocket bizWebSocket,
      WebSocketBase vertxWebSocket) {
    Objects.requireNonNull(invocationType, "VertxWebSocketAdaptor invocationType is null");
    Objects.requireNonNull(websocketSessionId, "VertxWebSocketAdaptor websocketSessionId is null");
    Objects.requireNonNull(workerPool, "VertxWebSocketAdaptor workerPool is null");
    Objects.requireNonNull(bizWebSocket, "VertxWebSocketAdaptor bizWebSocket is null");
    Objects.requireNonNull(vertxWebSocket, "VertxWebSocketAdaptor vertxWebSocket is null");
    this.invocationType = invocationType;
    this.websocketSessionId = websocketSessionId;
    this.executor = workerPool instanceof ReactiveExecutor ?
        workerPool // for reactive case, no need to wrap it into a serial queue model
        : prepareSerialExecutorWrapper(workerPool);
    this.bizWebSocket = bizWebSocket;
    this.vertxWebSocket = vertxWebSocket;
    inPauseStatus = true;
    vertxWebSocket.pause(); // make sure the vert.x WebSocket pause status keep consistent with inPauseStatus flag

    // make sure the bi-direction message stream is established
    linkVertxToBiz();
    linkBizToVertx(bizWebSocket);
    // notify that the stream connection is ready to work
    startWorking();
  }

  private void linkBizToVertx(AbstractBaseWebSocket bizWebSocket) {
    bizWebSocket.setWebSocketAdapter(this);
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

  private void linkVertxToBiz() {
    linkVertxDrainHandler();
    linkVertxTextMessageHandler();
    linkVertxBinaryMessageHandler();
    linkVertxFrameHandler();
    linkVertxExceptionHandler();
    linkVertxCloseHandler();
  }

  private void linkVertxCloseHandler() {
    vertxWebSocket.closeHandler(v ->
        scheduleTask(() -> bizWebSocket.onClose(vertxWebSocket.closeStatusCode(), vertxWebSocket.closeReason())));
  }

  private void linkVertxExceptionHandler() {
    vertxWebSocket.exceptionHandler(t -> scheduleTask(() -> bizWebSocket.onError(t)));
  }

  private void linkVertxFrameHandler() {
    // not support for now
    // if this feature is added, should notice that the controlling frame may not be forwarded in EdgeService case
  }

  private void linkVertxBinaryMessageHandler() {
    vertxWebSocket.binaryMessageHandler(buffer -> {
      final byte[] bytes = buffer.getBytes();
      scheduleTask(
          () -> bizWebSocket.onMessage(new BinaryBytesWebSocketMessage(bytes)));
    });
  }

  private void linkVertxTextMessageHandler() {
    vertxWebSocket.textMessageHandler(s ->
        scheduleTask(
            () -> bizWebSocket.onMessage(new TextWebSocketMessage(s))));
  }

  private void linkVertxDrainHandler() {
    vertxWebSocket.drainHandler(v ->
        scheduleTask(bizWebSocket::onWriteQueueDrain));
  }

  private void startWorking() {
    scheduleTask(
        bizWebSocket::startWorking);
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
    synchronized (pauseLock) {
      if (inPauseStatus) {
        return;
      }
      vertxWebSocket.pause();
      inPauseStatus = true;
      LOGGER.info("[{}]-[{}] pause websocket", invocationType, websocketSessionId);
    }
  }

  @Override
  public void resume() {
    synchronized (pauseLock) {
      if (!inPauseStatus) {
        return;
      }
      vertxWebSocket.resume();
      inPauseStatus = false;
      LOGGER.info("[{}]-[{}] resume websocket", invocationType, websocketSessionId);
    }
  }

  @Override
  public boolean writeQueueFull() {
    return vertxWebSocket.writeQueueFull();
  }
}
