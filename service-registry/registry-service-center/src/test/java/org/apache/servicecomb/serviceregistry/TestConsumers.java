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

package org.apache.servicecomb.serviceregistry;

import java.util.Arrays;

import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.MicroserviceKey;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.consumer.MicroserviceVersion;
import org.apache.servicecomb.registry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.registry.consumer.MicroserviceVersions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import mockit.Mock;
import mockit.MockUp;

public class TestConsumers extends TestRegistryBase {
  @Before
  public void before() {

  }

  @After
  public void tearDown() {
  }

  @Test
  public void getOrCreateMicroserviceVersionRule() {
    MicroserviceVersionRule microserviceVersionRule = appManager
        .getOrCreateMicroserviceVersionRule(appId, serviceName, versionRule);
    Assertions.assertEquals("0.0.0.0+", microserviceVersionRule.getVersionRule().getVersionRule());
    Assertions.assertEquals(1, microserviceManager.getVersionsByName().size());

    MicroserviceVersion microserviceVersion = microserviceVersionRule.getLatestMicroserviceVersion();
    Assertions.assertEquals(serviceName, microserviceVersion.getMicroserviceName());
    Assertions.assertEquals(serviceId, microserviceVersion.getMicroserviceId());
    Assertions.assertEquals(version, microserviceVersion.getVersion().getVersion());
  }

  @Test
  public void testCreateRuleServiceNotExists() {
    serviceName = "notExist";
    MicroserviceVersions microserviceVersions = microserviceManager.getOrCreateMicroserviceVersions(serviceName);
    MicroserviceVersionRule microserviceVersionRule = microserviceVersions
        .getOrCreateMicroserviceVersionRule(versionRule);
    Assertions.assertEquals("0.0.0.0+", microserviceVersionRule.getVersionRule().getVersionRule());
    Assertions.assertNull(microserviceVersionRule.getLatestMicroserviceVersion());
    Assertions.assertEquals(0, microserviceManager.getVersionsByName().size());
  }

  @Test
  public void watchDeleteEvent() {
    MicroserviceVersionRule microserviceVersionRule = appManager
        .getOrCreateMicroserviceVersionRule(appId, serviceName, versionRule);
    Assertions.assertEquals("0.0.0.0+", microserviceVersionRule.getVersionRule().getVersionRule());
    Assertions.assertEquals(1, microserviceManager.getVersionsByName().size());

    mockNotExist();

    MicroserviceKey key = new MicroserviceKey();
    MicroserviceInstanceChangedEvent event = new MicroserviceInstanceChangedEvent();
    event.setKey(key);

    key.setAppId(appId);
    key.setServiceName(serviceName);
    eventBus.post(event);
    long begin = System.currentTimeMillis();
    while (microserviceManager.getVersionsByName().size() > 0 && System.currentTimeMillis() - begin < 1000) {
      Thread.yield();
    }
    Assertions.assertEquals(0, microserviceManager.getVersionsByName().size());
  }

  @Test
  public void deleteWhenCreateMicroserviceVersion() {
    new MockUp<DiscoveryManager>() {
      @Mock
      public Microservice getMicroservice(String microserviceId) {
        return null;
      }
    };

    try (LogCollector collector = new LogCollector()) {
      appManager.getOrCreateMicroserviceVersionRule(appId, serviceName, versionRule);
      Assertions.assertEquals(0, microserviceManager.getVersionsByName().size());

      MatcherAssert.assertThat(collector.getEvents().stream()
              .filter(e -> e.getThrowableInformation() != null)
              .map(e -> e.getThrowableInformation().getThrowable().getMessage())
              .toArray(),
          Matchers.hasItemInArray("failed to query by microserviceId '002' from ServiceCenter."));
    }
  }

  @Test
  public void delete_disconnect_cache() {
    MicroserviceVersionRule microserviceVersionRule = appManager
        .getOrCreateMicroserviceVersionRule(appId, serviceName, versionRule);
    Assertions.assertEquals("0.0.0.0+", microserviceVersionRule.getVersionRule().getVersionRule());
    Assertions.assertEquals(1, microserviceManager.getVersionsByName().size());

    mockDisconnect();
    appManager.pullInstances();

    Assertions.assertEquals(1, microserviceManager.getVersionsByName().size());
  }

  @Test
  public void registryMicroserviceMapping() {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    RegistrationManager.INSTANCE
        .registerMicroserviceMapping("3rd", "1.0.0", Arrays.asList(microserviceInstance), Hello.class);

    MicroserviceVersionRule microserviceVersionRule = appManager.getOrCreateMicroserviceVersionRule(appId, "3rd", "0+");
    MatcherAssert.assertThat(microserviceVersionRule.getInstances().values(), Matchers.contains(microserviceInstance));
  }

  @Test
  public void registryMicroserviceMappingByEndpoints() {
    RegistrationManager.INSTANCE.registerMicroserviceMappingByEndpoints(
        "3rd",
        "1.0.0",
        Arrays.asList("cse://127.0.0.1:8080", "cse://127.0.0.1:8081"),
        Hello.class);

    MicroserviceVersionRule microserviceVersionRule = appManager.getOrCreateMicroserviceVersionRule(appId, "3rd", "0+");
    Assertions.assertEquals(2, microserviceVersionRule.getInstances().size());
    MatcherAssert.assertThat(microserviceVersionRule.getInstances().values().stream()
            .flatMap(inst -> inst.getEndpoints().stream())
            .toArray(),
        Matchers.arrayContainingInAnyOrder("cse://127.0.0.1:8080", "cse://127.0.0.1:8081"));
  }
}
