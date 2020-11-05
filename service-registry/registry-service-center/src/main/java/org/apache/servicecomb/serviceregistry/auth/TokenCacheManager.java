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

import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.auth.Cipher;
import org.apache.servicecomb.foundation.common.concurrency.SuppressedRunnableWrapper;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.TimeUtils;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.model.RbacTokenRequest;
import org.apache.servicecomb.service.center.client.model.RbacTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TokenCacheManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenCacheManager.class);

  private static final TokenCacheManager INSTANCE = new TokenCacheManager();

  private Clock clock = TimeUtils.getSystemDefaultZoneClock();

  private ScheduledExecutorService tokenCacheWorker;

  private Map<String, TokenCache> tokenCacheMap;

  private Map<String, ServiceCenterClient> serviceCenterClients;

  public static TokenCacheManager getInstance() {
    return INSTANCE;
  }

  private TokenCacheManager() {
    tokenCacheWorker = Executors.newScheduledThreadPool(2, new ThreadFactory() {
      private final AtomicInteger threadIndexer = new AtomicInteger();

      @Override
      public Thread newThread(@Nonnull Runnable r) {
        Thread thread = new Thread(r, "auth-token-cache-" + threadIndexer.getAndIncrement());
        thread.setDaemon(true);
        return thread;
      }
    });
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

    TokenCache tokenCache = new TokenCache(registryName, accountName, password, cipher, this.clock);
    tokenCache.setTokenCacheWorker(this.tokenCacheWorker);
    tokenCacheMap.put(registryName, tokenCache);
    tokenCache.refreshToken();
  }

  public String getToken(String registryName) {
    return Optional.ofNullable(tokenCacheMap.get(registryName))
        .map(TokenCache::getToken)
        .orElse("");
  }

  public class TokenCache {
    private final String registryName;

    private final String accountName;

    private final String password;

    private final Clock clock;

    private String token;

    private long nextRefreshTime;

    /**
     * The life cycle period of a token, in millisecond.
     * After the {@code tokenLife} time since the token created, it should be refreshed.
     * <p>
     * Default life time in sc is 30min, give 2min buffer
     * </p>
     */
    private long tokenLife = TimeUnit.MINUTES.toMillis(30 - 2);

    private ScheduledExecutorService tokenCacheWorker;

    private Cipher cipher;

    public TokenCache(String registryName, String accountName, String password,
        Cipher cipher, Clock clock) {
      this.registryName = registryName;
      this.accountName = accountName;
      this.password = password;
      this.cipher = cipher;
      this.clock = clock;
    }

    public String getToken() {
      return token == null ? "" : token;
    }

    public void setTokenCacheWorker(ScheduledExecutorService tokenCacheWorker) {
      Objects.requireNonNull(tokenCacheWorker, "input tokenCacheWorker is null");
      if (this.tokenCacheWorker != null) {
        throw new IllegalStateException("tokenCacheWorker already set!");
      }

      this.tokenCacheWorker = tokenCacheWorker;
      startTokenRefreshTask();
    }

    private void startTokenRefreshTask() {
      this.tokenCacheWorker.scheduleAtFixedRate(
          new SuppressedRunnableWrapper(() -> {
            if (isTokenOutdated()) {
              refreshToken();
            }
          }),
          1,
          5,
          TimeUnit.SECONDS);
    }

    private boolean isTokenOutdated() {
      return clock.millis() > nextRefreshTime;
    }

    private void refreshToken() {
      ServiceCenterClient serviceCenterClient = serviceCenterClients.get(this.registryName);

      RbacTokenRequest request = new RbacTokenRequest();
      request.setName(new String(cipher.decrypt(accountName.toCharArray())));
      request.setPassword(new String(cipher.decrypt(password.toCharArray())));
      RbacTokenResponse rbacTokenResponse = serviceCenterClient.queryToken(request);
      LOGGER.info("refresh token successfully {}", rbacTokenResponse.getStatusCode());
      if (StringUtils.isEmpty(this.token)) {
        if (Status.UNAUTHORIZED.getStatusCode() == rbacTokenResponse.getStatusCode()) {
          // password wrong, do not try anymore
          LOGGER.warn("username or password may be wrong!");
          this.tokenCacheWorker.shutdown();
        } else if (Status.NOT_FOUND.getStatusCode() == rbacTokenResponse.getStatusCode()) {
          // service center not support, do not try
          LOGGER.warn("service center do not support rbac token, you should not config account info");
          this.tokenCacheWorker.shutdown();
        }
      }
      this.token = rbacTokenResponse.getToken();
      this.nextRefreshTime = clock.millis() + tokenLife;
    }
  }
}
