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

import org.apache.servicecomb.foundation.vertx.client.http.HttpClientOptionsSPI;

import com.netflix.config.ConcurrentCompositeConfiguration;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;

public class HttpTransportHttpClientOptionsSPI implements HttpClientOptionsSPI {
  public static final String CLIENT_NAME = "http-transport-client";

  public static final String CLIENT_TAG = "rest.consumer";

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
    return TransportClientConfig.isHttpTransportClientEnabled();
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
    return TransportClientConfig.getThreadCount();
  }

  @Override
  public boolean isWorker() {
    return true;
  }

  @Override
  public String getWorkerPoolName() {
    return "pool-transport-client-http";
  }

  @Override
  public int getWorkerPoolSize() {
    return 2;
  }

  @Override
  public HttpVersion getHttpVersion() {
    return HttpVersion.HTTP_1_1;
  }

  @Override
  public int getConnectTimeoutInMillis() {
    return TransportClientConfig.getConnectionTimeoutInMillis();
  }

  @Override
  public int getIdleTimeoutInSeconds() {
    return TransportClientConfig.getConnectionIdleTimeoutInSeconds();
  }

  @Override
  public boolean isTryUseCompression() {
    return TransportClientConfig.getConnectionCompression();
  }

  @Override
  public int getMaxWaitQueueSize() {
    return TransportClientConfig.getMaxWaitQueueSize();
  }

  @Override
  public int getMaxPoolSize() {
    return TransportClientConfig.getConnectionMaxPoolSize();
  }

  @Override
  public boolean isKeepAlive() {
    return TransportClientConfig.getConnectionKeepAlive();
  }

  @Override
  public int getMaxHeaderSize() {
    return TransportClientConfig.getMaxHeaderSize();
  }

  @Override
  public int getKeepAliveTimeout() {
    return TransportClientConfig.getConnectionIdleTimeoutInSeconds();
  }

  @Override
  public int getHttp2MultiplexingLimit() {
    return HttpClientOptions.DEFAULT_HTTP2_MULTIPLEXING_LIMIT;
  }

  @Override
  public int getHttp2MaxPoolSize() {
    return HttpClientOptions.DEFAULT_HTTP2_MAX_POOL_SIZE;
  }

  @Override
  public boolean isUseAlpn() {
    return HttpClientOptions.DEFAULT_USE_ALPN;
  }

  @Override
  public boolean isProxyEnable() {
    // now transport proxy not implemented
    return false;
  }

  @Override
  public String getProxyHost() {
    return null;
  }

  @Override
  public int getProxyPort() {
    return 0;
  }

  @Override
  public String getProxyUsername() {
    return null;
  }

  @Override
  public String getProxyPassword() {
    return null;
  }

  @Override
  public boolean isSsl() {
    return true;
  }
}
