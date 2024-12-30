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
 * The WebSocket session interface. This is the root WebSocket type,
 * and any concrete implementation class should not inherit from this type directly.
 * Please inherit from {@link ServerWebSocket} and {@link ClientWebSocket} instead.
 */
public interface WebSocket {
  default void onConnectionReady() {
    onOpen();
    resume();
  }

  void onOpen();

  void onMessage(WebSocketMessage<?> message);

  default void onFrame(WebSocketFrame frame) {
  }

  void onError(Throwable t);

  void onClose(Short closeStatusCode, String closeReason);

  CompletableFuture<Void> sendMessage(WebSocketMessage<?> message);

  CompletableFuture<Void> sendFrame(WebSocketFrame frame);

  CompletableFuture<Void> close();

  CompletableFuture<Void> close(Short closeStatusCode, String closeReason);

  void pause();

  void resume();

  boolean writeQueueFull();

  void onWriteQueueDrain();

  Status getStatus();

  enum Status {
    CREATED,
    RUNNING,
    PAUSED,
    WAITING_TO_CLOSE,
    CLOSING,
    CLOSED
  }
}
