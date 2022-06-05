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

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.event.ShutdownEvent;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Assertions;

public class TestRemoteServiceRegistry {
  static class TestingRemoteServiceRegistry extends RemoteServiceRegistry {
    public TestingRemoteServiceRegistry(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
        Configuration configuration) {
      super(eventBus, serviceRegistryConfig, configuration);
    }

    @Override
    protected ServiceRegistryClient createServiceRegistryClient() {
      return new LocalServiceRegistryClientImpl();
    }
  }

  @Test
  public void testLifeCycle(@Injectable ServiceRegistryConfig config,
      @Injectable ServiceRegistry registry) throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    ServiceRegistryTaskInitializer initializer = new MockUp<ServiceRegistryTaskInitializer>() {
      @Mock
      void init(RemoteServiceRegistry remoteServiceRegistry) {
        latch.countDown();
      }
    }.getMockInstance();

    new Expectations(SPIServiceUtils.class) {
      {
        config.getHeartbeatInterval();
        result = 30;
        config.getInstancePullInterval();
        result = 30;
        config.getRegistryName();
        result = "TestRegistry";
        SPIServiceUtils.getOrLoadSortedService(ServiceRegistryTaskInitializer.class);
        result = Arrays.asList(initializer);
      }
    };

    ServiceRegistry oldRegistry = RegistryUtils.getServiceRegistry();
    RegistryUtils.setServiceRegistry(registry);
    EventBus bus = new EventBus();
    RemoteServiceRegistry remote = new TestingRemoteServiceRegistry(bus, config, ConfigUtil.createLocalConfig());
    remote.init();
    remote.run();

    // should not block
    latch.await();

    Assertions.assertTrue(2 <= remote.getTaskPool().getTaskCount()); // includes complete tasks

    bus.post(new ShutdownEvent());

    remote.getTaskPool().schedule(() -> {
    }, 0, TimeUnit.SECONDS);
    Assertions.assertTrue(remote.getTaskPool().isShutdown());
    RegistryUtils.setServiceRegistry(oldRegistry);
  }
}
