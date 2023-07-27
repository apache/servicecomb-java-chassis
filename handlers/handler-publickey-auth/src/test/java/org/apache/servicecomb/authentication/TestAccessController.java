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

import org.apache.servicecomb.authentication.provider.AccessController;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestAccessController {
  @AfterEach
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testIsValidOfWhiteByServiceName() {
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.propertyName", "serviceName");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.rule", "trust*");
    AccessController controller = new AccessController();
    DiscoveryInstance service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trustCustomer");
    Assertions.assertTrue(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("nottrustCustomer");
    Assertions.assertFalse(controller.isAllowed(service));

    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.rule", "*trust");
    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assertions.assertTrue(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust_not");
    Assertions.assertFalse(controller.isAllowed(service));

    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.rule", "trust");
    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trust");
    Assertions.assertTrue(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assertions.assertFalse(controller.isAllowed(service));
  }

  @Test
  public void testIsValidOfBlackByServiceName() {
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.propertyName", "serviceName");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.rule", "trust*");
    AccessController controller = new AccessController();

    DiscoveryInstance service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trustCustomer");
    Assertions.assertFalse(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("nottrustCustomer");
    Assertions.assertTrue(controller.isAllowed(service));

    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.rule", "*trust");
    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assertions.assertFalse(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust_not");
    Assertions.assertTrue(controller.isAllowed(service));

    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.rule", "trust");
    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trust");
    Assertions.assertFalse(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assertions.assertTrue(controller.isAllowed(service));
  }

  @Test
  public void testIsValidOfBlackAndWhiteByServiceName() {
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.propertyName", "serviceName");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.rule", "trust*");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.propertyName", "serviceName");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.rule", "*hacker");

    AccessController controller = new AccessController();
    DiscoveryInstance service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trustCustomer");
    Assertions.assertTrue(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trustCustomerhacker");
    Assertions.assertFalse(controller.isAllowed(service));
  }

  @Test
  public void testIsValidOfBlackByProperties() {
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.propertyName", "tag");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.rule", "test");
    AccessController controller = new AccessController();
    DiscoveryInstance service = Mockito.mock(DiscoveryInstance.class);
    Map<String, String> map = new HashMap<>();
    map.put("tag", "test");
    Mockito.when(service.getProperties()).thenReturn(map);

    Assertions.assertFalse(controller.isAllowed(service));

    map.put("tag", "testa");
    Mockito.when(service.getProperties()).thenReturn(map);
    Assertions.assertTrue(controller.isAllowed(service));
  }

  @Test
  public void testIsValidOfWhiteByProperties() {
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.propertyName", "tag");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.rule", "test");
    AccessController controller = new AccessController();
    DiscoveryInstance service = Mockito.mock(DiscoveryInstance.class);
    Map<String, String> map = new HashMap<>();
    map.put("tag", "test");
    Mockito.when(service.getProperties()).thenReturn(map);
    Assertions.assertTrue(controller.isAllowed(service));

    map.put("tag", "testa");
    Mockito.when(service.getProperties()).thenReturn(map);
    Assertions.assertFalse(controller.isAllowed(service));
  }

  @Test
  public void testIsValidOfBlackAndWhiteByServiceNameAndVersion() {
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.propertyName", "serviceName");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.rule", "trust*");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.propertyName", "version");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.rule", "0.0.1");

    AccessController controller = new AccessController();
    DiscoveryInstance service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trustCustomer");
    Mockito.when(service.getVersion()).thenReturn("0.0.1");

    Assertions.assertFalse(controller.isAllowed(service));
  }
}
