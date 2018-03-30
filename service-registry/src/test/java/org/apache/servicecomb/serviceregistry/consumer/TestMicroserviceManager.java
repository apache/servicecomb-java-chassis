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

import java.util.Collections;
import java.util.Map;

import javax.xml.ws.Holder;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.response.FindInstancesResponse;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.task.event.PeriodicPullEvent;
import org.apache.servicecomb.serviceregistry.task.event.RecoveryEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

public class TestMicroserviceManager {
  String appId = "appId";

  String serviceName = "msName";

  String versionRule = "0+";

  EventBus eventBus = new EventBus();

  AppManager appManager = new AppManager(eventBus);

  MicroserviceManager microserviceManager = new MicroserviceManager(appManager, appId);

  Map<String, MicroserviceVersions> cachedVersions = Deencapsulation.getField(microserviceManager, "versionsByName");

  MicroserviceInstances microserviceInstances = null;

  FindInstancesResponse findInstancesResponse = null;

  @Before
  public void setUp() throws Exception {
    microserviceInstances = new MicroserviceInstances();
    findInstancesResponse = new FindInstancesResponse();
    findInstancesResponse.setInstances(Collections.emptyList());
    microserviceInstances.setInstancesResponse(findInstancesResponse);
  }

  @After
  public void tearDown() throws Exception {
    findInstancesResponse = null;
    microserviceInstances = null;
  }

  @Test
  public void getOrCreateMicroserviceVersionRule() {
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.findServiceInstances(appId, serviceName, DefinitionConst.VERSION_RULE_ALL, null);
        result = microserviceInstances;
      }
    };

    MicroserviceVersionRule microserviceVersionRule =
        microserviceManager.getOrCreateMicroserviceVersionRule(serviceName, versionRule);
    Assert.assertEquals("0.0.0+", microserviceVersionRule.getVersionRule().getVersionRule());
    Assert.assertNull(microserviceVersionRule.getLatestMicroserviceVersion());
    Assert.assertEquals(1, cachedVersions.size());
  }

  @Test
  public void testCreateRuleServiceNotExists() {
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.findServiceInstances(appId, serviceName, DefinitionConst.VERSION_RULE_ALL, null);
        result = null;
      }
    };

    MicroserviceVersionRule microserviceVersionRule =
        microserviceManager.getOrCreateMicroserviceVersionRule(serviceName, versionRule);
    Assert.assertEquals("0.0.0+", microserviceVersionRule.getVersionRule().getVersionRule());
    Assert.assertNull(microserviceVersionRule.getLatestMicroserviceVersion());
    Assert.assertEquals(0, cachedVersions.size());
  }

  @Test
  public void periodicPull() {
    testPullEvent(new PeriodicPullEvent());
  }

  @Test
  public void serviceRegistryRecovery() {
    testPullEvent(new RecoveryEvent());
  }

  private void testPullEvent(Object event) {
    Map<String, MicroserviceVersions> versionsByName = Deencapsulation.getField(microserviceManager, "versionsByName");

    Holder<Integer> count = new Holder<>();
    count.value = 0;
    MicroserviceVersions versions = new MockUp<MicroserviceVersions>() {
      @Mock
      void submitPull() {
        count.value++;
      }
    }.getMockInstance();
    versionsByName.put("ms", versions);

    eventBus.post(event);
    Assert.assertEquals(1, (int) count.value);
  }
}
