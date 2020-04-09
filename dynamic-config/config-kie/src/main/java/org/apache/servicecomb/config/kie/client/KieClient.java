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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpStatus;
import org.apache.servicecomb.config.kie.archaius.sources.KieConfigurationSourceImpl.UpdateHandler;
import org.apache.servicecomb.config.kie.model.KVResponse;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpClientRequest;

public class KieClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(KieClient.class);

  private ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1, (r) -> {
    Thread thread = new Thread(r);
    thread.setName("org.apache.servicecomb.config.kie");
    thread.setDaemon(true);
    return thread;
  });

  private static final long PULL_REQUEST_TIME_OUT_IN_MILLIS = 10000;

  private static final long LONG_POLLING_REQUEST_TIME_OUT_IN_MILLIS = 60000;

  private static AtomicBoolean IS_FIRST_PULL = new AtomicBoolean(true);

  private static final int LONG_POLLING_WAIT_TIME_IN_SECONDS = 30;

  private static String revision = "0";

  private static final KieConfig KIE_CONFIG = KieConfig.INSTANCE;

  private final int refreshInterval = KIE_CONFIG.getRefreshInterval();

  private final int firstRefreshInterval = KIE_CONFIG.getFirstRefreshInterval();

  private final boolean enableLongPolling = KIE_CONFIG.enableLongPolling();

  private final String serviceUri = KIE_CONFIG.getServerUri();

  public KieClient(UpdateHandler updateHandler) {
    HttpClients.addNewClientPoolManager(new ConfigKieHttpClientOptionsSPI());
    KieWatcher.INSTANCE.setUpdateHandler(updateHandler);
  }

  public void refreshKieConfig() {
    if (enableLongPolling) {
      EXECUTOR.execute(new ConfigRefresh(serviceUri));
    } else {
      EXECUTOR.scheduleWithFixedDelay(new ConfigRefresh(serviceUri), firstRefreshInterval,
          refreshInterval, TimeUnit.MILLISECONDS);
    }
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
        CountDownLatch latch = new CountDownLatch(1);
        refreshConfig(latch);
        latch.await();
      } catch (Throwable e) {
        LOGGER.error("client refresh thread exception ", e);
      }
      if (enableLongPolling) {
        EXECUTOR.execute(this);
      }
    }

    @SuppressWarnings("deprecation")
    void refreshConfig(CountDownLatch latch) {
      String path = "/v1/"
          + KieConfig.INSTANCE.getDomainName()
          + "/kie/kv?label=app:"
          + KieConfig.INSTANCE.getAppName()
          + "&revision=" + revision;
      long timeout;
      if (enableLongPolling && !IS_FIRST_PULL.get()) {
        path += "&wait=" + LONG_POLLING_WAIT_TIME_IN_SECONDS + "s";
        timeout = LONG_POLLING_REQUEST_TIME_OUT_IN_MILLIS;
      } else {
        IS_FIRST_PULL.compareAndSet(true, false);
        timeout = PULL_REQUEST_TIME_OUT_IN_MILLIS;
      }
      String finalPath = path;
      HttpClients.getClient(ConfigKieHttpClientOptionsSPI.CLIENT_NAME).runOnContext(client -> {
        IpPort ipPort = NetUtils.parseIpPortFromURI(serviceUri);
        HttpClientRequest request = client
            .get(ipPort.getPort(), ipPort.getHostOrIp(), finalPath, rsp -> {
              if (rsp.statusCode() == HttpStatus.SC_OK) {
                revision = rsp.getHeader("X-Kie-Revision");
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
                  latch.countDown();
                });
              } else if (rsp.statusCode() == HttpStatus.SC_NOT_MODIFIED) {
                EventManager.post(new ConnSuccEvent());
                latch.countDown();
              } else {
                EventManager.post(new ConnFailEvent("fetch config fail"));
                LOGGER.error("Config update from {} failed. Error code is {}, error message is [{}].",
                    serviceUri,
                    rsp.statusCode(),
                    rsp.statusMessage());
                latch.countDown();
              }
            }).setTimeout(timeout);

        request.exceptionHandler(e -> {
          EventManager.post(new ConnFailEvent("fetch config fail"));
          LOGGER.error("Config update from {} failed. Error message is [{}].",
              serviceUri,
              e.getMessage());
          latch.countDown();
        });
        request.end();
      });
    }
  }
}
