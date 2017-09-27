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

package io.servicecomb.transport.rest.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.ssl.SSLCustom;
import io.servicecomb.foundation.ssl.SSLOption;
import io.servicecomb.foundation.ssl.SSLOptionFactory;
import io.servicecomb.foundation.vertx.VertxTLSBuilder;
import io.servicecomb.foundation.vertx.VertxUtils;
import io.servicecomb.foundation.vertx.client.ClientPoolManager;
import io.servicecomb.foundation.vertx.client.http.HttpClientVerticle;
import io.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.transport.rest.client.http.VertxHttpMethod;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;

public final class RestTransportClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestTransportClient.class);

  private static final String SSL_KEY = "rest.consumer";

  private ClientPoolManager<HttpClientWithContext> clientMgr = new ClientPoolManager<>();

  private final boolean sslEnabled;

  public RestTransportClient(boolean sslEnabled) {
    this.sslEnabled = sslEnabled;
  }

  public void init(Vertx vertx) throws Exception {
    HttpClientOptions httpClientOptions = createHttpClientOptions();
    DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientMgr,
        TransportClientConfig.getThreadCount(),
        TransportClientConfig.getConnectionPoolPerThread(),
        httpClientOptions);
    VertxUtils.blockDeploy(vertx, HttpClientVerticle.class, deployOptions);
  }

  private HttpClientOptions createHttpClientOptions() {
    HttpClientOptions httpClientOptions = new HttpClientOptions();

    if (this.sslEnabled) {
      SSLOptionFactory factory =
          SSLOptionFactory.createSSLOptionFactory(SSL_KEY,
              null);
      SSLOption sslOption;
      if (factory == null) {
        sslOption = SSLOption.buildFromYaml(SSL_KEY);
      } else {
        sslOption = factory.createSSLOption();
      }
      SSLCustom sslCustom = SSLCustom.createSSLCustom(sslOption.getSslCustomClass());
      VertxTLSBuilder.buildHttpClientOptions(sslOption, sslCustom, httpClientOptions);
    }
    return httpClientOptions;
  }

  public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    HttpClientWithContext httpClientWithContext = clientMgr.findThreadBindClientPool();
    try {
      VertxHttpMethod.INSTANCE.doMethod(httpClientWithContext, invocation, asyncResp);
    } catch (Exception e) {
      asyncResp.fail(invocation.getInvocationType(), e);
      LOGGER.error("vertx rest transport send error.", e);
    }
  }
}
