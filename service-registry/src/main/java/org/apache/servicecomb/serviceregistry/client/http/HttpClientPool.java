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

import org.apache.servicecomb.foundation.common.encrypt.Encryptions;
import org.apache.servicecomb.foundation.vertx.VertxTLSBuilder;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.ProxyOptions;

final class HttpClientPool extends AbstractClientPool {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientPool.class);

  /**
   * The default instance, for default sc cluster.
   */
  public static final HttpClientPool INSTANCE = new HttpClientPool();

  private HttpClientPool() {
    super(getHttpClientOptionsFromConfigurations(ServiceRegistryConfig.INSTANCE));
  }

  HttpClientPool(ServiceRegistryConfig serviceRegistryConfig) {
    super(getHttpClientOptionsFromConfigurations(serviceRegistryConfig));
  }

  @Override
  protected boolean isWorker() {
    return false;
  }

  static HttpClientOptions getHttpClientOptionsFromConfigurations(ServiceRegistryConfig serviceRegistryConfig) {
    HttpVersion ver = serviceRegistryConfig.getHttpVersion();
    HttpClientOptions httpClientOptions = new HttpClientOptions();
    httpClientOptions.setProtocolVersion(ver);
    httpClientOptions.setConnectTimeout(serviceRegistryConfig.getConnectionTimeout());
    httpClientOptions.setIdleTimeout(serviceRegistryConfig.getIdleConnectionTimeout());
    if (serviceRegistryConfig.isProxyEnable()) {
      ProxyOptions proxy = new ProxyOptions();
      proxy.setHost(serviceRegistryConfig.getProxyHost());
      proxy.setPort(serviceRegistryConfig.getProxyPort());
      proxy.setUsername(serviceRegistryConfig.getProxyUsername());
      proxy.setPassword(
          Encryptions.decode(serviceRegistryConfig.getProxyPasswd(), ServiceRegistryConfig.PROXY_KEY));
      httpClientOptions.setProxyOptions(proxy);
    }
    if (ver == HttpVersion.HTTP_2) {
      LOGGER.debug("service center client protocol version is HTTP/2");
      httpClientOptions.setHttp2ClearTextUpgrade(false);
    }
    if (serviceRegistryConfig.isSsl()) {
      LOGGER.debug("service center client performs requests over TLS");
      VertxTLSBuilder.buildHttpClientOptions(ServiceRegistryConfig.SSL_KEY, httpClientOptions);
    }
    return httpClientOptions;
  }
}
