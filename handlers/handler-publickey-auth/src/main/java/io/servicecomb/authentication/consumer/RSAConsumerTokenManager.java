/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.authentication.consumer;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.authentication.RSAAuthenticationToken;
import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.foundation.token.RSAKeypair4Auth;
import io.servicecomb.serviceregistry.RegistryUtils;

public class RSAConsumerTokenManager {

  private static final Logger logger = LoggerFactory.getLogger(RSAConsumerTokenManager.class);

  private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  private RSAAuthenticationToken token;

  public String getToken() {
    readWriteLock.readLock().lock();
    if (!isExpired(token)) {
      String tokenStr = token.format();
      readWriteLock.readLock().unlock();
      return tokenStr;
    } else {
      readWriteLock.readLock().unlock();
      return createToken();
    }
  }

  public String createToken() {
    PrivateKey privateKey = RSAKeypair4Auth.INSTANCE.getPrivateKey();
    readWriteLock.writeLock().lock();
    if (!isExpired(token)) {
      logger.debug("Token had been recreated by another thread");
      return token.format();
    }
    String instanceId = RegistryUtils.getMicroserviceInstance().getInstanceId();
    String serviceId = RegistryUtils.getMicroservice().getServiceId();
    String randomCode = RandomStringUtils.randomAlphanumeric(128);
    long generateTime = System.currentTimeMillis();
    try {
      String plain = String.format("%s@%s@%s@%s", instanceId, serviceId, generateTime, randomCode);
      String sign = RSAUtils.sign(plain, privateKey);
      token = RSAAuthenticationToken.fromStr(String.format("%s@%s", plain, sign));
      return token.format();
    } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
      logger.error("create token error", e);
      throw new Error("create token error");
    }

  }

  /**
   * the TTL of Token is  24 hours
   * client token will expired 15 minutes early 
   */
  public boolean isExpired(RSAAuthenticationToken token) {
    if (null == token) {
      return true;
    }
    long generateTime = token.getGenerateTime();
    long expiredDate = generateTime + RSAAuthenticationToken.TOKEN_ACTIVE_TIME - 15 * 60 * 1000;
    long now = System.currentTimeMillis();
    return now > expiredDate;
  }


}
