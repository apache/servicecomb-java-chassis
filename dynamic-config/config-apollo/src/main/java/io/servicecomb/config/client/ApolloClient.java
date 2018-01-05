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

package io.servicecomb.config.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.servicecomb.config.archaius.sources.ApolloConfigurationSourceImpl.UpdateHandler;
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.foundation.vertx.client.ClientPoolManager;
import io.servicecomb.foundation.vertx.client.http.HttpClientWithContext;

public class ApolloClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApolloClient.class);

  private static final ApolloConfig APOLLO_CONFIG = ApolloConfig.INSTANCE;

  public static final Map<String, Object> originalConfigMap = new HashMap<>();

  private static ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);

  private int refreshInterval = APOLLO_CONFIG.getRefreshInterval();

  private int firstRefreshInterval = APOLLO_CONFIG.getFirstRefreshInterval();

  private String serviceUri = APOLLO_CONFIG.getServerUri();

  private String serviceName = APOLLO_CONFIG.getServiceName();

  private String token = APOLLO_CONFIG.getToken();

  private String env = APOLLO_CONFIG.getEnv();

  private String clusters = APOLLO_CONFIG.getServerClusters();

  private String namespace = APOLLO_CONFIG.getNamespace();

  private UpdateHandler updateHandler;

  private static ClientPoolManager<HttpClientWithContext> clientMgr = new ClientPoolManager<>();

  public ApolloClient(UpdateHandler updateHandler) {
    this.updateHandler = updateHandler;
  }

  public void refreshApolloConfig() {
    EXECUTOR
        .scheduleWithFixedDelay(new ConfigRefresh(serviceUri), firstRefreshInterval, refreshInterval, TimeUnit.SECONDS);
  }

  class ConfigRefresh implements Runnable {
    private String serviceUri;

    public ConfigRefresh(String serviceUris) {
      this.serviceUri = serviceUris;
    }

    @Override
    public void run() {
      try {
        refreshConfig();
      } catch (Exception e) {
        LOGGER.error("client refresh thread exception", e);
      }
    }

    public void refreshConfig() {
      RestTemplate rest = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/json;charset=UTF-8");
      headers.add("Authorization", token);
      HttpEntity<String> entity = new HttpEntity<String>(headers);
      ResponseEntity<String> exchange = rest.exchange(composeAPI(), HttpMethod.GET, entity, String.class);
      if (HttpResponseStatus.OK.code() == exchange.getStatusCode().value()) {
        try {
          Map<String, Object> body = JsonUtils.OBJ_MAPPER.readValue(exchange.getBody(),
              new TypeReference<Map<String, Object>>() {
              });
          refreshConfigItems((Map<String, Object>) body.get("configurations"));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    private String composeAPI() {
      String api = serviceUri + "/openapi/v1/envs/" + env +
          "/apps/" + serviceName +
          "/clusters/" + clusters +
          "/namespaces/" + namespace +
          "/releases/latest";
      return api;
    }

    private void refreshConfigItems(Map<String, Object> map) {
      compareChangedConfig(originalConfigMap, map);
      originalConfigMap.clear();
      originalConfigMap.putAll(map);
    }

    private void compareChangedConfig(Map<String, Object> before, Map<String, Object> after) {
      Map<String, Object> itemsCreated = new HashMap<>();
      Map<String, Object> itemsDeleted = new HashMap<>();
      Map<String, Object> itemsModified = new HashMap<>();
      if (before == null || before.isEmpty()) {
        updateHandler.handle("create", after);
        return;
      }
      if (after == null || after.isEmpty()) {
        updateHandler.handle("delete", before);
        return;
      }
      for (String itemKey : after.keySet()) {
        if (!before.containsKey(itemKey)) {
          itemsCreated.put(itemKey, after.get(itemKey));
        } else if (!after.get(itemKey).equals(before.get(itemKey))) {
          itemsModified.put(itemKey, after.get(itemKey));
        }
      }
      for (String itemKey : before.keySet()) {
        if (!after.containsKey(itemKey)) {
          itemsDeleted.put(itemKey, "");
        }
      }
      updateHandler.handle("create", itemsCreated);
      updateHandler.handle("set", itemsModified);
      updateHandler.handle("delete", itemsDeleted);
    }
  }
}

