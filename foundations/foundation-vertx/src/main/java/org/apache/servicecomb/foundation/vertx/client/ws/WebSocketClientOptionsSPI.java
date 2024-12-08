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

package org.apache.servicecomb.foundation.vertx.client.ws;

import org.apache.servicecomb.foundation.vertx.VertxTLSBuilder;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientOptionsSPI;

import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.WebSocketClientOptions;

/**
 * WebSocket client options must be set by implementation.
 */
public abstract class WebSocketClientOptionsSPI implements HttpClientOptionsSPI {
  public static WebSocketClientOptions createWebSocketClientOptions(WebSocketClientOptionsSPI spi) {
    WebSocketClientOptions webSocketClientOptions = new WebSocketClientOptions();
    HttpClientOptionsSPI.buildClientOptionsBase(spi, webSocketClientOptions);

    webSocketClientOptions.setMaxFrameSize(spi.getMaxFrameSize());
    webSocketClientOptions.setMaxMessageSize(spi.getMaxMessageSize());
    webSocketClientOptions.setMaxConnections(spi.getMaxConnections());
    webSocketClientOptions.setTryUsePerFrameCompression(spi.getTryUsePerFrameCompression());
    webSocketClientOptions.setTryUsePerMessageCompression(spi.getTryUsePerMessageCompression());
    webSocketClientOptions.setCompressionLevel(spi.getCompressionLevel());
    webSocketClientOptions.setClosingTimeout(spi.getClosingTimeoutInSeconds());
    webSocketClientOptions.setUseAlpn(spi.isUseAlpn());

    if (spi.isSsl()) {
      VertxTLSBuilder.buildWebSocketClientOptions(spi.getConfigTag(), webSocketClientOptions);
    }
    return webSocketClientOptions;
  }

  public abstract int getMaxFrameSize();

  public abstract int getMaxMessageSize();

  /**
   * Set the max number of WebSockets per endpoint.
   * @see WebSocketClientOptions#setMaxConnections(int)
   */
  public abstract int getMaxConnections();

  public abstract boolean getTryUsePerFrameCompression();

  public abstract boolean getTryUsePerMessageCompression();

  public abstract int getCompressionLevel();

  public abstract int getClosingTimeoutInSeconds();

  @Override
  public final HttpVersion getHttpVersion() {
    // does not actually work, just avoid error in
    // org.apache.servicecomb.foundation.vertx.client.http.HttpClientOptionsSPI.buildClientOptionsBase
    return HttpVersion.HTTP_1_1;
  }

  @Override
  public final boolean isTryUseCompression() {
    throw unsupportedException();
  }

  @Override
  public final int getMaxWaitQueueSize() {
    throw unsupportedException();
  }

  @Override
  public final int getMaxPoolSize() {
    throw unsupportedException();
  }

  @Override
  public final boolean isKeepAlive() {
    throw unsupportedException();
  }

  @Override
  public final int getMaxHeaderSize() {
    throw unsupportedException();
  }

  @Override
  public final int getHttp2MultiplexingLimit() {
    throw unsupportedException();
  }

  @Override
  public final int getHttp2MaxPoolSize() {
    throw unsupportedException();
  }

  @Override
  public final int getKeepAliveTimeout() {
    throw unsupportedException();
  }

  protected static UnsupportedOperationException unsupportedException() {
    return new UnsupportedOperationException("WebSocket Not Support this option.");
  }
}
