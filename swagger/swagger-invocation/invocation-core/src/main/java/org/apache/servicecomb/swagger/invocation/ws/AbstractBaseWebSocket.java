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

/**
 * AbstractBaseWebSocket
 */
public abstract class AbstractBaseWebSocket implements WebSocket {
  private WebSocketAdapter webSocketAdapter;

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
    return webSocketAdapter.close((short) 1000, "NORMAL");
  }

  @Override
  public CompletableFuture<Void> close(Short closeStatusCode, String closeReason) {
    return webSocketAdapter.close(closeStatusCode, closeReason);
  }

  @Override
  public void pause() {
    webSocketAdapter.pause();
  }

  @Override
  public void resume() {
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
}
