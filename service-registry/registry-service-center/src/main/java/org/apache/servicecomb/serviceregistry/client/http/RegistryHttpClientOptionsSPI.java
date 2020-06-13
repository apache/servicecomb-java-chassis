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

package org.apache.servicecomb.serviceregistry.client.http;

import org.apache.servicecomb.foundation.vertx.client.http.HttpClientOptionsSPI;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;

public class RegistryHttpClientOptionsSPI implements HttpClientOptionsSPI {
  public static final String CLIENT_NAME = "registry";

  private ServiceRegistryConfig serviceRegistryConfig = ServiceRegistryConfig.INSTANCE;

  @Override
  public String clientName() {
    return CLIENT_NAME;
  }

  @Override
  public int getOrder() {
    return -100;
  }

  @Override
  public boolean enabled() {
    return true;
  }

  @Override
  public String getConfigTag() {
    return "sc.consumer";
  }

  @Override
  public ConcurrentCompositeConfiguration getConfigReader() {
    return null;
  }

  @Override
  public int getEventLoopPoolSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty(ServiceRegistryConfig.EVENT_LOOP_POOL_SIZE, 4).get();
  }

  @Override
  public boolean useSharedVertx() {
    return false;
  }

  @Override
  public int getInstanceCount() {
    return serviceRegistryConfig.getInstances();
  }

  @Override
  public boolean isWorker() {
    return false;
  }

  @Override
  public String getWorkerPoolName() {
    return "pool-worker-service-center-client";
  }

  @Override
  public int getWorkerPoolSize() {
    return VertxOptions.DEFAULT_WORKER_POOL_SIZE;
  }

  @Override
  public HttpVersion getHttpVersion() {
    return serviceRegistryConfig.getHttpVersion();
  }

  @Override
  public int getConnectTimeoutInMillis() {
    return serviceRegistryConfig.getConnectionTimeout();
  }

  @Override
  public int getIdleTimeoutInSeconds() {
    return serviceRegistryConfig.getIdleConnectionTimeout();
  }

  @Override
  public boolean isTryUseCompression() {
    return false;
  }

  @Override
  public int getMaxWaitQueueSize() {
    return HttpClientOptions.DEFAULT_MAX_WAIT_QUEUE_SIZE;
  }

  @Override
  public int getMaxPoolSize() {
    return HttpClientOptions.DEFAULT_MAX_POOL_SIZE;
  }

  @Override
  public boolean isKeepAlive() {
    return HttpClientOptions.DEFAULT_KEEP_ALIVE;
  }

  @Override
  public int getMaxHeaderSize() {
    return HttpClientOptions.DEFAULT_MAX_HEADER_SIZE;
  }

  @Override
  public int getKeepAliveTimeout() {
    return HttpClientOptions.DEFAULT_KEEP_ALIVE_TIMEOUT;
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
    return serviceRegistryConfig.isProxyEnable();
  }

  @Override
  public String getProxyHost() {
    return serviceRegistryConfig.getProxyHost();
  }

  @Override
  public int getProxyPort() {
    return serviceRegistryConfig.getProxyPort();
  }

  @Override
  public String getProxyUsername() {
    return serviceRegistryConfig.getProxyUsername();
  }

  @Override
  public String getProxyPassword() {
    return serviceRegistryConfig.getProxyPasswd();
  }

  @Override
  public boolean isSsl() {
    return serviceRegistryConfig.isSsl();
  }
}
