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
package org.apache.servicecomb.authentication.provider;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.authentication.RSAAuthenticationToken;
import org.apache.servicecomb.foundation.common.utils.KeyPairUtils;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.registry.discovery.MicroserviceInstanceCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class ProviderTokenManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProviderTokenManager.class);

  private final Cache<RSAAuthenticationToken, Boolean> validatedToken = CacheBuilder.newBuilder()
      .expireAfterAccess(getExpiredTime(), TimeUnit.MILLISECONDS)
      .build();

  private AccessController accessController;

  private MicroserviceInstanceCache microserviceInstanceCache;

  @Autowired
  public void setMicroserviceInstanceCache(MicroserviceInstanceCache microserviceInstanceCache) {
    this.microserviceInstanceCache = microserviceInstanceCache;
  }

  @Autowired
  public void setAccessController(AccessController accessController) {
    this.accessController = accessController;
  }

  public boolean valid(String token) {
    try {
      RSAAuthenticationToken rsaToken = RSAAuthenticationToken.fromStr(token);
      if (null == rsaToken) {
        LOGGER.error("token format is error, perhaps you need to set auth handler at consumer");
        return false;
      }
      if (tokenExpired(rsaToken)) {
        LOGGER.error("token is expired");
        return false;
      }

      if (validatedToken.asMap().containsKey(rsaToken)) {
        return accessController.isAllowed(microserviceInstanceCache.getOrCreate(
            rsaToken.getServiceId(), rsaToken.getInstanceId()));
      }

      if (isValidToken(rsaToken) && !tokenExpired(rsaToken)) {
        validatedToken.put(rsaToken, true);
        return accessController.isAllowed(microserviceInstanceCache.getOrCreate(
            rsaToken.getServiceId(), rsaToken.getInstanceId()));
      }
      return false;
    } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
      LOGGER.error("verify error", e);
      return false;
    }
  }

  public boolean isValidToken(RSAAuthenticationToken rsaToken)
      throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    String sign = rsaToken.getSign();
    String content = rsaToken.plainToken();
    String publicKey = getPublicKeyFromInstance(rsaToken.getInstanceId(), rsaToken.getServiceId());
    return KeyPairUtils.verify(publicKey, sign, content);
  }

  protected int getExpiredTime() {
    return 60 * 60 * 1000;
  }

  private boolean tokenExpired(RSAAuthenticationToken rsaToken) {
    long generateTime = rsaToken.getGenerateTime();
    long expired = generateTime + RSAAuthenticationToken.TOKEN_ACTIVE_TIME + 15 * 60 * 1000;
    long now = System.currentTimeMillis();
    return now > expired;
  }

  private String getPublicKeyFromInstance(String instanceId, String serviceId) {
    DiscoveryInstance instances = microserviceInstanceCache.getOrCreate(serviceId, instanceId);
    if (instances != null) {
      return instances.getProperties().get(DefinitionConst.INSTANCE_PUBKEY_PRO);
    } else {
      LOGGER.error("not instance found {}-{}, maybe attack", instanceId, serviceId);
      return "";
    }
  }

  @VisibleForTesting
  Cache<RSAAuthenticationToken, Boolean> getValidatedToken() {
    return validatedToken;
  }
}
