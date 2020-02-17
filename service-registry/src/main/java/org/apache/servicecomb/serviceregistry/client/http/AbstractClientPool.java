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

import org.apache.servicecomb.foundation.vertx.AddressResolverConfig;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.ClientVerticle;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientPoolFactory;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;

/**
 * Created by  on 2017/4/28.
 */
abstract class AbstractClientPool implements ClientPool {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientPool.class);

  private ServiceRegistryConfig serviceRegistryConfig;

  private HttpClientOptions httpClientOptions;

  private ClientPoolManager<HttpClientWithContext> clientMgr;

  AbstractClientPool(ServiceRegistryConfig serviceRegistryConfig) {
    this.serviceRegistryConfig = serviceRegistryConfig;
    this.httpClientOptions = getHttpClientOptionsFromConfigurations(serviceRegistryConfig);
    create();
  }

  protected abstract boolean isWorker();

  protected abstract HttpClientOptions getHttpClientOptionsFromConfigurations(
      ServiceRegistryConfig serviceRegistryConfig);

  public HttpClientWithContext getClient() {
    return this.clientMgr.findThreadBindClientPool();
  }

  public void create() {
    DynamicIntProperty property = DynamicPropertyFactory.getInstance()
        .getIntProperty(ServiceRegistryConfig.EVENT_LOOP_POOL_SIZE, 4);
    DynamicIntProperty workerPoolSize = DynamicPropertyFactory.getInstance()
        .getIntProperty(ServiceRegistryConfig.WORKER_POOL_SIZE, 4);

    // 这里面是同步接口，且好像直接在事件线程中用，保险起见，先使用独立的vertx实例
    VertxOptions vertxOptions = new VertxOptions()
        .setAddressResolverOptions(AddressResolverConfig.getAddressResover(serviceRegistryConfig.getSslConfigTag()))
        .setEventLoopPoolSize(property.get());
    Vertx vertx = VertxUtils.getOrCreateVertxByName("registry", vertxOptions);
    clientMgr = new ClientPoolManager<>(vertx, new HttpClientPoolFactory(httpClientOptions));

    DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(this.clientMgr,
        ServiceRegistryConfig.INSTANCE.getInstances())
        .setWorker(isWorker())
        .setWorkerPoolName(ServiceRegistryConfig.WORKER_POOL_NAME)
        .setWorkerPoolSize(workerPoolSize.get());
    try {
      VertxUtils.blockDeploy(vertx, ClientVerticle.class, deployOptions);
    } catch (InterruptedException e) {
      LOGGER.error("deploy a registry verticle failed, {}", e.getMessage());
    }
  }
}
