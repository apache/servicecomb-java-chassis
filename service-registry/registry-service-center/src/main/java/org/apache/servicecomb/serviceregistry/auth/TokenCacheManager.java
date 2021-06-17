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

package org.apache.servicecomb.serviceregistry.auth;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.auth.Cipher;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.model.RbacTokenRequest;
import org.apache.servicecomb.service.center.client.model.RbacTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public final class TokenCacheManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenCacheManager.class);

  // special token used for special conditions
  // e.g. un-authorized: will query token after token expired period
  // e.g. not found:  will query token after token expired period
  public static final String INVALID_TOKEN = "invalid";

  private static final TokenCacheManager INSTANCE = new TokenCacheManager();


  private Map<String, TokenCache> tokenCacheMap;

  private Map<String, ServiceCenterClient> serviceCenterClients;

  public static TokenCacheManager getInstance() {
    return INSTANCE;
  }

  private TokenCacheManager() {
    tokenCacheMap = new ConcurrentHashMapEx<>();
  }

  public void setServiceCenterClients(Map<String, ServiceCenterClient> serviceCenterClients) {
    this.serviceCenterClients = serviceCenterClients;
  }

  public void addTokenCache(String registryName, String accountName, String password, Cipher cipher) {
    Objects.requireNonNull(registryName, "registryName should not be null!");
    if (tokenCacheMap.containsKey(registryName)) {
      LOGGER.warn("duplicate token cache registration for serviceRegistry[{}]", registryName);
      return;
    }

    tokenCacheMap.put(registryName, new TokenCache(registryName, accountName, password, cipher));
  }

  public String getToken(String registryName) {
    return Optional.ofNullable(tokenCacheMap.get(registryName))
        .map(TokenCache::getToken)
        .orElse(null);
  }

  public class TokenCache {
    private static final long TOKEN_REFRESH_TIME_IN_SECONDS = 20 * 60 * 1000;

    private final String registryName;

    private final String accountName;

    private final String password;

    private ExecutorService executorService;

    private LoadingCache<String, String> cache;

    private Cipher cipher;

    public TokenCache(String registryName, String accountName, String password,
        Cipher cipher) {
      this.registryName = registryName;
      this.accountName = accountName;
      this.password = password;
      this.cipher = cipher;

      if (enabled()) {
        executorService = Executors.newFixedThreadPool(1, t -> new Thread(t, "rbac-executor-" + this.registryName));
        cache = CacheBuilder.newBuilder()
            .maximumSize(1)
            .refreshAfterWrite(refreshTime(), TimeUnit.MILLISECONDS)
            .build(new CacheLoader<String, String>() {
              @Override
              public String load(String key) throws Exception {
                return createHeaders();
              }

              @Override
              public ListenableFuture<String> reload(String key, String oldValue) throws Exception {
                return Futures.submit(() -> createHeaders(), executorService);
              }
            });
      }
    }

    private String createHeaders() {
      LOGGER.info("start to create RBAC headers");

      ServiceCenterClient serviceCenterClient = serviceCenterClients.get(this.registryName);

      RbacTokenRequest request = new RbacTokenRequest();
      request.setName(new String(cipher.decrypt(accountName.toCharArray())));
      request.setPassword(new String(cipher.decrypt(password.toCharArray())));

      RbacTokenResponse rbacTokenResponse = serviceCenterClient.queryToken(request);

      if (Status.UNAUTHORIZED.getStatusCode() == rbacTokenResponse.getStatusCode()
          || Status.FORBIDDEN.getStatusCode() == rbacTokenResponse.getStatusCode()) {
        // password wrong, do not try anymore
        LOGGER.warn("username or password may be wrong, stop trying to query tokens.");
        return INVALID_TOKEN;
      } else if (Status.NOT_FOUND.getStatusCode() == rbacTokenResponse.getStatusCode()) {
        // service center not support, do not try
        LOGGER.warn("service center do not support RBAC token, you should not config account info");
        return INVALID_TOKEN;
      }

      LOGGER.info("refresh token successfully {}", rbacTokenResponse.getStatusCode());
      return rbacTokenResponse.getToken();
    }

    protected long refreshTime() {
      return TOKEN_REFRESH_TIME_IN_SECONDS;
    }

    public String getToken() {
      if (!enabled()) {
        return null;
      }

      try {
        return cache.get(registryName);
      } catch (Exception e) {
        LOGGER.error("failed to create token", e);
        return null;
      }
    }

    private boolean enabled() {
      return !StringUtils.isEmpty(this.accountName)
          && !StringUtils.isEmpty(this.password);
    }
  }
}
