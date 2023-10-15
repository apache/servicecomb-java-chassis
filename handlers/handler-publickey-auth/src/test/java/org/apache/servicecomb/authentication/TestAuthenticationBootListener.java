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

import static org.apache.servicecomb.core.SCBEngine.CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC;
import static org.apache.servicecomb.core.SCBEngine.DEFAULT_TURN_DOWN_STATUS_WAIT_SEC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.token.Keypair4Auth;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestAuthenticationBootListener {
  private SCBEngine engine;

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
    engine = SCBBootstrap.createSCBEngineForTest(environment);
    engine.run();
  }

  @AfterEach
  public void teardown() {
    engine.destroy();
  }

  @Test
  public void testGenerateRSAKey() {
    RegistrationManager registrationManager = Mockito.mock(RegistrationManager.class);
    AuthenticationBootListener authenticationBootListener = new AuthenticationBootListener();
    authenticationBootListener.setRegistrationManager(registrationManager);
    BootEvent bootEvent = new BootEvent();
    bootEvent.setEventType(BootListener.EventType.BEFORE_REGISTRY);
    authenticationBootListener.onBootEvent(bootEvent);
    Assertions.assertNotNull(Keypair4Auth.INSTANCE.getPrivateKey());
    Assertions.assertNotNull(Keypair4Auth.INSTANCE.getPublicKey());
  }

  @Test
  public void testMicroserviceInstancePublicKey() {
    RegistrationManager registrationManager = Mockito.mock(RegistrationManager.class);
    AuthenticationBootListener authenticationBootListener = new AuthenticationBootListener();
    authenticationBootListener.setRegistrationManager(registrationManager);
    BootEvent bootEvent = new BootEvent();
    bootEvent.setEventType(BootListener.EventType.BEFORE_REGISTRY);

    authenticationBootListener.onBootEvent(bootEvent);

    Mockito.verify(registrationManager, times(1))
        .addProperty(eq(DefinitionConst.INSTANCE_PUBKEY_PRO), any(String.class));
  }
}
