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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;

/**
 * A pipe that connecting server and client side {@link WebSocket}s.
 */
public class WebSocketPipe {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketPipe.class);

  private final PipeServerWebSocket serverWebSocket;

  private final PipeClientWebSocket clientWebSocket;

  private final Object statusLock = new Object();

  public WebSocketPipe() {
    this.clientWebSocket = new PipeClientWebSocket();
    this.serverWebSocket = new PipeServerWebSocket();
    this.clientWebSocket.connect(serverWebSocket);
    this.serverWebSocket.connect(clientWebSocket);
  }

  public ClientWebSocket getClientWebSocket() {
    return clientWebSocket;
  }

  public ServerWebSocket getServerWebSocket() {
    return serverWebSocket;
  }

  private enum PipeWebSocketStatus {
    CREATED,
    PEER_CONNECTED,
    OPENED,
    RUNNING,
    ERROR,
    CLOSING,
    CLOSED
  }

  private class PipeServerWebSocket extends ServerWebSocket {
    PipeClientWebSocket peer;

    private PipeWebSocketStatus status = PipeWebSocketStatus.CREATED;

    void connect(PipeClientWebSocket peer) {
      synchronized (statusLock) {
        transferStatus(PipeWebSocketStatus.CREATED, PipeWebSocketStatus.PEER_CONNECTED);
        this.peer = peer;
      }
    }

    @Override
    public void onConnectionReady() {
      pause();
      onOpen();
    }

    @Override
    public void onOpen() {
      synchronized (statusLock) {
        transferStatus(PipeWebSocketStatus.PEER_CONNECTED, PipeWebSocketStatus.OPENED);
        if (this.peer.status == PipeWebSocketStatus.OPENED) {
          resume();
          transferStatus(PipeWebSocketStatus.OPENED, PipeWebSocketStatus.RUNNING);
          peer.resume();
          peer.transferStatus(PipeWebSocketStatus.OPENED, PipeWebSocketStatus.RUNNING);
        }
      }
    }

    @Override
    public void onMessage(WebSocketMessage<?> message) {
      peer.sendMessage(message)
          .whenComplete((v, t) -> {
            if (t != null) {
              LOGGER.error("failed to forward message", t);
            }
          });
      if (peer.writeQueueFull()) {
        this.pause();
        LOGGER.debug("pipe paused, direction is server to client");
      }
    }

    @Override
    public void onError(Throwable t) {
      LOGGER.error("websocket error", t);
      synchronized (statusLock) {
        transferStatus(PipeWebSocketStatus.ERROR);
        safelyClose();
      }
    }

    @Override
    public void onClose(Short closeStatusCode, String closeReason) {
      transferStatus(PipeWebSocketStatus.CLOSED);
      safelyClose(); // should close peer
    }

    @Override
    public void onDrain() {
      peer.resume();
    }

    @Override
    public CompletableFuture<Void> close(Short closeStatusCode, String closeReason) {
      synchronized (statusLock) {
        if (status == PipeWebSocketStatus.CLOSING || status == PipeWebSocketStatus.CLOSED) {
          return CompletableFuture.completedFuture(null);
        }
        transferStatus(PipeWebSocketStatus.CLOSING);
        if (status.ordinal() < PipeWebSocketStatus.RUNNING.ordinal()) {
          transferStatus(PipeWebSocketStatus.CLOSED);
          return CompletableFuture.completedFuture(null);
        }
        return super.close(closeStatusCode, closeReason)
            .whenComplete((result, throwable) -> transferStatus(PipeWebSocketStatus.CLOSED));
      }
    }

    private void transferStatus(PipeWebSocketStatus newStatus) {
      synchronized (statusLock) {
        this.status = newStatus;
      }
    }

    private void transferStatus(PipeWebSocketStatus expectedOldStatus, PipeWebSocketStatus newStatus) {
      synchronized (statusLock) {
        if (status != expectedOldStatus) {
          safelyClose();
          throw new IllegalStateException("Illegal state transfer: ["
              + expectedOldStatus
              + "] to ["
              + newStatus
              + "]");
        }
        status = newStatus;
      }
    }

    private void safelyClose() {
      try {
        close((short) WebSocketCloseStatus.INTERNAL_SERVER_ERROR.code(),
            WebSocketCloseStatus.INTERNAL_SERVER_ERROR.reasonText());
      } catch (Throwable e) {
        LOGGER.error("failed to close pipe server websocket", e);
      }
      if (peer == null) {
        return;
      }
      try {
        peer.close((short) WebSocketCloseStatus.INTERNAL_SERVER_ERROR.code(),
            WebSocketCloseStatus.INTERNAL_SERVER_ERROR.reasonText());
      } catch (Throwable e) {
        LOGGER.error("failed to close pipe client websocket", e);
      }
    }
  }

  private class PipeClientWebSocket extends ClientWebSocket {
    PipeServerWebSocket peer;

    private PipeWebSocketStatus status = PipeWebSocketStatus.CREATED;

    void connect(PipeServerWebSocket peer) {
      transferStatus(PipeWebSocketStatus.CREATED, PipeWebSocketStatus.PEER_CONNECTED);
      this.peer = peer;
    }

    @Override
    public void onConnectionReady() {
      pause();
      onOpen();
    }

    @Override
    public void onOpen() {
      synchronized (statusLock) {
        transferStatus(PipeWebSocketStatus.PEER_CONNECTED, PipeWebSocketStatus.OPENED);
        if (this.peer.status == PipeWebSocketStatus.OPENED) {
          resume();
          transferStatus(PipeWebSocketStatus.OPENED, PipeWebSocketStatus.RUNNING);
          peer.resume();
          peer.transferStatus(PipeWebSocketStatus.OPENED, PipeWebSocketStatus.RUNNING);
        }
      }
    }

    @Override
    public void onMessage(WebSocketMessage<?> message) {
      peer.sendMessage(message)
          .whenComplete((v, t) -> {
            if (t != null) {
              LOGGER.error("failed to forward message", t);
            }
          });
      if (peer.writeQueueFull()) {
        this.pause();
        LOGGER.debug("pipe paused, direction is client to server");
      }
    }

    @Override
    public void onError(Throwable t) {
      LOGGER.error("websocket error", t);
      synchronized (statusLock) {
        transferStatus(PipeWebSocketStatus.ERROR);
        safelyClose();
      }
    }

    @Override
    public void onClose(Short closeStatusCode, String closeReason) {
      transferStatus(PipeWebSocketStatus.CLOSED);
      safelyClose(); // should close peer
    }

    @Override
    public void onDrain() {
      peer.resume();
    }

    private void transferStatus(PipeWebSocketStatus newStatus) {
      synchronized (statusLock) {
        this.status = newStatus;
      }
    }

    private void transferStatus(PipeWebSocketStatus expectedOldStatus, PipeWebSocketStatus newStatus) {
      synchronized (statusLock) {
        if (status != expectedOldStatus) {
          safelyClose();
          throw new IllegalStateException("Illegal state transfer: ["
              + expectedOldStatus
              + "] to ["
              + newStatus
              + "]");
        }
        status = newStatus;
      }
    }

    private void safelyClose() {
      try {
        close((short) WebSocketCloseStatus.INTERNAL_SERVER_ERROR.code(),
            WebSocketCloseStatus.INTERNAL_SERVER_ERROR.reasonText());
      } catch (Throwable e) {
        LOGGER.error("failed to close pipe client websocket", e);
      }
      if (peer == null) {
        return;
      }
      try {
        peer.close((short) WebSocketCloseStatus.INTERNAL_SERVER_ERROR.code(),
            WebSocketCloseStatus.INTERNAL_SERVER_ERROR.reasonText());
      } catch (Throwable e) {
        LOGGER.error("failed to close pipe server websocket", e);
      }
    }
  }
}
