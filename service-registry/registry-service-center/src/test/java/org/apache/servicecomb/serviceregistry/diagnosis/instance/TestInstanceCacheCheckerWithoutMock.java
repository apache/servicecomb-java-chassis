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
package org.apache.servicecomb.serviceregistry.diagnosis.instance;

import java.util.Arrays;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.time.MockClock;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.diagnosis.Status;
import org.apache.servicecomb.serviceregistry.registry.LocalServiceRegistryFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.Json;

public class TestInstanceCacheCheckerWithoutMock {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestInstanceCacheCheckerWithoutMock.class);

  ServiceRegistry serviceRegistry = LocalServiceRegistryFactory.createLocal();

  InstanceCacheChecker checker;

  InstanceCacheSummary expectedSummary = new InstanceCacheSummary();

  String appId = "appId";

  String microserviceName = "msName";

  @Before
  public void setUp() throws Exception {
    ConfigUtil.installDynamicConfig();

    DiscoveryManager.renewInstance();

    serviceRegistry.init();
    RegistryUtils.setServiceRegistry(serviceRegistry);

    checker = new InstanceCacheChecker(DiscoveryManager.INSTANCE.getAppManager());
    checker.clock = new MockClock(1L);
    expectedSummary.setStatus(Status.NORMAL);
    expectedSummary.setTimestamp(1);
  }

  @After
  public void tearDown() throws Exception {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void check_appManager_empty() {
    InstanceCacheSummary instanceCacheSummary = checker.check();

    Assert.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
  }

  @Test
  public void check_microserviceManager_empty() {
    try {
      appId = "notExist";
      DiscoveryManager.INSTANCE.getAppManager().getOrCreateMicroserviceVersions(appId, microserviceName);
      InstanceCacheSummary instanceCacheSummary = checker.check();
      Assert.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
    } catch (Exception e) {
      LOGGER.error("", e);
      Assert.fail();
    }
  }

  @Test
  public void check_StaticMicroservice() {
    microserviceName = appId + ":" + microserviceName;
    RegistrationManager.INSTANCE.registerMicroserviceMappingByEndpoints(microserviceName,
        "1",
        Arrays.asList("rest://localhost:8080"),
        ThirdPartyServiceForUT.class);

    MicroserviceVersionRule microserviceVersionRule = DiscoveryManager.INSTANCE.getAppManager()
        .getOrCreateMicroserviceVersionRule(appId, microserviceName, DefinitionConst.VERSION_RULE_ALL);
    Assert.assertEquals(microserviceName, microserviceVersionRule.getLatestMicroserviceVersion().getMicroserviceName());

    InstanceCacheSummary instanceCacheSummary = checker.check();

    expectedSummary.setStatus(Status.NORMAL);

    Assert.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
  }

  private interface ThirdPartyServiceForUT {
    String sayHello(String name);
  }
}
