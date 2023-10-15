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

import static org.apache.servicecomb.core.SCBEngine.CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC;
import static org.apache.servicecomb.core.SCBEngine.DEFAULT_TURN_DOWN_STATUS_WAIT_SEC;
import static org.mockito.ArgumentMatchers.any;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.authentication.RSAAuthenticationToken;
import org.apache.servicecomb.authentication.consumer.ConsumerTokenManager;
import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.utils.KeyPairEntry;
import org.apache.servicecomb.foundation.common.utils.KeyPairUtils;
import org.apache.servicecomb.foundation.token.Keypair4Auth;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.registry.discovery.MicroserviceInstanceCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import com.google.common.cache.Cache;

public class TestProviderTokenManager {
  static Environment environment = Mockito.mock(Environment.class);

  @BeforeAll
  public static void setUpClass() {
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.keyGeneratorAlgorithm", "RSA"))
        .thenReturn("RSA");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.signAlgorithm", "SHA256withRSA"))
        .thenReturn("SHA256withRSA");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.keySize", int.class, 2048))
        .thenReturn(2048);
    Mockito.when(environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
        long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)).thenReturn(DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void teardown() {
  }

  @Test
  public void testTokenExpired() {
    String tokenStr =
        "e8a04b54cf2711e7b701286ed488fc20@c8636e5acf1f11e7b701286ed488fc20@1511315597475@9t0tp8ce80SUM5ts6iRGjFJMvCdQ7uvhpyh0RM7smKm3p4wYOrojr4oT1Pnwx7xwgcgEFbQdwPJxIMfivpQ1rHGqiLp67cjACvJ3Ke39pmeAVhybsLADfid6oSjscFaJ@WBYouF6hXYrXzBA31HC3VX8Bw9PNgJUtVqOPAaeW9ye3q/D7WWb0M+XMouBIWxWY6v9Un1dGu5Rkjlx6gZbnlHkb2VO8qFR3Y6lppooWCirzpvEBRjlJQu8LPBur0BCfYGq8XYrEZA2NU6sg2zXieqCSiX6BnMnBHNn4cR9iZpk=";
    ProviderTokenManager tokenManager = new ProviderTokenManager();
    DiscoveryInstance microserviceInstance = Mockito.mock(DiscoveryInstance.class);
    Map<String, String> properties = new HashMap<>();
    Mockito.when(microserviceInstance.getProperties()).thenReturn(properties);
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
    ProviderTokenManager tokenManager = Mockito.spy(new ProviderTokenManager() {
      @Override
      protected int getExpiredTime() {
        return 500;
      }
    });
    ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);
    tokenManager.setAccessController(new AccessController(environment));
    MicroserviceInstanceCache microserviceInstanceCache = Mockito.mock(MicroserviceInstanceCache.class);
    DiscoveryInstance microserviceInstance = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(microserviceInstance.getInstanceId()).thenReturn("");
    Map<String, String> properties = new HashMap<>();
    Mockito.when(microserviceInstance.getProperties()).thenReturn(properties);
    Mockito.when(microserviceInstanceCache.getOrCreate(any(String.class), any(String.class)))
        .thenReturn(microserviceInstance);
    tokenManager.setMicroserviceInstanceCache(microserviceInstanceCache);
    try (MockedStatic<RSAAuthenticationToken> rsaAuthenticationTokenMockedStatic = Mockito.mockStatic(
        RSAAuthenticationToken.class)) {
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
    KeyPairEntry keyPairEntry = KeyPairUtils.generateALGKeyPair();
    Keypair4Auth.INSTANCE.setPrivateKey(keyPairEntry.getPrivateKey());
    Keypair4Auth.INSTANCE.setPublicKey(keyPairEntry.getPublicKey());
    Keypair4Auth.INSTANCE.setPublicKeyEncoded(keyPairEntry.getPublicKeyEncoded());
    String serviceId = "test";
    String instanceId = "test";
    ConsumerTokenManager consumerTokenManager = new ConsumerTokenManager();

    MicroserviceProperties microserviceProperties = Mockito.mock(MicroserviceProperties.class);
    Mockito.when(microserviceProperties.getName()).thenReturn("test");
    Mockito.when(microserviceProperties.getApplication()).thenReturn("test");
    consumerTokenManager.setMicroserviceProperties(microserviceProperties);
    DiscoveryInstance microserviceInstance = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(microserviceInstance.getInstanceId()).thenReturn(instanceId);
    Map<String, String> properties = new HashMap<>();
    Mockito.when(microserviceInstance.getProperties()).thenReturn(properties);
    properties.put(DefinitionConst.INSTANCE_PUBKEY_PRO, keyPairEntry.getPublicKeyEncoded());
    MicroserviceInstanceCache microserviceInstanceCache = Mockito.mock(MicroserviceInstanceCache.class);
    Mockito.when(microserviceInstanceCache.getOrCreate(serviceId, instanceId)).thenReturn(microserviceInstance);
    //Test Consumer first create token
    String token = consumerTokenManager.getToken();
    Assertions.assertNotNull(token);
    // use cache token
    Assertions.assertEquals(token, consumerTokenManager.getToken());
    ProviderTokenManager rsaProviderTokenManager = new ProviderTokenManager();
    ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);
    rsaProviderTokenManager.setAccessController(new AccessController(environment));

    rsaProviderTokenManager.setMicroserviceInstanceCache(microserviceInstanceCache);
    //first validate need to verify use RSA
    Assertions.assertTrue(rsaProviderTokenManager.valid(token));
    // second validate use validated pool
    Assertions.assertTrue(rsaProviderTokenManager.valid(token));
  }
}
