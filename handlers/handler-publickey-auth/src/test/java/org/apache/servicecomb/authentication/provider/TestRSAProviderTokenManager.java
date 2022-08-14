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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.authentication.RSAAuthenticationToken;
import org.apache.servicecomb.authentication.consumer.RSAConsumerTokenManager;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.utils.RSAKeyPairEntry;
import org.apache.servicecomb.foundation.common.utils.RSAUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.token.RSAKeypair4Auth;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.cache.MicroserviceInstanceCache;
import org.apache.servicecomb.registry.definition.DefinitionConst;

import com.google.common.cache.Cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class TestRSAProviderTokenManager {


  @BeforeEach
  public void setUp() {
    ConfigUtil.installDynamicConfig();
  }

  @AfterEach
  public void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testTokenExpired() {
    String tokenStr =
        "e8a04b54cf2711e7b701286ed488fc20@c8636e5acf1f11e7b701286ed488fc20@1511315597475@9t0tp8ce80SUM5ts6iRGjFJMvCdQ7uvhpyh0RM7smKm3p4wYOrojr4oT1Pnwx7xwgcgEFbQdwPJxIMfivpQ1rHGqiLp67cjACvJ3Ke39pmeAVhybsLADfid6oSjscFaJ@WBYouF6hXYrXzBA31HC3VX8Bw9PNgJUtVqOPAaeW9ye3q/D7WWb0M+XMouBIWxWY6v9Un1dGu5Rkjlx6gZbnlHkb2VO8qFR3Y6lppooWCirzpvEBRjlJQu8LPBur0BCfYGq8XYrEZA2NU6sg2zXieqCSiX6BnMnBHNn4cR9iZpk=";
    RSAProviderTokenManager tokenManager = new RSAProviderTokenManager();
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    Map<String, String> properties = new HashMap<>();
    microserviceInstance.setProperties(properties);
    properties.put(DefinitionConst.INSTANCE_PUBKEY_PRO,
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxKl5TNUTec7fL2degQcCk6vKf3c0wsfNK5V6elKzjWxm0MwbRj/UeR20VSnicBmVIOWrBS9LiERPPvjmmWUOSS2vxwr5XfhBhZ07gCAUNxBOTzgMo5nE45DhhZu5Jzt5qSV6o10Kq7+fCCBlDZ1UoWxZceHkUt5AxcrhEDulFjQIDAQAB");
    Assertions.assertFalse(tokenManager.valid(tokenStr));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTokenExpiredRemoveInstance() throws Exception {

    String tokenStr =
        "e8a04b54cf2711e7b701286ed488fc20@c8636e5acf1f11e7b701286ed488fc20@1511315597475@9t0tp8ce80SUM5ts6iRGjFJMvCdQ7uvhpyh0RM7smKm3p4wYOrojr4oT1Pnwx7xwgcgEFbQdwPJxIMfivpQ1rHGqiLp67cjACvJ3Ke39pmeAVhybsLADfid6oSjscFaJ@WBYouF6hXYrXzBA31HC3VX8Bw9PNgJUtVqOPAaeW9ye3q/D7WWb0M+XMouBIWxWY6v9Un1dGu5Rkjlx6gZbnlHkb2VO8qFR3Y6lppooWCirzpvEBRjlJQu8LPBur0BCfYGq8XYrEZA2NU6sg2zXieqCSiX6BnMnBHNn4cR9iZpk=";
    RSAAuthenticationToken token = Mockito.spy(RSAAuthenticationToken.fromStr(tokenStr));
    RSAProviderTokenManager tokenManager = Mockito.spy(new RSAProviderTokenManager() {
      @Override
      protected int getExpiredTime() {
        return 500;
      }
    });

    try (MockedStatic<RSAAuthenticationToken> rsaAuthenticationTokenMockedStatic = Mockito.mockStatic(RSAAuthenticationToken.class)) {
      rsaAuthenticationTokenMockedStatic.when(() -> RSAAuthenticationToken.fromStr(tokenStr)).thenReturn(token);
      Mockito.when(token.getGenerateTime()).thenReturn(System.currentTimeMillis());
      Mockito.doReturn(true).when(tokenManager).isValidToken(token);
      Assertions.assertTrue(tokenManager.valid(tokenStr));

      Cache<RSAAuthenticationToken, Boolean> cache = tokenManager
              .getValidatedToken();
      Assertions.assertTrue(cache.asMap().containsKey(token));

      Thread.sleep(1000);
      Assertions.assertFalse(cache.asMap().containsKey(token));
    }
  }

  @Test
  public void testTokenFromValidatePool() {
    RSAKeyPairEntry rsaKeyPairEntry = RSAUtils.generateRSAKeyPair();
    RSAKeypair4Auth.INSTANCE.setPrivateKey(rsaKeyPairEntry.getPrivateKey());
    RSAKeypair4Auth.INSTANCE.setPublicKey(rsaKeyPairEntry.getPublicKey());
    RSAKeypair4Auth.INSTANCE.setPublicKeyEncoded(rsaKeyPairEntry.getPublicKeyEncoded());
    String serviceId = "c8636e5acf1f11e7b701286ed488fc20";
    String instanceId = "e8a04b54cf2711e7b701286ed488fc20";
    RSAConsumerTokenManager rsaConsumerTokenManager = new RSAConsumerTokenManager();
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setInstanceId(instanceId);
    Map<String, String> properties = new HashMap<>();
    microserviceInstance.setProperties(properties);
    properties.put(DefinitionConst.INSTANCE_PUBKEY_PRO, rsaKeyPairEntry.getPublicKeyEncoded());
    Microservice microservice = new Microservice();
    microservice.setServiceId(serviceId);
    RegistrationManager.INSTANCE = Mockito.spy(RegistrationManager.INSTANCE);
    Mockito.when(RegistrationManager.INSTANCE.getMicroservice()).thenReturn(microservice);
    Mockito.when(RegistrationManager.INSTANCE.getMicroserviceInstance()).thenReturn(microserviceInstance);
    //Test Consumer first create token
    String token = rsaConsumerTokenManager.getToken();
    Assertions.assertNotNull(token);
    // use cache token
    Assertions.assertEquals(token, rsaConsumerTokenManager.getToken());
    try (MockedStatic<MicroserviceInstanceCache> microserviceInstanceCacheMockedStatic = Mockito.mockStatic(MicroserviceInstanceCache.class)) {
      microserviceInstanceCacheMockedStatic.when(() -> MicroserviceInstanceCache.getOrCreate(serviceId, instanceId))
              .thenReturn(microserviceInstance);
      microserviceInstanceCacheMockedStatic.when(() -> MicroserviceInstanceCache.getOrCreate(serviceId))
              .thenReturn(microservice);
      RSAProviderTokenManager rsaProviderTokenManager = new RSAProviderTokenManager();
      //first validate need to verify use RSA
      Assertions.assertTrue(rsaProviderTokenManager.valid(token));
      // second validate use validated pool
      Assertions.assertTrue(rsaProviderTokenManager.valid(token));
    }
  }
}
