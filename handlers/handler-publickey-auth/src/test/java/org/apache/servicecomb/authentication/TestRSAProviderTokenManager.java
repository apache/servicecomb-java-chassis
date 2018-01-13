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
package org.apache.servicecomb.authentication;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.authentication.consumer.RSAConsumerTokenManager;
import org.apache.servicecomb.authentication.provider.RSAProviderTokenManager;
import org.apache.servicecomb.foundation.common.utils.RSAKeyPairEntry;
import org.apache.servicecomb.foundation.common.utils.RSAUtils;
import org.apache.servicecomb.foundation.token.RSAKeypair4Auth;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.MicroserviceInstanceCache;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;

public class TestRSAProviderTokenManager {


  @Test
  public void testTokenExpried() {
    String tokenStr =
        "e8a04b54cf2711e7b701286ed488fc20@c8636e5acf1f11e7b701286ed488fc20@1511315597475@9t0tp8ce80SUM5ts6iRGjFJMvCdQ7uvhpyh0RM7smKm3p4wYOrojr4oT1Pnwx7xwgcgEFbQdwPJxIMfivpQ1rHGqiLp67cjACvJ3Ke39pmeAVhybsLADfid6oSjscFaJ@WBYouF6hXYrXzBA31HC3VX8Bw9PNgJUtVqOPAaeW9ye3q/D7WWb0M+XMouBIWxWY6v9Un1dGu5Rkjlx6gZbnlHkb2VO8qFR3Y6lppooWCirzpvEBRjlJQu8LPBur0BCfYGq8XYrEZA2NU6sg2zXieqCSiX6BnMnBHNn4cR9iZpk=";
    RSAProviderTokenManager tokenManager = new RSAProviderTokenManager();
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    Map<String, String> properties = new HashMap<>();
    microserviceInstance.setProperties(properties);
    properties.put(Const.INSTANCE_PUBKEY_PRO,
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxKl5TNUTec7fL2degQcCk6vKf3c0wsfNK5V6elKzjWxm0MwbRj/UeR20VSnicBmVIOWrBS9LiERPPvjmmWUOSS2vxwr5XfhBhZ07gCAUNxBOTzgMo5nE45DhhZu5Jzt5qSV6o10Kq7+fCCBlDZ1UoWxZceHkUt5AxcrhEDulFjQIDAQAB");
    Assert.assertFalse(tokenManager.valid(tokenStr));
  }

  @Test
  public void testTokenFromVaidatePool() {
    RSAKeyPairEntry rsaKeyPairEntry = RSAUtils.generateRSAKeyPair();
    RSAKeypair4Auth.INSTANCE.setPrivateKey(rsaKeyPairEntry.getPrivateKey());
    RSAKeypair4Auth.INSTANCE.setPublicKey(rsaKeyPairEntry.getPublicKey());
    RSAKeypair4Auth.INSTANCE.setPublicKeyEncoded(rsaKeyPairEntry.getPublicKeyEncoded());
    String serviceId = "c8636e5acf1f11e7b701286ed488fc20";
    String instanceId = "e8a04b54cf2711e7b701286ed488fc20";
    RSAConsumerTokenManager rsaCoumserTokenManager = new RSAConsumerTokenManager();
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setInstanceId(instanceId);
    Map<String, String> properties = new HashMap<>();
    microserviceInstance.setProperties(properties);
    properties.put(Const.INSTANCE_PUBKEY_PRO, rsaKeyPairEntry.getPublicKeyEncoded());
    Microservice microservice = new Microservice();
    microservice.setServiceId(serviceId);
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroservice();
        result = microservice;
        RegistryUtils.getMicroserviceInstance();
        result = microserviceInstance;

      }
    };

    //Test Consumer first create token
    String token = rsaCoumserTokenManager.getToken();
    Assert.assertNotNull(token);
    // use cache token
    Assert.assertEquals(token, rsaCoumserTokenManager.getToken());
    new Expectations(MicroserviceInstanceCache.class) {
      {
        MicroserviceInstanceCache.getOrCreate(serviceId, instanceId);
        result = microserviceInstance;

      }
    };
    RSAProviderTokenManager rsaProviderTokenManager = new RSAProviderTokenManager();
    //first validate need to verify use RSA
    Assert.assertTrue(rsaProviderTokenManager.valid(token));
    // second validate use validated pool
    Assert.assertTrue(rsaProviderTokenManager.valid(token));

  }



}
