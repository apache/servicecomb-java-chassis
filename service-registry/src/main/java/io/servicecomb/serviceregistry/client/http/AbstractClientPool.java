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

import io.servicecomb.foundation.ssl.SSLCustom;
import io.servicecomb.foundation.ssl.SSLOption;
import io.servicecomb.foundation.ssl.SSLOptionFactory;
import io.servicecomb.foundation.vertx.VertxTLSBuilder;
import io.servicecomb.foundation.vertx.VertxUtils;
import io.servicecomb.foundation.vertx.client.ClientPoolManager;
import io.servicecomb.foundation.vertx.client.http.HttpClientVerticle;
import io.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;

/**
 * Created by  on 2017/4/28.
 */
public abstract class AbstractClientPool implements ClientPool {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientPool.class);

  private ClientPoolManager<HttpClientWithContext> clientMgr = new ClientPoolManager<>();

  private static final String SSL_KEY = "sc.consumer";

  public AbstractClientPool() {
    create();
  }

  public HttpClientWithContext getClient() {
    return this.clientMgr.findThreadBindClientPool();
  }

  public void create() {
    // 这里面是同步接口，且好像直接在事件线程中用，保险起见，先使用独立的vertx实例
    Vertx vertx = VertxUtils.getOrCreateVertxByName("registry", null);
    HttpClientOptions httpClientOptions = createHttpClientOptions();
    DeploymentOptions deployOptions =
        VertxUtils.createClientDeployOptions(this.clientMgr,
            ServiceRegistryConfig.INSTANCE.getWorkerPoolSize(),
            1,
            httpClientOptions);
    try {
      VertxUtils.blockDeploy(vertx, HttpClientVerticle.class, deployOptions);
    } catch (InterruptedException e) {
      LOGGER.error("deploy a registry verticle failed, {}", e.getMessage());
    }
  }

  protected void buildSecureClientOptions(HttpClientOptions httpClientOptions) {
    SSLOptionFactory factory =
        SSLOptionFactory.createSSLOptionFactory(SSL_KEY, null);
    SSLOption sslOption;
    if (factory == null) {
      sslOption = SSLOption.buildFromYaml(SSL_KEY);
    } else {
      sslOption = factory.createSSLOption();
    }
    SSLCustom sslCustom = SSLCustom.createSSLCustom(sslOption.getSslCustomClass());
    VertxTLSBuilder.buildHttpClientOptions(sslOption, sslCustom, httpClientOptions);
  }
}
