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
import org.apache.servicecomb.foundation.common.utils.RSAUtils;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.cache.MicroserviceInstanceCache;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class RSAProviderTokenManager {

  private final static Logger LOGGER = LoggerFactory.getLogger(RSAProviderTokenManager.class);

  private static Cache<RSAAuthenticationToken, Boolean> validatedToken = CacheBuilder.newBuilder()
      .expireAfterAccess(getExpiredTime(), TimeUnit.MILLISECONDS)
      .build();

  private AccessController accessController = new AccessController();

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

      if (getValidatedToken().asMap().containsKey(rsaToken)) {
        return accessController.isAllowed(MicroserviceInstanceCache.getOrCreate(rsaToken.getServiceId()));
      }

      if (isValidToken(rsaToken) && !tokenExpired(rsaToken)) {
        getValidatedToken().put(rsaToken, true);
        return accessController.isAllowed(MicroserviceInstanceCache.getOrCreate(rsaToken.getServiceId()));
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
    String publicKey = getPublicKey(rsaToken.getInstanceId(), rsaToken.getServiceId());
    return RSAUtils.verify(publicKey, sign, content);
  }

  public static int getExpiredTime() {
    return 60 * 60 * 1000;
  }

  private boolean tokenExpired(RSAAuthenticationToken rsaToken) {
    long generateTime = rsaToken.getGenerateTime();
    long expired = generateTime + RSAAuthenticationToken.TOKEN_ACTIVE_TIME + 15 * 60 * 1000;
    long now = System.currentTimeMillis();
    return now > expired;
  }

  private String getPublicKey(String instanceId, String serviceId) {
    MicroserviceInstance instances = MicroserviceInstanceCache.getOrCreate(serviceId, instanceId);
    if (instances != null) {
      return instances.getProperties().get(DefinitionConst.INSTANCE_PUBKEY_PRO);
    } else {
      LOGGER.error("not instance found {}-{}, maybe attack", instanceId, serviceId);
      return "";
    }
  }

  public static Cache<RSAAuthenticationToken, Boolean> getValidatedToken() {
    return validatedToken;
  }
}
