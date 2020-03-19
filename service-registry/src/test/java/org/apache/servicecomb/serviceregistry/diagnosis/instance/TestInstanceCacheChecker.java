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
import java.util.Arrays;

import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.testing.MockClock;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.response.FindInstancesResponse;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.diagnosis.Status;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.json.Json;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestInstanceCacheChecker {
  AppManager originalAppManager = RegistryUtils.getAppManager();

  ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();

  InstanceCacheChecker checker;

  InstanceCacheSummary expectedSummary = new InstanceCacheSummary();

  String appId = "appId";

  String microserviceName = "msName";

  @Before
  public void setUp() throws Exception {
    Deencapsulation.setField(RegistryUtils.class, "appManager", new AppManager());

    serviceRegistry.init();
    RegistryUtils.setServiceRegistry(serviceRegistry);

    checker = new InstanceCacheChecker(RegistryUtils.getAppManager());
    checker.clock = new MockClock(new Holder<>(1L));
    expectedSummary.setStatus(Status.NORMAL);
    expectedSummary.setTimestamp(1);
  }

  @After
  public void tearDown() throws Exception {
    Deencapsulation.setField(RegistryUtils.class, "appManager", originalAppManager);
    RegistryUtils.setServiceRegistry(null);
  }

  @Test
  public void check_appManager_empty() {
    InstanceCacheSummary instanceCacheSummary = checker.check();

    Assert.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
  }

  @Test
  public void check_microserviceManager_empty() {
    appId = "notExist";
    RegistryUtils.getAppManager().getOrCreateMicroserviceVersions(appId, microserviceName);
    InstanceCacheSummary instanceCacheSummary = checker.check();
    Assert.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
  }

  protected Holder<MicroserviceInstances> createFindServiceInstancesResult() {
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

  protected void registerMicroservice(String appId, String microserviceName) {
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
          String versionRule, String revision) {
        return findHolder.value;
      }
    };

    registerMicroservice(appId, microserviceName);

    RegistryUtils.getAppManager()
        .getOrCreateMicroserviceVersionRule(appId, microserviceName, DefinitionConst.VERSION_RULE_ALL);

    findHolder.value = null;
    InstanceCacheSummary instanceCacheSummary = checker.check();

    InstanceCacheResult instanceCacheResult = new InstanceCacheResult();
    instanceCacheResult.setAppId(appId);
    instanceCacheResult.setMicroserviceName(microserviceName);
    instanceCacheResult.setStatus(Status.UNKNOWN);
    instanceCacheResult.setDetail("failed to find instances from service center");
    instanceCacheResult.setPulledInstances(new ArrayList<>());
    expectedSummary.getProducers().add(instanceCacheResult);
    expectedSummary.setStatus(Status.UNKNOWN);

    Assert.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
  }

  @Test
  public void check_findInstances_serviceNotExist() {
    Holder<MicroserviceInstances> findHolder = createFindServiceInstancesResult();

    new MockUp<RegistryUtils>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule, String revision) {
        return findHolder.value;
      }
    };

    registerMicroservice(appId, microserviceName);

    RegistryUtils.getAppManager()
        .getOrCreateMicroserviceVersionRule(appId, microserviceName, DefinitionConst.VERSION_RULE_ALL);

    findHolder.value.setMicroserviceNotExist(true);
    InstanceCacheSummary instanceCacheSummary = checker.check();

    InstanceCacheResult instanceCacheResult = new InstanceCacheResult();
    instanceCacheResult.setAppId(appId);
    instanceCacheResult.setMicroserviceName(microserviceName);
    instanceCacheResult.setStatus(Status.UNKNOWN);
    instanceCacheResult.setDetail("microservice is not exist anymore, will be deleted from memory in next pull");
    instanceCacheResult.setPulledInstances(new ArrayList<>());
    expectedSummary.getProducers().add(instanceCacheResult);
    expectedSummary.setStatus(Status.UNKNOWN);

    Assert.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
  }

  @Test
  public void check_findInstances_revisionNotMatch() {
    Holder<MicroserviceInstances> findHolder = createFindServiceInstancesResult();

    new MockUp<RegistryUtils>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule, String revision) {
        return findHolder.value;
      }
    };

    registerMicroservice(appId, microserviceName);

    RegistryUtils.getAppManager()
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

    Assert.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
  }

  @Test
  public void check_findInstances_cacheNotMatch() {
    Holder<MicroserviceInstances> findHolder = createFindServiceInstancesResult();

    new MockUp<RegistryUtils>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule, String revision) {
        return findHolder.value;
      }
    };

    registerMicroservice(appId, microserviceName);

    MicroserviceVersions microserviceVersions = RegistryUtils.getAppManager()
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

    Assert.assertEquals(Json.encode(expectedSummary), Json.encode(instanceCacheSummary));
    Assert.assertNull(microserviceVersions.getRevision());
  }

  @Test
  public void check_StaticMicroservice() {
    microserviceName = appId + ":" + microserviceName;
    serviceRegistry.registerMicroserviceMappingByEndpoints(microserviceName,
        "1",
        Arrays.asList("rest://localhost:8080"),
        ThirdPartyServiceForUT.class);

    MicroserviceVersionRule microserviceVersionRule = RegistryUtils.getAppManager()
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
