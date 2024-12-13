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

package org.apache.servicecomb.transport.rest.client.ws;

import org.apache.servicecomb.foundation.vertx.client.ws.WebSocketClientOptionsSPI;
import org.apache.servicecomb.transport.common.TransportConfigUtils;
import org.apache.servicecomb.transport.rest.client.HttpTransportHttpClientOptionsSPI;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;

public class WebSocketTransportClientOptionsSPI extends WebSocketClientOptionsSPI {
  public static final String CLIENT_NAME = "ws-transport-client";

  public static final String CLIENT_TAG = "websocket.consumer";

  public static final boolean DEFAULT_CLIENT_COMPRESSION_SUPPORT = false;

  private static final int DEFAULT_IDLE_TIME_OUT = 150;

  @Override
  public String clientName() {
    return CLIENT_NAME;
  }

  @Override
  public int getOrder() {
    return 100;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.websocket.client.enabled",
            true)
        .get();
  }

  @Override
  public String getConfigTag() {
    return CLIENT_TAG;
  }

  @Override
  public ConcurrentCompositeConfiguration getConfigReader() {
    return null;
  }

  @Override
  public int getEventLoopPoolSize() {
    // not reading this, using shared transport vert.x
    return -1;
  }

  @Override
  public boolean useSharedVertx() {
    return true;
  }

  @Override
  public int getInstanceCount() {
    return TransportConfigUtils.readVerticleCount(
        "servicecomb.websocket.client.verticle-count",
        "servicecomb.websocket.client.thread-count");
  }

  @Override
  public boolean isWorker() {
    return false;
  }

  @Override
  public String getWorkerPoolName() {
    return "pool-worker-transport-client-websocket";
  }

  @Override
  public int getWorkerPoolSize() {
    return VertxOptions.DEFAULT_WORKER_POOL_SIZE;
  }

  @Override
  public int getConnectTimeoutInMillis() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.websocket.client.connection.timeoutInMillis", 60000)
        .get();
  }

  @Override
  public int getIdleTimeoutInSeconds() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.websocket.client.connection.idleTimeoutInSeconds",
            DEFAULT_IDLE_TIME_OUT)
        .get();
  }

  @Override
  public boolean enableLogActivity() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.websocket.client.enableLogActivity", false)
        .get();
  }

  @Override
  public boolean isUseAlpn() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.websocket.client.useAlpnEnabled", false)
        .get();
  }

  /**
   * keep the same to {@link HttpTransportHttpClientOptionsSPI#isProxyEnable()}
   */
  @Override
  public boolean isProxyEnable() {
    return false;
  }

  /**
   * keep the same to {@link HttpTransportHttpClientOptionsSPI#getProxyHost()}
   */
  @Override
  public String getProxyHost() {
    return null;
  }

  /**
   * keep the same to {@link HttpTransportHttpClientOptionsSPI#getProxyPort()}
   */
  @Override
  public int getProxyPort() {
    return 0;
  }

  /**
   * keep the same to {@link HttpTransportHttpClientOptionsSPI#getProxyUsername()}
   */
  @Override
  public String getProxyUsername() {
    return null;
  }

  /**
   * keep the same to {@link HttpTransportHttpClientOptionsSPI#getProxyPassword()}
   */
  @Override
  public String getProxyPassword() {
    return null;
  }

  @Override
  public boolean isSsl() {
    return true;
  }

  // options below are only owned by websocket
  @Override
  public int getMaxFrameSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.websocket.client.maxFrameSize",
            HttpClientOptions.DEFAULT_MAX_WEBSOCKET_FRAME_SIZE)
        .get();
  }

  @Override
  public int getMaxMessageSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.websocket.client.maxMessageSize",
            HttpClientOptions.DEFAULT_MAX_WEBSOCKET_MESSAGE_SIZE)
        .get();
  }

  @Override
  public int getMaxConnections() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.websocket.client.connection.maxPoolSize",
            HttpClientOptions.DEFAULT_MAX_WEBSOCKETS)
        .get();
  }

  @Override
  public boolean getTryUsePerFrameCompression() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.websocket.client.tryUsePerFrameCompression",
            DEFAULT_CLIENT_COMPRESSION_SUPPORT)
        .get();
  }

  @Override
  public boolean getTryUsePerMessageCompression() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.websocket.client.tryUsePerMessageCompression",
            DEFAULT_CLIENT_COMPRESSION_SUPPORT)
        .get();
  }

  @Override
  public int getCompressionLevel() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.websocket.client.compressionLevel",
            HttpClientOptions.DEFAULT_WEBSOCKET_COMPRESSION_LEVEL)
        .get();
  }

  @Override
  public int getClosingTimeoutInSeconds() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.websocket.client.connection.closingTimeoutInSeconds",
            HttpClientOptions.DEFAULT_WEBSOCKET_CLOSING_TIMEOUT)
        .get();
  }
}
