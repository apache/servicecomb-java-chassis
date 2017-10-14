/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.client.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;

/**
 * Created by on 2017/4/28.
 */
public final class WebsocketClientPool extends AbstractClientPool {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketClientPool.class);

  public static final WebsocketClientPool INSTANCE = new WebsocketClientPool();

  private WebsocketClientPool() {
  }

  @Override
  public HttpClientOptions createHttpClientOptions() {
    HttpVersion ver = ServiceRegistryConfig.INSTANCE.getHttpVersion();
    HttpClientOptions httpClientOptions = new HttpClientOptions();
    httpClientOptions.setProtocolVersion(ver);
    httpClientOptions.setConnectTimeout(ServiceRegistryConfig.INSTANCE.getConnectionTimeout());
    httpClientOptions.setIdleTimeout(ServiceRegistryConfig.INSTANCE.getIdleWatchTimeout());
    if (ver == HttpVersion.HTTP_2) {
      LOGGER.debug("service center ws client protocol version is HTTP/2");
      httpClientOptions.setHttp2ClearTextUpgrade(false);
    }
    if (ServiceRegistryConfig.INSTANCE.isSsl()) {
      LOGGER.debug("service center ws client performs requests over TLS");
      buildSecureClientOptions(httpClientOptions);
    }
    return httpClientOptions;
  }
}
