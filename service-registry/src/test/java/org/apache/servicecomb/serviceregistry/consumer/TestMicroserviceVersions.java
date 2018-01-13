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

package org.apache.servicecomb.serviceregistry.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.MicroserviceKey;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.serviceregistry.api.response.FindInstancesResponse;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.task.event.PullMicroserviceVersionsInstancesEvent;
import org.apache.servicecomb.serviceregistry.version.Version;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestMicroserviceVersions {
  EventBus eventBus = new EventBus();

  AppManager appManager = new AppManager(eventBus);

  String appId = "appId";

  String microserviceName = "msName";

  Map<String, Microservice> microservices = new HashMap<>();

  List<MicroserviceInstance> instances = new ArrayList<>();

  MicroserviceVersions microserviceVersions;

  AtomicInteger pendingPullCount;

  MicroserviceInstances microserviceInstances = null;

  FindInstancesResponse findInstancesResponse = null;

  @Before
  public void setUp() throws Exception {
    microserviceInstances = new MicroserviceInstances();
    findInstancesResponse = new FindInstancesResponse();
  }

  @After
  public void tearDown() throws Exception {
    findInstancesResponse = null;
    microserviceInstances = null;
  }

  public TestMicroserviceVersions() {
    microserviceVersions = new MicroserviceVersions(appManager, appId, microserviceName);
    pendingPullCount = Deencapsulation.getField(microserviceVersions, "pendingPullCount");
  }

  private void createMicroservice(String microserviceId) {
    Microservice microservice = new Microservice();
    microservice.setServiceId(microserviceId);
    microservice.setVersion(microserviceId + ".0.0");

    microservices.put(microserviceId, microservice);
  }

  private void createInstance(String microserviceId) {
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setInstanceId("i" + microserviceId);
    instance.setServiceId(microserviceId);

    instances.add(instance);
  }

  private void createMicroserviceInstances() {
    findInstancesResponse.setInstances(instances);
    microserviceInstances.setInstancesResponse(findInstancesResponse);
    microserviceInstances.setRevision("1");
    microserviceInstances.setNeedRefresh(true);
  }

  private void setup(String microserviceId) {
    createMicroservice(microserviceId);
    createInstance(microserviceId);
    createMicroserviceInstances();

    new MockUp<RegistryUtils>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule, String revision) {
        return microserviceInstances;
      }

      @Mock
      Microservice getMicroservice(String microserviceId) {
        return microservices.get(microserviceId);
      }
    };
  }

  @Test
  public void construct() {
    microserviceVersions = new MicroserviceVersions(appManager, appId, microserviceName);

    Assert.assertEquals(appId, microserviceVersions.getAppId());
    Assert.assertEquals(microserviceName, microserviceVersions.getMicroserviceName());
  }

  @Test
  public void submitPull() {
    String microserviceId = "1";
    setup(microserviceId);
    microserviceVersions.submitPull();

    Assert.assertSame(microservices.get(microserviceId),
        microserviceVersions.getVersions().get(microserviceId).getMicroservice());
    Assert.assertSame(microservices.get(microserviceId),
        microserviceVersions.getVersion(microserviceId).getMicroservice());
  }

  @Test
  public void pullInstancesCancel() {
    new MockUp<RegistryUtils>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule, String revision) {
        throw new Error("must not pull");
      }
    };

    pendingPullCount.set(2);

    microserviceVersions.pullInstances();
    Assert.assertEquals(1, pendingPullCount.get());
  }

  @Test
  public void pullInstancesNull() {
    new MockUp<RegistryUtils>() {
      @Mock
      MicroserviceInstances findServiceInstances(String appId, String serviceName,
          String versionRule, String revision) {
        return null;
      }
    };

    pendingPullCount.set(1);

    // not throw exception
    microserviceVersions.pullInstances();
  }

  @Test
  public void setInstancesMatch() {
    String microserviceId = "1";
    setup(microserviceId);
    pendingPullCount.set(1);

    MicroserviceVersionRule microserviceVersionRule = microserviceVersions.getOrCreateMicroserviceVersionRule("1.0.0");
    microserviceVersions.pullInstances();

    Assert.assertSame(instances.get(0), microserviceVersionRule.getInstances().get("i1"));
  }

  @Test
  public void setInstances_selectUp() {
    String microserviceId = "1";
    setup(microserviceId);

    instances.get(0).setStatus(MicroserviceInstanceStatus.DOWN);
    Deencapsulation.invoke(microserviceVersions, "setInstances", instances, "0");

    List<?> resultInstances = Deencapsulation.getField(microserviceVersions, "instances");
    Assert.assertTrue(resultInstances.isEmpty());
  }

  @Test
  public void getOrCreateMicroserviceVersionRule() {
    MicroserviceVersionRule microserviceVersionRule = microserviceVersions.getOrCreateMicroserviceVersionRule("1.0.0");
    Assert.assertSame(microserviceVersionRule, microserviceVersions.getOrCreateMicroserviceVersionRule("1.0.0"));
  }

  @Test
  public void createAndInitMicroserviceVersionRule(@Mocked MicroserviceVersion microserviceVersion) {
    String microserviceId = "1";
    createMicroservice(microserviceId);

    Version version = new Version("1.0.0");

    new Expectations() {
      {
        microserviceVersion.getVersion();
        result = version;
        microserviceVersion.getMicroservice();
        result = microservices.get(microserviceId);
      }
    };

    microserviceVersions.getVersions().put(microserviceId, microserviceVersion);

    MicroserviceVersionRule microserviceVersionRule =
        microserviceVersions.createAndInitMicroserviceVersionRule("1.0.0");
    Assert.assertSame(microserviceVersion, microserviceVersionRule.getLatestMicroserviceVersion());
  }

  @Test
  public void onMicroserviceInstanceChangedAppNotMatch() {
    MicroserviceKey key = new MicroserviceKey();
    key.setAppId("otherAppId");

    MicroserviceInstanceChangedEvent event = new MicroserviceInstanceChangedEvent();
    event.setKey(key);

    microserviceVersions.onMicroserviceInstanceChanged(event);

    Assert.assertEquals(0, pendingPullCount.get());
  }

  @Test
  public void onMicroserviceInstanceChangedNameNotMatch() {
    MicroserviceKey key = new MicroserviceKey();
    key.setAppId(appId);
    key.setServiceName("otherName");

    MicroserviceInstanceChangedEvent event = new MicroserviceInstanceChangedEvent();
    event.setKey(key);

    eventBus.post(event);

    Assert.assertEquals(0, pendingPullCount.get());
  }

  @Test
  public void onMicroserviceInstanceChangedMatch() {
    MicroserviceKey key = new MicroserviceKey();
    key.setAppId(appId);
    key.setServiceName(microserviceName);

    MicroserviceInstanceChangedEvent event = new MicroserviceInstanceChangedEvent();
    event.setKey(key);

    eventBus.register(new Object() {
      @Subscribe
      public void onEvent(PullMicroserviceVersionsInstancesEvent pullEvent) {
        pendingPullCount.incrementAndGet();
      }
    });
    eventBus.post(event);

    Assert.assertEquals(2, pendingPullCount.get());
  }
}
