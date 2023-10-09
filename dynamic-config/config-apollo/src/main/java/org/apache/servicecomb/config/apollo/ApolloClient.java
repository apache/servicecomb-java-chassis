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

package org.apache.servicecomb.config.apollo;

import static org.apache.servicecomb.config.apollo.ConfigurationAction.CREATE;
import static org.apache.servicecomb.config.apollo.ConfigurationAction.DELETE;
import static org.apache.servicecomb.config.apollo.ConfigurationAction.SET;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.config.apollo.ApolloDynamicPropertiesSource.UpdateHandler;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ApolloClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApolloClient.class);

  private static final Map<String, Object> originalConfigMap = new ConcurrentHashMap<>();

  private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);

  private final ApolloConfig apolloConfig;

  private final UpdateHandler updateHandler;

  private static RestTemplate rest = new RestTemplate();

  public ApolloClient(UpdateHandler updateHandler, ApolloConfig apolloConfig) {
    this.updateHandler = updateHandler;
    this.apolloConfig = apolloConfig;
  }

  @VisibleForTesting
  static Map<String, Object> getOriginalConfigMap() {
    return originalConfigMap;
  }

  @VisibleForTesting
  static void setRest(RestTemplate rest) {
    ApolloClient.rest = rest;
  }

  public void refreshApolloConfig() {
    EXECUTOR.scheduleWithFixedDelay(new ConfigRefresh(apolloConfig.getServerUri()),
        apolloConfig.getFirstRefreshInterval(),
        apolloConfig.getRefreshInterval(), TimeUnit.SECONDS);
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

    @SuppressWarnings("unchecked")
    void refreshConfig() {
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/json;charset=UTF-8");
      headers.add("Authorization", apolloConfig.getToken());
      HttpEntity<String> entity = new HttpEntity<>(headers);
      ResponseEntity<String> exchange = rest.exchange(composeAPI(), HttpMethod.GET, entity, String.class);
      if (HttpResponseStatus.OK.code() == exchange.getStatusCode().value()) {
        try {
          Map<String, Object> body = JsonUtils.OBJ_MAPPER.readValue(exchange.getBody(),
              new TypeReference<Map<String, Object>>() {
              });
          refreshConfigItems((Map<String, Object>) body.get("configurations"));
        } catch (IOException e) {
          LOGGER.error("JsonObject parse config center response error: ", e);
        }
      } else {
        LOGGER.error("fetch configuration failed, error code:{} for {}",
            exchange.getStatusCode().value(),
            exchange.getBody());
      }
    }

    private String composeAPI() {
      String api = serviceUri + "/openapi/v1/envs/" + apolloConfig.getEnv() +
          "/apps/" + apolloConfig.getServiceName() +
          "/clusters/" + apolloConfig.getServerClusters() +
          "/namespaces/" + apolloConfig.getNamespace() +
          "/releases/latest";
      return api;
    }

    private void refreshConfigItems(Map<String, Object> map) {
      compareChangedConfig(originalConfigMap, map);
      originalConfigMap.clear();
      originalConfigMap.putAll(map);
    }

    void compareChangedConfig(Map<String, Object> before, Map<String, Object> after) {
      Map<String, Object> itemsCreated = new HashMap<>();
      Map<String, Object> itemsDeleted = new HashMap<>();
      Map<String, Object> itemsModified = new HashMap<>();
      if (before == null || before.isEmpty()) {
        updateHandler.handle(CREATE, after);
        return;
      }
      if (after == null || after.isEmpty()) {
        updateHandler.handle(DELETE, before);
        return;
      }
      after.forEach((itemKey, itemValue) -> {
        if (!before.containsKey(itemKey)) {
          itemsCreated.put(itemKey, itemValue);
        } else if (!itemValue.equals(before.get(itemKey))) {
          itemsModified.put(itemKey, itemValue);
        }
      });
      for (String itemKey : before.keySet()) {
        if (!after.containsKey(itemKey)) {
          itemsDeleted.put(itemKey, "");
        }
      }
      updateHandler.handle(CREATE, itemsCreated);
      updateHandler.handle(SET, itemsModified);
      updateHandler.handle(DELETE, itemsDeleted);
    }
  }
}

