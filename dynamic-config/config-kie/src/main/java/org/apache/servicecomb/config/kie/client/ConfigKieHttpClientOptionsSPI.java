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

package org.apache.servicecomb.config.kie.client;

import org.apache.servicecomb.foundation.vertx.client.http.HttpClientOptionsSPI;

import com.netflix.config.ConcurrentCompositeConfiguration;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;

public class ConfigKieHttpClientOptionsSPI implements HttpClientOptionsSPI {
  public static final String CLIENT_NAME = "config-kie";

  @Override
  public String clientName() {
    return CLIENT_NAME;
  }

  @Override
  public int getOrder() {
    return -200;
  }

  @Override
  public boolean enabled() {
    return KieConfig.INSTANCE.getServerUri() != null;
  }

  @Override
  public String getConfigTag() {
    return "kie.consumer";
  }

  @Override
  public ConcurrentCompositeConfiguration getConfigReader() {
    return KieConfig.getFinalConfig();
  }

  @Override
  public int getEventLoopPoolSize() {
    return KieConfig.INSTANCE.getEventLoopSize();
  }

  @Override
  public boolean useSharedVertx() {
    return false;
  }

  @Override
  public int getInstanceCount() {
    return KieConfig.INSTANCE.getVerticalInstanceCount();
  }

  @Override
  public boolean isWorker() {
    return false;
  }

  @Override
  public String getWorkerPoolName() {
    return "pool-worker-kie-client";
  }

  @Override
  public int getWorkerPoolSize() {
    return VertxOptions.DEFAULT_WORKER_POOL_SIZE;
  }

  @Override
  public HttpVersion getHttpVersion() {
    return HttpVersion.HTTP_1_1;
  }

  @Override
  public int getConnectTimeoutInMillis() {
    return KieConfig.INSTANCE.getConnectionTimeOut();
  }

  @Override
  public int getIdleTimeoutInSeconds() {
    return KieConfig.INSTANCE.getIdleTimeoutInSeconds();
  }

  @Override
  public boolean isTryUseCompression() {
    return HttpClientOptions.DEFAULT_TRY_USE_COMPRESSION;
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
    return KieConfig.INSTANCE.isProxyEnable();
  }

  @Override
  public String getProxyHost() {
    return KieConfig.INSTANCE.getProxyHost();
  }

  @Override
  public int getProxyPort() {
    return KieConfig.INSTANCE.getProxyPort();
  }

  @Override
  public String getProxyUsername() {
    return KieConfig.INSTANCE.getProxyUsername();
  }

  @Override
  public String getProxyPassword() {
    return KieConfig.INSTANCE.getProxyPasswd();
  }

  @Override
  public boolean isSsl() {
    return KieConfig.INSTANCE.getServerUri() != null && KieConfig.INSTANCE.getServerUri().startsWith("https");
  }
}
