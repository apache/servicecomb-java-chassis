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

package org.apache.servicecomb.foundation.vertx.client.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.AddressResolverConfig;
import org.apache.servicecomb.foundation.vertx.SharedVertxFactory;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.ClientVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 *  load and manages a set of HttpClient at boot up.
 */
public class HttpClients {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpClients.class);

  private static final Map<String, ClientPoolManager<HttpClientWithContext>> httpClients = new HashMap<>();

  /* load at boot up, call this method once and only once. */
  public static void load() {
    List<HttpClientOptionsSPI> clientOptionsList = SPIServiceUtils.getOrLoadSortedService(HttpClientOptionsSPI.class);
    clientOptionsList.forEach(option -> {
      if (option.enabled()) {
        ClientPoolManager<HttpClientWithContext> clientPoolManager = httpClients.get(option.clientName());
        if (clientPoolManager != null) {
          LOGGER.warn("client pool {} initialized again.", option.clientName());
        }
        httpClients.put(option.clientName(), createClientPoolManager(option));
      }
    });
  }

  @VisibleForTesting
  public static void mockClientPoolManager(String name, ClientPoolManager<HttpClientWithContext> clientPool) {
    httpClients.put(name, clientPool);
  }

  /* used for configurations module: these module must be boot before other HttpClients is initialized. so can
  *  not load by SPI, must add manually  */
  public static void addNewClientPoolManager(HttpClientOptionsSPI option) {
    httpClients.put(option.clientName(), createClientPoolManager(option));
  }

  /* destroy at shutdown. */
  public static void destroy() {
    httpClients.clear();
    List<HttpClientOptionsSPI> clientOptionsList = SPIServiceUtils.getOrLoadSortedService(HttpClientOptionsSPI.class);
    clientOptionsList.forEach(option -> {
      VertxUtils.blockCloseVertxByName(option.clientName());
    });
  }

  private static ClientPoolManager<HttpClientWithContext> createClientPoolManager(HttpClientOptionsSPI option) {
    Vertx vertx;

    if (option.useSharedVertx()) {
      vertx = SharedVertxFactory.getSharedVertx();
    } else {
      VertxOptions vertxOptions = new VertxOptions()
          .setAddressResolverOptions(AddressResolverConfig.getAddressResover(option.getConfigTag()))
          .setEventLoopPoolSize(option.getEventLoopPoolSize());

      // Maybe we can deploy only one vert.x for the application. However this has did it like this.
      vertx = VertxUtils.getOrCreateVertxByName(option.clientName(), vertxOptions);
    }

    ClientPoolManager<HttpClientWithContext> clientPoolManager = new ClientPoolManager<>(vertx,
        new HttpClientPoolFactory(HttpClientOptionsSPI.createHttpClientOptions(option)));

    DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientPoolManager,
        option.getInstanceCount())
        .setWorker(option.isWorker())
        .setWorkerPoolName(option.getWorkerPoolName())
        .setWorkerPoolSize(option.getWorkerPoolSize());
    try {
      VertxUtils.blockDeploy(vertx, ClientVerticle.class, deployOptions);
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
    return clientPoolManager;
  }

  /**
   * get client instance by name
   * @param clientName instance name
   * @return the deployed instance name
   */
  public static HttpClientWithContext getClient(String clientName) {
    if (httpClients.get(clientName) == null) {
      LOGGER.error("client name [{}] not exists, should only happen in tests.", clientName);
      return null;
    }
    return httpClients.get(clientName).findThreadBindClientPool();
  }

  /**
   * get client instance by name
   * @param clientName instance name
   * @param sync reactive or not. false for reactive.
   * @return the deployed instance name
   */
  public static HttpClientWithContext getClient(String clientName, boolean sync) {
    if (httpClients.get(clientName) == null) {
      LOGGER.error("client name [{}] not exists, should only happen in tests.", clientName);
      return null;
    }
    return httpClients.get(clientName).findClientPool(sync);
  }

  /**
   * get client instance by name
   * @param clientName instance name
   * @param sync reactive or not. false for reactive.
   * @param targetContext running context
   * @return the deployed instance name
   */
  public static HttpClientWithContext getClient(String clientName, boolean sync, Context targetContext) {
    if (httpClients.get(clientName) == null) {
      LOGGER.error("client name [{}] not exists, should only happen in tests.", clientName);
      return null;
    }
    return httpClients.get(clientName).findClientPool(sync, targetContext);
  }
}
