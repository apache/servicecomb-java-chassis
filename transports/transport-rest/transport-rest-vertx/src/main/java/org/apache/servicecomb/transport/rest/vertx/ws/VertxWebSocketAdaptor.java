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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.WebSocketActionEvent;
import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.apache.servicecomb.core.tracing.BraveTraceIdGenerator;
import org.apache.servicecomb.core.tracing.TraceIdGenerator;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.ws.AbstractBaseWebSocket;
import org.apache.servicecomb.swagger.invocation.ws.BinaryBytesWebSocketMessage;
import org.apache.servicecomb.swagger.invocation.ws.SerialExecutorWrapper;
import org.apache.servicecomb.swagger.invocation.ws.TextWebSocketMessage;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketActionType;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketAdapter;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketFrame;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocketBase;

/**
 * VertxWebSocketAdaptor
 */
public class VertxWebSocketAdaptor implements WebSocketAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(VertxWebSocketAdaptor.class);

  private static final TraceIdGenerator CONNECTION_ID_GENERATOR = new BraveTraceIdGenerator();

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

  private final Invocation invocation;

  private final long creationTimestamp;

  private final EventBus eventBus;

  private boolean inPauseStatus;

  private final String connectionId;

  private final InvocationType invocationType;

  public VertxWebSocketAdaptor(
      Invocation invocation,
      InvocationType invocationType, Executor workerPool,
      AbstractBaseWebSocket bizWebSocket,
      WebSocketBase vertxWebSocket) {
    Objects.requireNonNull(invocation, "VertxWebSocketAdaptor invocation is null");
    Objects.requireNonNull(workerPool, "VertxWebSocketAdaptor workerPool is null");
    Objects.requireNonNull(bizWebSocket, "VertxWebSocketAdaptor bizWebSocket is null");
    Objects.requireNonNull(vertxWebSocket, "VertxWebSocketAdaptor vertxWebSocket is null");
    creationTimestamp = System.currentTimeMillis();
    this.invocation = invocation;
    this.invocationType = invocationType;
    this.connectionId = CONNECTION_ID_GENERATOR.generate();
    this.executor = workerPool instanceof ReactiveExecutor ?
        workerPool // for reactive case, no need to wrap it into a serial queue model
        : prepareSerialExecutorWrapper(workerPool);
    this.bizWebSocket = bizWebSocket;
    this.vertxWebSocket = vertxWebSocket;
    eventBus = EventManager.getEventBus();
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
        connectionId,
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
        scheduleTask(createWebSocketActionEvent(WebSocketActionType.ON_CLOSE),
            () -> bizWebSocket.onClose(vertxWebSocket.closeStatusCode(), vertxWebSocket.closeReason())));
  }

  private void linkVertxExceptionHandler() {
    vertxWebSocket.exceptionHandler(t -> scheduleTask(
        createWebSocketActionEvent(WebSocketActionType.ON_ERROR),
        () -> bizWebSocket.onError(t)));
  }

  private void linkVertxFrameHandler() {
    // not support for now
    // if this feature is added, should notice that the controlling frame may not be forwarded in EdgeService case
  }

  private void linkVertxBinaryMessageHandler() {
    vertxWebSocket.binaryMessageHandler(buffer -> {
      final byte[] bytes = buffer.getBytes();
      scheduleTask(createWebSocketActionEvent(WebSocketActionType.ON_MESSAGE_BINARY)
              .setDataSize(buffer.length()),
          () -> bizWebSocket.onMessage(new BinaryBytesWebSocketMessage(bytes)));
    });
  }

  private void linkVertxTextMessageHandler() {
    vertxWebSocket.textMessageHandler(s ->
        scheduleTask(createWebSocketActionEvent(WebSocketActionType.ON_MESSAGE_TEXT)
                .setDataSize(s.length()),
            () -> bizWebSocket.onMessage(new TextWebSocketMessage(s))));
  }

  private void linkVertxDrainHandler() {
    vertxWebSocket.drainHandler(v ->
        scheduleTask(createWebSocketActionEvent(WebSocketActionType.ON_SEND_QUEUE_DRAIN),
            bizWebSocket::onWriteQueueDrain));
  }

  private void startWorking() {
    WebSocketActionEvent event = createWebSocketActionEventInSyncMode(WebSocketActionType.CONNECTION_PREPARE);
    scheduleTask(
        createWebSocketActionEvent(WebSocketActionType.ON_OPEN),
        bizWebSocket::startWorking);
    eventBus.post(event.setActionEndTimestamp(System.currentTimeMillis()));
  }

  private WebSocketActionEvent createWebSocketActionEvent(WebSocketActionType actionType) {
    return new WebSocketActionEvent()
        .setActionType(actionType)
        .setConnectionStartTimestamp(creationTimestamp)
        .setInvocationType(invocationType)
        .setHandleThreadName(Thread.currentThread().getName())
        .setOperationMeta(invocation.getOperationMeta())
        .setConnectionId(connectionId)
        .setTraceId(invocation.getTraceId())
        .setScheduleStartTimestamp(System.currentTimeMillis());
  }

  private WebSocketActionEvent createWebSocketActionEventInSyncMode(WebSocketActionType actionType) {
    return createWebSocketActionEvent(actionType)
        .setActionStartTimestamp(System.currentTimeMillis())
        .setHandleThreadName(Thread.currentThread().getName());
  }

  private void scheduleTask(WebSocketActionEvent event, Runnable task) {
    try {
      executor.execute(() -> {
        event.setActionStartTimestamp(System.currentTimeMillis())
            .setHandleThreadName(Thread.currentThread().getName());
        try {
          task.run();
        } catch (Throwable e) {
          LOGGER.error("[{}]-[{}] error occurs while executing task, actionType is {}",
              invocationType, connectionId, event.getActionType());
        } finally {
          eventBus.post(
              event.setActionEndTimestamp(System.currentTimeMillis())
          );
        }
      });
    } catch (Throwable e) {
      LOGGER.error("[{}]-[{}] error occurs in scheduleTask", invocationType, connectionId, e);
    }
  }

  @Override
  public CompletableFuture<Void> sendMessage(WebSocketMessage<?> message) {
    if (message instanceof TextWebSocketMessage) {
      String payload = ((TextWebSocketMessage) message).getPayload();
      return decorateSenderAction(WebSocketActionType.DO_SEND_TEXT,
          payload.length(),
          vertxWebSocket.writeTextMessage(
                  payload)
              .toCompletionStage()
              .toCompletableFuture());
    }

    if (message instanceof BinaryBytesWebSocketMessage) {
      byte[] payload = ((BinaryBytesWebSocketMessage) message).getPayload();
      return decorateSenderAction(WebSocketActionType.DO_SEND_BINARY,
          payload.length,
          vertxWebSocket.writeBinaryMessage(Buffer.buffer(
                  payload))
              .toCompletionStage()
              .toCompletableFuture());
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
    return decorateSenderAction(WebSocketActionType.DO_CLOSE, 0,
        vertxWebSocket.close(statusCode, reason)
            .toCompletionStage()
            .toCompletableFuture());
  }

  @Override
  public void pause() {
    WebSocketActionEvent event = createWebSocketActionEventInSyncMode(WebSocketActionType.DO_PAUSE);
    synchronized (pauseLock) {
      if (inPauseStatus) {
        return;
      }
      vertxWebSocket.pause();
      inPauseStatus = true;
      LOGGER.info("[{}]-[{}] pause websocket", invocationType, connectionId);
    }
    eventBus.post(event.setActionEndTimestamp(System.currentTimeMillis()));
  }

  @Override
  public void resume() {
    WebSocketActionEvent event = createWebSocketActionEventInSyncMode(WebSocketActionType.DO_RESUME);
    synchronized (pauseLock) {
      if (!inPauseStatus) {
        return;
      }
      vertxWebSocket.resume();
      inPauseStatus = false;
      LOGGER.info("[{}]-[{}] resume websocket", invocationType, connectionId);
    }
    eventBus.post(event.setActionEndTimestamp(System.currentTimeMillis()));
  }

  @Override
  public boolean writeQueueFull() {
    return vertxWebSocket.writeQueueFull();
  }

  private <T> CompletableFuture<T> decorateSenderAction(WebSocketActionType actionType,
      int dataSize,
      CompletableFuture<T> actionFuture) {
    WebSocketActionEvent event = createWebSocketActionEventInSyncMode(actionType).setDataSize(dataSize);
    actionFuture
        .whenComplete((v, t) ->
            eventBus.post(event.setActionEndTimestamp(System.currentTimeMillis())));
    return actionFuture;
  }
}
