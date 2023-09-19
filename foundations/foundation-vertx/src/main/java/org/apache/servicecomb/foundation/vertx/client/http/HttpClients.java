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

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.AddressResolverConfig;
import org.apache.servicecomb.foundation.vertx.SharedVertxFactory;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.ClientVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;

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

  /* destroy at shutdown. */
  public static void destroy() {
    httpClients.clear();
    List<HttpClientOptionsSPI> clientOptionsList = SPIServiceUtils.getOrLoadSortedService(HttpClientOptionsSPI.class);
    clientOptionsList.forEach(option -> VertxUtils.blockCloseVertxByName(option.clientName()));
  }

  private static ClientPoolManager<HttpClientWithContext> createClientPoolManager(HttpClientOptionsSPI option) {
    Vertx vertx = getOrCreateVertx(option);
    ClientPoolManager<HttpClientWithContext> clientPoolManager = new ClientPoolManager<>(vertx,
        new HttpClientPoolFactory(HttpClientOptionsSPI.createHttpClientOptions(option)));

    DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientPoolManager,
            option.getInstanceCount())
        .setWorker(option.isWorker())
        .setWorkerPoolName(option.getWorkerPoolName())
        .setWorkerPoolSize(option.getWorkerPoolSize());
    try {
      VertxUtils.blockDeploy(vertx, ClientVerticle.class, deployOptions);
      return clientPoolManager;
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  private static Vertx getOrCreateVertx(HttpClientOptionsSPI option) {
    if (option.useSharedVertx()) {
      return SharedVertxFactory.getSharedVertx(LegacyPropertyFactory.getEnvironment());
    }

    AddressResolverOptions resolverOptions = AddressResolverConfig
        .getAddressResolverOptions(option.getConfigTag());
    VertxOptions vertxOptions = new VertxOptions()
        .setAddressResolverOptions(resolverOptions)
        .setEventLoopPoolSize(option.getEventLoopPoolSize());

    // Maybe we can deploy only one vert.x for the application. However this has did it like this.
    return VertxUtils.getOrCreateVertxByName(option.clientName(), vertxOptions);
  }

  /**
   * get client instance by name
   * @param clientName instance name
   * @return the deployed instance name
   */
  public static HttpClientWithContext getClient(String clientName) {
    return getClient(clientName, true);
  }

  /**
   * get client instance by name
   * @param clientName instance name
   * @param sync reactive or not. false for reactive.
   * @return the deployed instance name
   */
  public static HttpClientWithContext getClient(String clientName, boolean sync) {
    return getClient(clientName, sync, null);
  }

  /**
   * get client instance by name
   * @param clientName instance name
   * @param sync reactive or not. false for reactive.
   * @param targetContext running context
   * @return the deployed instance name
   */
  public static HttpClientWithContext getClient(String clientName, boolean sync, Context targetContext) {
    ClientPoolManager<HttpClientWithContext> poolManager = httpClients.get(clientName);
    if (poolManager == null) {
      LOGGER.error("client name [{}] not exists, should only happen in tests.", clientName);
      return null;
    }
    return poolManager.findClientPool(sync, targetContext);
  }
}
