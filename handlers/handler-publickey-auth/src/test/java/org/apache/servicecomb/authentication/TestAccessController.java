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
import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;

public class TestAccessController {
  ConfigurableEnvironment environment;

  EnumerablePropertySource<?> propertySource;

  @BeforeEach
  public void tearDown() {
    environment = Mockito.mock(ConfigurableEnvironment.class);
    propertySource = Mockito.mock(EnumerablePropertySource.class);
  }

  @Test
  public void testIsValidOfWhiteByServiceName() {
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    mutablePropertySources.addLast(propertySource);
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);
    Mockito.when(propertySource.getPropertyNames()).thenReturn(new String[] {
        "servicecomb.publicKey.accessControl.white.list1.propertyName",
        "servicecomb.publicKey.accessControl.white.list1.category",
        "servicecomb.publicKey.accessControl.white.list1.rule"
    });
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.propertyName"))
        .thenReturn("serviceName");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.category"))
        .thenReturn("property");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.rule"))
        .thenReturn("trust*");

    AccessController controller = new AccessController(environment);
    DiscoveryInstance service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trustCustomer");
    Assertions.assertTrue(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("nottrustCustomer");
    Assertions.assertFalse(controller.isAllowed(service));

    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.rule"))
        .thenReturn("*trust");
    Map<String, Object> latest = new HashMap<>();
    latest.put("servicecomb.publicKey.accessControl.white.list1.rule", "*trust");
    controller.onConfigurationChangedEvent(ConfigurationChangedEvent.createIncremental(latest, new HashMap<>()));
    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assertions.assertTrue(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust_not");
    Assertions.assertFalse(controller.isAllowed(service));

    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.rule"))
        .thenReturn("trust");
    latest.put("servicecomb.publicKey.accessControl.white.list1.rule", "trust");
    controller.onConfigurationChangedEvent(ConfigurationChangedEvent.createIncremental(latest, new HashMap<>()));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trust");
    Assertions.assertTrue(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assertions.assertFalse(controller.isAllowed(service));
  }

  @Test
  public void testIsValidOfBlackByServiceName() {
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    mutablePropertySources.addLast(propertySource);
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);
    Mockito.when(propertySource.getPropertyNames()).thenReturn(new String[] {
        "servicecomb.publicKey.accessControl.black.list1.propertyName",
        "servicecomb.publicKey.accessControl.black.list1.category",
        "servicecomb.publicKey.accessControl.black.list1.rule"
    });
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.propertyName"))
        .thenReturn("serviceName");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.category"))
        .thenReturn("property");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.rule"))
        .thenReturn("trust*");

    AccessController controller = new AccessController(environment);

    DiscoveryInstance service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trustCustomer");
    Assertions.assertFalse(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("nottrustCustomer");
    Assertions.assertTrue(controller.isAllowed(service));

    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.rule"))
        .thenReturn("*trust");
    Map<String, Object> latest = new HashMap<>();
    latest.put("servicecomb.publicKey.accessControl.black.list1.rule", "*trust");
    controller.onConfigurationChangedEvent(ConfigurationChangedEvent.createIncremental(latest, new HashMap<>()));
    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assertions.assertFalse(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust_not");
    Assertions.assertTrue(controller.isAllowed(service));

    ArchaiusUtils.setProperty("servicecomb.publicKey.accessControl.black.list1.rule", "trust");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.rule"))
        .thenReturn("trust");
    latest = new HashMap<>();
    latest.put("servicecomb.publicKey.accessControl.black.list1.rule", "trust");
    controller.onConfigurationChangedEvent(ConfigurationChangedEvent.createIncremental(latest, new HashMap<>()));
    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trust");
    Assertions.assertFalse(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("Customer_trust");
    Assertions.assertTrue(controller.isAllowed(service));
  }

  @Test
  public void testIsValidOfBlackAndWhiteByServiceName() {
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    mutablePropertySources.addLast(propertySource);
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);
    Mockito.when(propertySource.getPropertyNames()).thenReturn(new String[] {
        "servicecomb.publicKey.accessControl.black.list1.propertyName",
        "servicecomb.publicKey.accessControl.black.list1.category",
        "servicecomb.publicKey.accessControl.black.list1.rule",
        "servicecomb.publicKey.accessControl.white.list1.propertyName",
        "servicecomb.publicKey.accessControl.white.list1.category",
        "servicecomb.publicKey.accessControl.white.list1.rule"
    });
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.propertyName"))
        .thenReturn("serviceName");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.category"))
        .thenReturn("property");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.rule"))
        .thenReturn("trust*");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.propertyName"))
        .thenReturn("serviceName");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.category"))
        .thenReturn("property");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.rule"))
        .thenReturn("*hacker");

    AccessController controller = new AccessController(environment);
    DiscoveryInstance service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trustCustomer");
    Assertions.assertTrue(controller.isAllowed(service));

    service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trustCustomerhacker");
    Assertions.assertFalse(controller.isAllowed(service));
  }

  @Test
  public void testIsValidOfBlackByProperties() {
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    mutablePropertySources.addLast(propertySource);
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);
    Mockito.when(propertySource.getPropertyNames()).thenReturn(new String[] {
        "servicecomb.publicKey.accessControl.black.list1.propertyName",
        "servicecomb.publicKey.accessControl.black.list1.category",
        "servicecomb.publicKey.accessControl.black.list1.rule",
    });
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.propertyName"))
        .thenReturn("tag");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.category"))
        .thenReturn("property");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.rule"))
        .thenReturn("test");

    AccessController controller = new AccessController(environment);
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
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    mutablePropertySources.addLast(propertySource);
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);
    Mockito.when(propertySource.getPropertyNames()).thenReturn(new String[] {
        "servicecomb.publicKey.accessControl.white.list1.propertyName",
        "servicecomb.publicKey.accessControl.white.list1.category",
        "servicecomb.publicKey.accessControl.white.list1.rule",
    });
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.propertyName"))
        .thenReturn("tag");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.category"))
        .thenReturn("property");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.rule"))
        .thenReturn("test");

    AccessController controller = new AccessController(environment);
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
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    mutablePropertySources.addLast(propertySource);
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);
    Mockito.when(propertySource.getPropertyNames()).thenReturn(new String[] {
        "servicecomb.publicKey.accessControl.black.list1.propertyName",
        "servicecomb.publicKey.accessControl.black.list1.category",
        "servicecomb.publicKey.accessControl.black.list1.rule",
        "servicecomb.publicKey.accessControl.white.list1.propertyName",
        "servicecomb.publicKey.accessControl.white.list1.category",
        "servicecomb.publicKey.accessControl.white.list1.rule"
    });
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.propertyName"))
        .thenReturn("serviceName");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.category"))
        .thenReturn("property");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.white.list1.rule"))
        .thenReturn("trust*");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.propertyName"))
        .thenReturn("version");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.category"))
        .thenReturn("property");
    Mockito.when(environment.getProperty("servicecomb.publicKey.accessControl.black.list1.rule"))
        .thenReturn("0.0.1");

    AccessController controller = new AccessController(environment);
    DiscoveryInstance service = Mockito.mock(DiscoveryInstance.class);
    Mockito.when(service.getServiceName()).thenReturn("trustCustomer");
    Mockito.when(service.getVersion()).thenReturn("0.0.1");

    Assertions.assertFalse(controller.isAllowed(service));
  }
}
