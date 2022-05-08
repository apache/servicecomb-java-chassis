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

import java.util.ArrayList;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.time.MockClock;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.consumer.MicroserviceVersions;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.diagnosis.Status;
import org.apache.servicecomb.serviceregistry.registry.LocalServiceRegistryFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.Json;
import mockit.Mock;
import mockit.MockUp;

public class TestInstanceCacheCheckerMock {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestInstanceCacheCheckerWithoutMock.class);

  ServiceRegistry serviceRegistry = LocalServiceRegistryFactory.createLocal();

  InstanceCacheChecker checker;

  InstanceCacheSummary expectedSummary = new InstanceCacheSummary();

  String appId = "appId";

  String microserviceName = "msName";

  @Before
  public void setUp() throws Exception {
    ConfigUtil.installDynamicConfig();

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

  private Holder<MicroserviceInstances> createFindServiceInstancesResult() {
    MicroserviceInstances microserviceInstances = new MicroserviceInstances();
    microserviceInstances.setNeedRefresh(true);
    microserviceInstances.setRevision("first");
    FindInstancesResponse findInstancesResponse = new FindInstancesResponse();
    findInstancesResponse.setInstances(new ArrayList<>());
    microserviceInstances.setInstancesResponse(findInstancesResponse);

    Holder<MicroserviceInstances> findHolder = new Holder<>();
    findHolder.value = microserviceInstances;
    return findHolder;
  }

  private void registerMicroservice(String appId, String microserviceName) {
    Microservice microservice = new Microservice();
    microservice.setAppId(appId);
    microservice.setServiceName(microserviceName);
    microservice.setVersion("1.0.0");

    serviceRegistry.getServiceRegistryClient().registerMicroservice(microservice);
  }

  @Test
  public void check_findInstances_failed() {
    Holder<MicroserviceInstances> findHolder = createFindServiceInstancesResult();

    new MockUp<RegistryUtils>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule) {
        return findHolder.value;
      }
    };

    registerMicroservice(appId, microserviceName);

    DiscoveryManager.INSTANCE.getAppManager()
        .getOrCreateMicroserviceVersionRule(appId, microserviceName, DefinitionConst.VERSION_RULE_ALL);

    findHolder.value = null;
    try {
      InstanceCacheSummary instanceCacheSummary = checker.check();

      InstanceCacheResult instanceCacheResult = new InstanceCacheResult();
      instanceCacheResult.setAppId(appId);
      instanceCacheResult.setMicroserviceName(microserviceName);
      instanceCacheResult.setStatus(Status.UNKNOWN);
      instanceCacheResult.setDetail("failed to find instances from service center");
      instanceCacheResult.setPulledInstances(new ArrayList<>());
      expectedSummary.getProducers().add(instanceCacheResult);
      expectedSummary.setStatus(Status.UNKNOWN);

      Assertions.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
    } catch (Exception e) {
      LOGGER.error("", e);
      Assertions.fail();
    }
  }

  @Test
  public void check_findInstances_serviceNotExist() {
    Holder<MicroserviceInstances> findHolder = createFindServiceInstancesResult();

    new MockUp<RegistryUtils>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule) {
        return findHolder.value;
      }
    };

    registerMicroservice(appId, microserviceName);

    DiscoveryManager.INSTANCE.getAppManager()
        .getOrCreateMicroserviceVersionRule(appId, microserviceName, DefinitionConst.VERSION_RULE_ALL);

    findHolder.value.setMicroserviceNotExist(true);
    try {
      InstanceCacheSummary instanceCacheSummary = checker.check();

      InstanceCacheResult instanceCacheResult = new InstanceCacheResult();
      instanceCacheResult.setAppId(appId);
      instanceCacheResult.setMicroserviceName(microserviceName);
      instanceCacheResult.setStatus(Status.UNKNOWN);
      instanceCacheResult.setDetail("microservice is not exist anymore, will be deleted from memory in next pull");
      instanceCacheResult.setPulledInstances(new ArrayList<>());
      expectedSummary.getProducers().add(instanceCacheResult);
      expectedSummary.setStatus(Status.UNKNOWN);

      Assertions.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
    } catch (Exception e) {
      LOGGER.error("", e);
      Assertions.fail();
    }
  }

  @Test
  public void check_findInstances_revisionNotMatch() {
    Holder<MicroserviceInstances> findHolder = createFindServiceInstancesResult();

    new MockUp<RegistryUtils>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule) {
        return findHolder.value;
      }
    };

    registerMicroservice(appId, microserviceName);

    DiscoveryManager.INSTANCE.getAppManager()
        .getOrCreateMicroserviceVersionRule(appId, microserviceName, DefinitionConst.VERSION_RULE_ALL);

    findHolder.value.setRevision("second");
    InstanceCacheSummary instanceCacheSummary = checker.check();

    InstanceCacheResult instanceCacheResult = new InstanceCacheResult();
    instanceCacheResult.setAppId(appId);
    instanceCacheResult.setMicroserviceName(microserviceName);
    instanceCacheResult.setStatus(Status.UNKNOWN);
    instanceCacheResult.setPulledInstances(new ArrayList<>());
    instanceCacheResult.setDetail(
        "revision is different, will be synchronized in next pull. local revision=first, remote revision=second");
    expectedSummary.getProducers().add(instanceCacheResult);
    expectedSummary.setStatus(Status.UNKNOWN);

    Assertions.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
  }

  @Test
  public void check_findInstances_cacheNotMatch() {
    Holder<MicroserviceInstances> findHolder = createFindServiceInstancesResult();

    new MockUp<RegistryUtils>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule) {
        return findHolder.value;
      }
    };

    registerMicroservice(appId, microserviceName);

    MicroserviceVersions microserviceVersions = DiscoveryManager.INSTANCE.getAppManager()
        .getOrCreateMicroserviceVersions(appId, microserviceName);
    microserviceVersions.setRevision("first");
    microserviceVersions.getOrCreateMicroserviceVersionRule(DefinitionConst.VERSION_RULE_ALL);

    Holder<MicroserviceInstances> newFindHolder = createFindServiceInstancesResult();
    newFindHolder.value.getInstancesResponse().getInstances().add(new MicroserviceInstance());
    findHolder.value = newFindHolder.value;
    InstanceCacheSummary instanceCacheSummary = checker.check();

    InstanceCacheResult instanceCacheResult = new InstanceCacheResult();
    instanceCacheResult.setAppId(appId);
    instanceCacheResult.setMicroserviceName(microserviceName);
    instanceCacheResult.setStatus(Status.ABNORMAL);
    instanceCacheResult.setDetail(
        "instance cache not match");
    instanceCacheResult.setPulledInstances(new ArrayList<>());
    expectedSummary.getProducers().add(instanceCacheResult);
    expectedSummary.setStatus(Status.ABNORMAL);

    Assertions.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
    Assertions.assertNull(microserviceVersions.getRevision());
  }
}
