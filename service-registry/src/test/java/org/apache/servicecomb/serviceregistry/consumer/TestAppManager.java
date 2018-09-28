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

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.StaticMicroservice;
import org.apache.servicecomb.serviceregistry.api.response.FindInstancesResponse;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestAppManager {
  EventBus eventBus = new EventBus();

  AppManager appManager = new AppManager(eventBus);

  String appId = "appId";

  String serviceName = "msName";

  String versionRule = "0+";

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
        appManager.getOrCreateMicroserviceVersionRule(appId, serviceName, versionRule);
    Assert.assertEquals("0.0.0+", microserviceVersionRule.getVersionRule().getVersionRule());
    Assert.assertNull(microserviceVersionRule.getLatestMicroserviceVersion());
  }

  @Test
  public void getOrCreateMicroserviceVersions() {
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.findServiceInstances(appId, serviceName, DefinitionConst.VERSION_RULE_ALL, null);
        result = microserviceInstances;
      }
    };

    MicroserviceVersions microserviceVersions = appManager.getOrCreateMicroserviceVersions(appId, serviceName);
    Assert.assertEquals(appId, microserviceVersions.getAppId());
    Assert.assertEquals(serviceName, microserviceVersions.getMicroserviceName());
  }

  @Test
  public void setMicroserviceVersionFactory(@Mocked MicroserviceVersionFactory microserviceVersionFactory) {
    appManager.setMicroserviceVersionFactory(microserviceVersionFactory);

    Assert.assertSame(microserviceVersionFactory, appManager.getMicroserviceVersionFactory());
  }

  @Test
  public void getStaticMicroserviceVersionFactory() {
    new MockUp<ServiceRegistryConfig>() {
      @Mock
      String getStaticMicroserviceVersionFactory() {
        return TestStaticMicroserviceVersionFactory.class.getName();
      }
    };

    Assert.assertNull(Deencapsulation.getField(appManager, "staticMicroserviceVersionFactory"));

    Assert.assertEquals(TestStaticMicroserviceVersionFactory.class,
        appManager.getStaticMicroserviceVersionFactory().getClass());
  }

  static class TestStaticMicroserviceVersionFactory implements StaticMicroserviceVersionFactory {
    @Override
    public MicroserviceVersion create(StaticMicroservice microservice) {
      return null;
    }
  }
}
