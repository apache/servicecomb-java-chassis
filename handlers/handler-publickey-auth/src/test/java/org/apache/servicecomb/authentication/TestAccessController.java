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

import org.apache.servicecomb.authentication.provider.AccessController;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestAccessController {
  @Before
  public void setUp() throws Exception {
    Log4jUtils.init();
  }

  @After
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testIsValidOfWhite() {
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.propertyName", "serviceName");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.rule", "trust*");
    AccessController controller = new AccessController();
    Microservice service = Mockito.mock(Microservice.class);

    Mockito.when(service.getServiceName()).thenReturn("trustCustomer");
    Assert.assertTrue(controller.isAllowed(service));

    Mockito.when(service.getServiceName()).thenReturn("nottrustCustomer");
    Assert.assertTrue(!controller.isAllowed(service));

    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.rule", "*trust");
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assert.assertTrue(controller.isAllowed(service));

    Mockito.when(service.getServiceName()).thenReturn("Customer_trust_not");
    Assert.assertTrue(!controller.isAllowed(service));

    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.rule", "trust");
    Mockito.when(service.getServiceName()).thenReturn("trust");
    Assert.assertTrue(controller.isAllowed(service));

    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assert.assertTrue(!controller.isAllowed(service));
  }

  @Test
  public void testIsValidOfBlack() {
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.propertyName", "serviceName");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.rule", "trust*");
    AccessController controller = new AccessController();
    Microservice service = Mockito.mock(Microservice.class);

    Mockito.when(service.getServiceName()).thenReturn("trustCustomer");
    Assert.assertTrue(!controller.isAllowed(service));

    Mockito.when(service.getServiceName()).thenReturn("nottrustCustomer");
    Assert.assertTrue(controller.isAllowed(service));

    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.rule", "*trust");
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assert.assertTrue(!controller.isAllowed(service));

    Mockito.when(service.getServiceName()).thenReturn("Customer_trust_not");
    Assert.assertTrue(controller.isAllowed(service));

    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.rule", "trust");
    Mockito.when(service.getServiceName()).thenReturn("trust");
    Assert.assertTrue(!controller.isAllowed(service));

    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assert.assertTrue(controller.isAllowed(service));
  }

  @Test
  public void testIsValidOfBlackAndWhite() {
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.propertyName", "serviceName");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.white.list1.rule", "trust*");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.propertyName", "serviceName");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.category", "property");
    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.rule", "*hacker");

    AccessController controller = new AccessController();
    Microservice service = Mockito.mock(Microservice.class);

    Mockito.when(service.getServiceName()).thenReturn("trustCustomer");
    Assert.assertTrue(controller.isAllowed(service));

    Mockito.when(service.getServiceName()).thenReturn("trustCustomerhacker");
    Assert.assertTrue(!controller.isAllowed(service));
  }
}
