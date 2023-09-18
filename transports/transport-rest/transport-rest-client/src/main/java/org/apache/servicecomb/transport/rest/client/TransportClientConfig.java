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

package org.apache.servicecomb.transport.rest.client;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.transport.common.TransportConfigUtils;

import io.vertx.core.http.HttpClientOptions;

public final class TransportClientConfig {
  private static final int DEFAULT_IDLE_TIME_OUT = 150;

  private static final int DEFAULT_KEEP_ALIVE_TIME_OUT = 60;

  private TransportClientConfig() {
  }

  public static int getThreadCount() {
    return TransportConfigUtils.readVerticleCount(
        "servicecomb.rest.client.verticle-count",
        "servicecomb.rest.client.thread-count");
  }

  public static int getHttp2ConnectionMaxPoolSize() {
    return LegacyPropertyFactory.getIntProperty("servicecomb.rest.client.http2.maxPoolSize",
        HttpClientOptions.DEFAULT_HTTP2_MAX_POOL_SIZE);
  }

  public static int getHttp2MultiplexingLimit() {
    return LegacyPropertyFactory.getIntProperty("servicecomb.rest.client.http2.multiplexingLimit",
        HttpClientOptions.DEFAULT_HTTP2_MULTIPLEXING_LIMIT);
  }

  public static boolean getUseAlpn() {
    return LegacyPropertyFactory.getBooleanProperty("servicecomb.rest.client.http2.useAlpnEnabled", true);
  }

  public static boolean isHttp2TransportClientEnabled() {
    return LegacyPropertyFactory.getBooleanProperty("servicecomb.rest.client.http2.enabled",
        true);
  }

  public static int getConnectionMaxPoolSize() {
    return LegacyPropertyFactory.getIntProperty("servicecomb.rest.client.connection.maxPoolSize",
        HttpClientOptions.DEFAULT_MAX_POOL_SIZE);
  }

  public static int getHttp2ConnectionIdleTimeoutInSeconds() {
    return LegacyPropertyFactory.getIntProperty("servicecomb.rest.client.http2.connection.idleTimeoutInSeconds",
        DEFAULT_IDLE_TIME_OUT);
  }

  public static int getConnectionIdleTimeoutInSeconds() {
    return LegacyPropertyFactory.getIntProperty("servicecomb.rest.client.connection.idleTimeoutInSeconds",
        DEFAULT_IDLE_TIME_OUT);
  }

  public static boolean getConnectionKeepAlive() {
    return LegacyPropertyFactory
        .getBooleanProperty("servicecomb.rest.client.connection.keepAlive", HttpClientOptions.DEFAULT_KEEP_ALIVE);
  }

  public static int getConnectionKeepAliveTimeoutInSeconds() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.client.connection.keepAliveTimeoutInSeconds", DEFAULT_KEEP_ALIVE_TIME_OUT);
  }

  public static int getHttp2ConnectionKeepAliveTimeoutInSeconds() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.client.http2.connection.keepAliveTimeoutInSeconds",
            DEFAULT_KEEP_ALIVE_TIME_OUT);
  }

  public static boolean getConnectionCompression() {
    return LegacyPropertyFactory
        .getBooleanProperty("servicecomb.rest.client.connection.compression",
            HttpClientOptions.DEFAULT_TRY_USE_COMPRESSION);
  }

  public static int getMaxHeaderSize() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.client.maxHeaderSize", HttpClientOptions.DEFAULT_MAX_HEADER_SIZE);
  }

  public static int getMaxWaitQueueSize() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.client.maxWaitQueueSize",
            HttpClientOptions.DEFAULT_MAX_WAIT_QUEUE_SIZE);
  }

  public static boolean isHttpTransportClientEnabled() {
    return LegacyPropertyFactory
        .getBooleanProperty("servicecomb.rest.client.enabled",
            true);
  }

  public static int getConnectionTimeoutInMillis() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.client.connection.timeoutInMillis", 1000);
  }
}
