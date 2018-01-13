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

import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.consumer.DefaultMicroserviceVersionFactory;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.eventbus.EventBus;

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
  public void initAppManagerSpecialMicroserviceVersionFactoryFailed()
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
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
}
