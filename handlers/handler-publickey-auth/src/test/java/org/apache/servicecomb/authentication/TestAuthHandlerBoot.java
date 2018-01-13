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

import org.apache.servicecomb.AuthHandlerBoot;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.foundation.token.RSAKeypair4Auth;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;

public class TestAuthHandlerBoot {


  @Test
  public void testGenerateRSAKey() {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    Microservice microservice = new Microservice();
    microservice.setInstance(microserviceInstance);
    new Expectations(RegistryUtils.class) {
      {

        RegistryUtils.getMicroserviceInstance();
        result = microserviceInstance;
      }
    };

    AuthHandlerBoot authHandlerBoot = new AuthHandlerBoot();
    BootEvent bootEvent = new BootEvent();
    bootEvent.setEventType(BootListener.EventType.BEFORE_REGISTRY);
    authHandlerBoot.onBootEvent(bootEvent);
    Assert.assertNotNull(RSAKeypair4Auth.INSTANCE.getPrivateKey());
    Assert.assertNotNull(RSAKeypair4Auth.INSTANCE.getPublicKey());
  }
}
