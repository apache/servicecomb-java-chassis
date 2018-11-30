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

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.TCPSSLOptions;

public final class TransportClientConfig {
  private static Class<? extends RestTransportClient> restTransportClientCls = RestTransportClient.class;

  private TransportClientConfig() {
  }

  public static Class<? extends RestTransportClient> getRestTransportClientCls() {
    return restTransportClientCls;
  }

  public static void setRestTransportClientCls(Class<? extends RestTransportClient> restTransportClientCls) {
    TransportClientConfig.restTransportClientCls = restTransportClientCls;
  }

  public static int getThreadCount() {
    return DynamicPropertyFactory.getInstance().getIntProperty("servicecomb.rest.client.thread-count", 1).get();
  }

  public static int getHttp2ConnectionMaxPoolSize() {
    return DynamicPropertyFactory.getInstance().getIntProperty("servicecomb.rest.client.http2.maxPoolSize",
        HttpClientOptions.DEFAULT_HTTP2_MAX_POOL_SIZE)
        .get();
  }

  public static int getHttp2MultiplexingLimit() {
    return DynamicPropertyFactory.getInstance().getIntProperty("servicecomb.rest.client.http2.multiplexingLimit",
        HttpClientOptions.DEFAULT_HTTP2_MULTIPLEXING_LIMIT)
        .get();
  }

  public static int getHttp2ConnectionIdleTimeoutInSeconds() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.client.http2.idleTimeoutInSeconds", TCPSSLOptions.DEFAULT_IDLE_TIMEOUT)
        .get();
  }


  public static boolean getUseAlpn() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.client.http2.useAlpnEnabled", true)
        .get();
  }

  public static int getConnectionMaxPoolSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.client.connection.maxPoolSize", HttpClientOptions.DEFAULT_MAX_POOL_SIZE)
        .get();
  }

  public static int getConnectionIdleTimeoutInSeconds() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.client.connection.idleTimeoutInSeconds", 30)
        .get();
  }

  public static boolean getConnectionKeepAlive() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.client.connection.keepAlive", HttpClientOptions.DEFAULT_KEEP_ALIVE)
        .get();
  }


  public static boolean getConnectionCompression() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.client.connection.compression",
            HttpClientOptions.DEFAULT_TRY_USE_COMPRESSION)
        .get();
  }
}
