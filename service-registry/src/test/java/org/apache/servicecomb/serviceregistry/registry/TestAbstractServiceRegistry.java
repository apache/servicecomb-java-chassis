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

package org.apache.servicecomb.serviceregistry.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.DefaultMicroserviceVersionFactory;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersion;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import org.apache.servicecomb.serviceregistry.consumer.StaticMicroserviceVersionFactory;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestAbstractServiceRegistry {
  class AbstractServiceRegistryForTest extends AbstractServiceRegistry {
    public AbstractServiceRegistryForTest(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
        MicroserviceDefinition microserviceDefinition) {
      super(eventBus, serviceRegistryConfig, microserviceDefinition);
    }

    @Override
    protected ServiceRegistryClient createServiceRegistryClient() {
      return null;
    }
  }

  static class DefaultMicroserviceVersionFactoryForTest extends DefaultMicroserviceVersionFactory {

  }

  EventBus eventBus = new EventBus();

  String appId = "app";

  String microserviceName = "ms";

  @Mocked
  ServiceRegistryConfig serviceRegistryConfig;

  MicroserviceDefinition microserviceDefinition = MicroserviceDefinition.create(appId, microserviceName);

  AbstractServiceRegistryForTest registry;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setup() {
    registry =
        new AbstractServiceRegistryForTest(eventBus, serviceRegistryConfig, microserviceDefinition);
    RegistryUtils.setServiceRegistry(registry);
  }

  @After
  public void tearDown() {
    registry.appManager = null;
  }

  @AfterClass
  public static void afterClass() {
    RegistryUtils.setServiceRegistry(null);
  }

  @Test
  public void initAppManagerDefault() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    registry.initAppManager();

    Assert.assertThat(registry.appManager.getMicroserviceVersionFactory(),
        Matchers.instanceOf(DefaultMicroserviceVersionFactory.class));
  }

  @Test
  public void initAppManagerSpecialMicroserviceVersionFactoryNormal()
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    new Expectations() {
      {
        serviceRegistryConfig.getMicroserviceVersionFactory();
        result = DefaultMicroserviceVersionFactoryForTest.class.getName();
      }
    };
    registry.initAppManager();

    Assert.assertThat(registry.appManager.getMicroserviceVersionFactory(),
        Matchers.instanceOf(DefaultMicroserviceVersionFactoryForTest.class));
  }

  @Test
  public void initAppManagerSpecialMicroserviceVersionFactoryFailed() {
    new Expectations() {
      {
        serviceRegistryConfig.getMicroserviceVersionFactory();
        result = "invalid";
      }
    };
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("Failed to init appManager."));

    registry.init();
  }

  @Test
  public void registryMicroserviceMapping() {
    String testServiceName = "testService";
    final String testVersion = "1.0.1";

    HashMap<String, MicroserviceVersions> versionsByName = prepareForMicroserviceMappingRegistry();

    ArrayList<MicroserviceInstance> instancesParam = new ArrayList<>();
    instancesParam.add(new MicroserviceInstance());
    registry.registerMicroserviceMapping(testServiceName, testVersion, instancesParam, Test3rdPartyServiceIntf.class);

    MicroserviceVersions microserviceVersions = versionsByName.get(testServiceName);
    List<MicroserviceInstance> instances = Deencapsulation.getField(microserviceVersions, "instances");
    Assert.assertEquals(1, instances.size());
    Assert.assertSame(instancesParam.get(0), instances.get(0));

    // nothing will happen if register repeatedly
    List<MicroserviceInstance> newInstancesParam = new ArrayList<>();
    newInstancesParam.add(new MicroserviceInstance());
    registry.registerMicroserviceMapping(
        testServiceName, testVersion, newInstancesParam, Test3rdPartyServiceIntf.class);

    microserviceVersions = versionsByName.get(testServiceName);
    instances = Deencapsulation.getField(microserviceVersions, "instances");
    Assert.assertEquals(1, instances.size());
    Assert.assertSame(instancesParam.get(0), instances.get(0));
  }

  @Test
  public void registryMicroserviceMappingByEndpoints() {
    String testServiceName = "testService";
    final String testVersion = "1.0.1";

    HashMap<String, MicroserviceVersions> versionByName = prepareForMicroserviceMappingRegistry();

    registry.registerMicroserviceMappingByEndpoints(testServiceName, testVersion,
        Arrays.asList("cse://127.0.0.1:8080", "cse://127.0.0.1:8081"), Test3rdPartyServiceIntf.class);

    MicroserviceVersions microserviceVersions = versionByName.get(testServiceName);
    List<MicroserviceInstance> instances = Deencapsulation.getField(microserviceVersions, "instances");
    Assert.assertEquals(2, instances.size());
    Assert.assertEquals("cse://127.0.0.1:8080", instances.get(0).getEndpoints().get(0));
    Assert.assertEquals("cse://127.0.0.1:8081", instances.get(1).getEndpoints().get(0));
  }

  private HashMap<String, MicroserviceVersions> prepareForMicroserviceMappingRegistry() {
    registry.appManager = Mockito.mock(AppManager.class);
    MicroserviceManager microserviceManager = Mockito.mock(MicroserviceManager.class);
    StaticMicroserviceVersionFactory staticMicroserviceVersionFactory =
        Mockito.mock(StaticMicroserviceVersionFactory.class);
    HashMap<String, MicroserviceVersions> versionsByName = new HashMap<>();

    Mockito.when(registry.appManager.getOrCreateMicroserviceManager(this.appId)).thenReturn(microserviceManager);
    Mockito.when(registry.appManager.getEventBus()).thenReturn(Mockito.mock(EventBus.class));
    Mockito.when(registry.appManager.getStaticMicroserviceVersionFactory())
        .thenReturn(staticMicroserviceVersionFactory);
    Mockito.when(staticMicroserviceVersionFactory.create(Mockito.any()))
        .thenReturn(Mockito.mock(MicroserviceVersion.class));

    Mockito.when(microserviceManager.getVersionsByName()).thenReturn(versionsByName);
    return versionsByName;
  }

  private interface Test3rdPartyServiceIntf {
  }
}
