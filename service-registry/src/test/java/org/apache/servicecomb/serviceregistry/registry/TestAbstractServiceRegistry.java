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

import javax.xml.ws.Holder;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.DefaultMicroserviceVersionFactory;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import org.apache.servicecomb.serviceregistry.consumer.StaticMicroserviceVersions;
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

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
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

    registry.appManager = Mockito.mock(AppManager.class);
    MicroserviceManager microserviceManager = Mockito.mock(MicroserviceManager.class);
    Mockito.when(registry.appManager.getOrCreateMicroserviceManager(this.appId)).thenReturn(microserviceManager);
    HashMap<String, MicroserviceVersions> versionsByName = new HashMap<>();
    Mockito.when(microserviceManager.getVersionsByName()).thenReturn(versionsByName);

    ArrayList<MicroserviceInstance> instancesParam = new ArrayList<>();
    Holder<Boolean> checked = new Holder<>(false);
    StaticMicroserviceVersions microserviceVersions = new MockUp<StaticMicroserviceVersions>() {
      @Mock
      void addInstances(String version, List<MicroserviceInstance> instances) {
        Assert.assertEquals(version, version);
        Assert.assertSame(instancesParam, instances);
        checked.value = true;
      }
    }.getMockInstance();
    versionsByName.put(testServiceName, microserviceVersions);

    registry.registryMicroserviceMapping(testServiceName, testVersion, Test3rdPartyServiceIntf.class, instancesParam);
    Assert.assertTrue(checked.value);
  }

  @Test
  public void registryMicroserviceMappingByEndpoints() {
    String testServiceName = "testService";
    final String testVersion = "1.0.1";

    registry.appManager = Mockito.mock(AppManager.class);
    MicroserviceManager microserviceManager = Mockito.mock(MicroserviceManager.class);
    Mockito.when(registry.appManager.getOrCreateMicroserviceManager(this.appId)).thenReturn(microserviceManager);
    HashMap<String, MicroserviceVersions> versionsByName = new HashMap<>();
    Mockito.when(microserviceManager.getVersionsByName()).thenReturn(versionsByName);

    ArrayList<MicroserviceInstance> instancesParam = new ArrayList<>();
    Holder<Boolean> checked = new Holder<>(false);
    StaticMicroserviceVersions microserviceVersions = new MockUp<StaticMicroserviceVersions>() {
      @Mock
      void addInstances(String version, List<MicroserviceInstance> instances) {
        Assert.assertEquals("1.0.1", version);
        Assert.assertEquals(2, instances.size());
        Assert.assertEquals(1, instances.get(0).getEndpoints().size());
        Assert.assertEquals("http://127.0.0.1:8080", instances.get(0).getEndpoints().get(0));
        Assert.assertEquals(1, instances.get(1).getEndpoints().size());
        Assert.assertEquals("http://127.0.0.1:8081", instances.get(1).getEndpoints().get(0));
        checked.value = true;
      }
    }.getMockInstance();
    versionsByName.put(testServiceName, microserviceVersions);

    registry.registryMicroserviceMappingByEndpoints("testService", "1.0.1", Test3rdPartyServiceIntf.class,
        Arrays.asList("http://127.0.0.1:8080", "http://127.0.0.1:8081"));
    Assert.assertTrue(checked.value);
  }

  private interface Test3rdPartyServiceIntf {
  }
}
