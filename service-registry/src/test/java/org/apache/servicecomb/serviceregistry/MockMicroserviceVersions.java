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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.event.SimpleEventBus;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersion;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import org.apache.servicecomb.serviceregistry.version.Version;
import org.hamcrest.Matchers;
import org.junit.Assert;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

public class MockMicroserviceVersions extends MicroserviceVersions {
  // key is serviceId
  private Map<String, Microservice> mockedMicroservices = new HashMap<>();

  private List<MicroserviceInstance> mockedInstances = new ArrayList<>();

  public MockMicroserviceVersions() {
    super(new AppManager(new SimpleEventBus()), "appId", "msName");

    appManager.setMicroserviceVersionFactory(this::createMicroserviceVersion);

    addMock("1.0.0", 2);
    addMock("2.0.0", 2);
    addMock("3.0.0", 2);
    addMock("4.0.0", 2);
  }

  public void update_v1() {
    update(findInstances("1.0.0"));
  }

  public void update_v1_then_v2() {
    update(findInstances("1.0.0"));
    update(findInstances("2.0.0"));
  }

  public void update_v1_then_v2_then_v1() {
    update(findInstances("1.0.0"));
    update(findInstances("2.0.0"));
    update(findInstances("1.0.0"));
  }

  public void update_v1_then_all() {
    update(findInstances("1.0.0"));
    update(mockedInstances);
  }

  public void update_all() {
    update(mockedInstances);
  }

  public void update_all_then_v3() {
    update(mockedInstances);
    update(findInstances("3.0.0"));
  }

  public List<MicroserviceInstance> findInstances(String... strVersions) {
    List<String> unifyVersions = Arrays.asList(strVersions).stream()
        .map(version -> new Version(version).getVersion())
        .collect(Collectors.toList());

    List<MicroserviceInstance> instances = new ArrayList<>();

    for (MicroserviceInstance instance : mockedInstances) {
      Microservice microservice = mockedMicroservices.get(instance.getServiceId());

      for (String version : unifyVersions) {
        if (version.equals(microservice.getVersion())) {
          instances.add(instance);
          break;
        }
      }
    }

    return instances;
  }

  public void check(MicroserviceVersionRule microserviceVersionRule, String latestVersion, String... instanceVersions) {
    Assert.assertSame(findMicroserviceVersion(latestVersion), microserviceVersionRule.getLatestMicroserviceVersion());
    Assert.assertThat(microserviceVersionRule.getInstances().values(),
        Matchers.containsInAnyOrder(findInstances(instanceVersions).toArray()));
  }

  public MicroserviceVersion findMicroserviceVersion(String version) {
    return getVersion("sid-" + new Version(version).getVersion());
  }

  public void update(List<MicroserviceInstance> pulledInstances) {
    super.safeSetInstances(pulledInstances, "rev");
  }

  public void addMock(String version, int instanceCount) {
    version = new Version(version).getVersion();

    Microservice microservice = new Microservice();
    microservice.setAppId(getAppId());
    microservice.setServiceName(getMicroserviceName());
    microservice.setServiceId("sid-" + version);
    microservice.setVersion(version);
    mockedMicroservices.put(microservice.getServiceId(), microservice);

    for (int idx = 0; idx < instanceCount; idx++) {
      MicroserviceInstance instance = new MicroserviceInstance();

      instance.setServiceId(microservice.getServiceId());
      instance.setInstanceId(String.format("iid-%s-%d", version, idx));

      mockedInstances.add(instance);
    }
  }

  private MicroserviceVersion createMicroserviceVersion(String microserviceName, String serviceId) {
    Microservice microservice = mockedMicroservices.get(serviceId);

    ServiceRegistry serviceRegistry = new MockUp<ServiceRegistry>() {
      @Mock
      Microservice getAggregatedRemoteMicroservice(String microserviceId) {
        return microservice;
      }
    }.getMockInstance();

    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getServiceRegistry();
        result = serviceRegistry;
      }
    };
    return new MicroserviceVersion(serviceId);
  }
}
