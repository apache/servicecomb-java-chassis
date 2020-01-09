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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import org.apache.servicecomb.config.kie.archaius.sources.KieConfigurationSourceImpl.UpdateHandler;
import org.apache.servicecomb.config.kie.model.KVResponse;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.vertx.AddressResolverConfig;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.ClientVerticle;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientPoolFactory;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(KieClient.class);

  private ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);

  private static final long TIME_OUT = 10000;

  private static final KieConfig KIE_CONFIG = KieConfig.INSTANCE;

  private final int refreshInterval = KIE_CONFIG.getRefreshInterval();

  private final int firstRefreshInterval = KIE_CONFIG.getFirstRefreshInterval();

  private final String serviceUri = KIE_CONFIG.getServerUri();

  private ClientPoolManager<HttpClientWithContext> clientMgr;

  public KieClient(UpdateHandler updateHandler) {
    KieWatcher.INSTANCE.setUpdateHandler(updateHandler);
  }

  public void refreshKieConfig() {
    try {
      deployConfigClient();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
    EXECUTOR
        .scheduleWithFixedDelay(new ConfigRefresh(serviceUri), firstRefreshInterval,
            refreshInterval, TimeUnit.SECONDS);
  }

  private void deployConfigClient() throws InterruptedException {
    VertxOptions vertxOptions = new VertxOptions();
    vertxOptions.setAddressResolverOptions(AddressResolverConfig.getAddressResover("kie.consumer",
        KieConfig.getFinalConfig()));
    Vertx vertx = VertxUtils.getOrCreateVertxByName("kie", vertxOptions);

    HttpClientOptions httpClientOptions = new HttpClientOptions();
    clientMgr = new ClientPoolManager<>(vertx, new HttpClientPoolFactory(httpClientOptions));

    DeploymentOptions deployOptions = VertxUtils.createClientDeployOptions(clientMgr, 1);
    VertxUtils.blockDeploy(vertx, ClientVerticle.class, deployOptions);
  }

  public void destroy() {
    if (EXECUTOR != null) {
      EXECUTOR.shutdown();
      EXECUTOR = null;
    }
  }

  class ConfigRefresh implements Runnable {

    private final String serviceUri;

    ConfigRefresh(String serviceUris) {
      this.serviceUri = serviceUris;
    }

    @Override
    public void run() {
      try {
        refreshConfig();
      } catch (Throwable e) {
        LOGGER.error("client refresh thread exception ", e);
      }
    }

    //todo : latch down
    @SuppressWarnings("deprecation")
    void refreshConfig() {
      String path = "/v1/"
          + KieConfig.INSTANCE.getDomainName()
          + "/kie/kv?label=app:"
          + KieConfig.INSTANCE.getAppName();
      clientMgr.findThreadBindClientPool().runOnContext(client -> {
        IpPort ipPort = NetUtils.parseIpPortFromURI(serviceUri);
        HttpClientRequest request = client
            .get(ipPort.getPort(), ipPort.getHostOrIp(), path, rsp -> {
              if (rsp.statusCode() == HttpResponseStatus.OK.code()) {
                rsp.bodyHandler(buf -> {
                  try {
                    Map<String, Object> resMap = KieUtil.getConfigByLabel(JsonUtils.OBJ_MAPPER
                        .readValue(buf.toString(), KVResponse.class));
                    KieWatcher.INSTANCE.refreshConfigItems(resMap);
                    EventManager.post(new ConnSuccEvent());
                  } catch (IOException e) {
                    EventManager.post(new ConnFailEvent(
                        "config update result parse fail " + e.getMessage()));
                    LOGGER.error("Config update from {} failed. Error message is [{}].",
                        serviceUri,
                        e.getMessage());
                  }
                });
                // latch.countDown();
              } else if (rsp.statusCode() == HttpStatus.SC_NOT_FOUND) {
                EventManager.post(new ConnSuccEvent());
//                latch.countDown();
              } else {
                EventManager.post(new ConnFailEvent("fetch config fail"));
                LOGGER.error("Config update from {} failed. Error message is [{}].",
                    serviceUri,
                    rsp.statusMessage());
              }
            }).setTimeout(TIME_OUT);
        request.exceptionHandler(e -> {
          EventManager.post(new ConnFailEvent("fetch config fail"));
          LOGGER.error("Config update from {} failed. Error message is [{}].",
              serviceUri,
              e.getMessage());
//          latch.countDown();
        });
        request.end();
      });
    }
  }
}
