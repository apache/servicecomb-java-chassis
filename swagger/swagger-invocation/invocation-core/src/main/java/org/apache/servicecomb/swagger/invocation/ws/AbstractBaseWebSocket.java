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

package org.apache.servicecomb.swagger.invocation.ws;

import java.util.concurrent.CompletableFuture;

import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;

/**
 * AbstractBaseWebSocket
 */
public abstract class AbstractBaseWebSocket implements WebSocket {
  private WebSocketAdapter webSocketAdapter;

  private Status status = Status.CREATED;

  private CompletableFuture<Void> closeFuture;

  private Short closeStatusCode;

  private String closeReason;

  @Override
  public CompletableFuture<Void> sendMessage(WebSocketMessage<?> message) {
    return webSocketAdapter.sendMessage(message);
  }

  @Override
  public CompletableFuture<Void> sendFrame(WebSocketFrame frame) {
    return webSocketAdapter.sendFrame(frame);
  }

  @Override
  public CompletableFuture<Void> close() {
    return this.close((short) WebSocketCloseStatus.NORMAL_CLOSURE.code(),
        WebSocketCloseStatus.NORMAL_CLOSURE.reasonText());
  }

  @Override
  public CompletableFuture<Void> close(Short closeStatusCode, String closeReason) {
    synchronized (this) {
      if (status == Status.WAITING_TO_CLOSE || status == Status.CLOSING || status == Status.CLOSED) {
        return CompletableFuture.completedFuture(null);
      }
      status = Status.WAITING_TO_CLOSE;
      this.closeStatusCode = closeStatusCode;
      this.closeReason = closeReason;
      if (webSocketAdapter == null) {
        // the case that close when WebSocket still not complete handshake
        closeFuture = new CompletableFuture<>();
        return closeFuture;
      }
    }
    status = Status.CLOSING;
    return webSocketAdapter.close(closeStatusCode, closeReason)
        .whenComplete((v, t) -> status = Status.CLOSED);
  }

  @Override
  public void pause() {
    status = Status.PAUSED;
    webSocketAdapter.pause();
  }

  @Override
  public void resume() {
    status = Status.RUNNING;
    webSocketAdapter.resume();
  }

  /**
   * Check whether the message sending queue is full, in which case you should pause your sending action.
   * To get to know when to recover sending action, please override the {@link #onWriteQueueDrain()} method
   * to subscribe the notification that the message sending queue is ready to accept message again.
   *
   * @return true if message sending queue is full.
   */
  @Override
  public boolean writeQueueFull() {
    return webSocketAdapter.writeQueueFull();
  }

  /**
   * The callback to notify when the message sending queue is ready to accept sending message/frame again.
   * Usually this method is used in conjunction with the {@link #writeQueueFull()}.
   */
  @Override
  public void onWriteQueueDrain() {
  }

  public void setWebSocketAdapter(WebSocketAdapter webSocketAdapter) {
    this.webSocketAdapter = webSocketAdapter;
  }

  public void startWorking() {
    synchronized (this) {
      if (status == Status.WAITING_TO_CLOSE) {
        status = Status.CLOSING;
        webSocketAdapter.close(closeStatusCode, closeReason)
            .whenComplete((v, t) -> {
              status = Status.CLOSED;
              if (t != null) {
                closeFuture.completeExceptionally(t);
              } else {
                closeFuture.complete(null);
              }
            });
        return;
      }
      status = Status.RUNNING;
    }
    onConnectionReady();
  }

  @Override
  public Status getStatus() {
    return status;
  }
}
