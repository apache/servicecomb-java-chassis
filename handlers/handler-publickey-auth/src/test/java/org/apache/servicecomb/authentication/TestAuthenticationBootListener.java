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

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.token.Keypair4Auth;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestAuthenticationBootListener {
  private SCBEngine engine;

  @BeforeEach
  public void setUp() {
    ConfigUtil.installDynamicConfig();
    engine = SCBBootstrap.createSCBEngineForTest().run();
  }

  @AfterEach
  public void teardown() {
    engine.destroy();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testGenerateRSAKey() {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    Microservice microservice = new Microservice();
    microservice.setInstance(microserviceInstance);

    AuthenticationBootListener authenticationBootListener = new AuthenticationBootListener();
    BootEvent bootEvent = new BootEvent();
    bootEvent.setEventType(BootListener.EventType.BEFORE_REGISTRY);
    authenticationBootListener.onBootEvent(bootEvent);
    Assertions.assertNotNull(Keypair4Auth.INSTANCE.getPrivateKey());
    Assertions.assertNotNull(Keypair4Auth.INSTANCE.getPublicKey());
  }

  @Test
  public void testMicroserviceInstancePublicKey() {
    AuthenticationBootListener authenticationBootListener = new AuthenticationBootListener();
    BootEvent bootEvent = new BootEvent();
    bootEvent.setEventType(BootListener.EventType.BEFORE_REGISTRY);
    authenticationBootListener.onBootEvent(bootEvent);
    String publicKey = RegistrationManager.INSTANCE.getMicroserviceInstance().
        getProperties().get(DefinitionConst.INSTANCE_PUBKEY_PRO);
    Assertions.assertNotNull(publicKey);
  }
}
