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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import org.apache.servicecomb.serviceregistry.task.event.PullMicroserviceVersionsInstancesEvent;
import org.apache.servicecomb.serviceregistry.task.event.ShutdownEvent;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.eventbus.EventBus;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRemoteServiceRegistry {
  class TestingRemoteServiceRegistry extends RemoteServiceRegistry {
    public TestingRemoteServiceRegistry(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
        MicroserviceDefinition microserviceDefinition) {
      super(eventBus, serviceRegistryConfig, microserviceDefinition);
    }

    @Override
    protected ServiceRegistryClient createServiceRegistryClient() {
      return new LocalServiceRegistryClientImpl();
    }
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testLifeCycle(@Injectable ServiceRegistryConfig config, @Injectable MicroserviceDefinition definition,
      @Injectable ServiceRegistry registry) {
    ArrayList<IpPort> ipPortList = new ArrayList<>();
    ipPortList.add(new IpPort("127.0.0.1", 9980));
    ipPortList.add(new IpPort("127.0.0.1", 9981));

    new Expectations() {
      {
        definition.getConfiguration();
        result = ConfigUtil.createLocalConfig();
        config.getIpPort();
        result = ipPortList;
        config.getTransport();
        result = "rest";
        config.isRegistryAutoDiscovery();
        result = true;
        config.getHeartbeatInterval();
        result = 30;
        config.getInstancePullInterval();
        result = 30;
        config.isWatch();
        result = false;
      }
    };

    ServiceRegistry oldRegistry = RegistryUtils.getServiceRegistry();
    RegistryUtils.setServiceRegistry(registry);
    EventBus bus = new EventBus();
    RemoteServiceRegistry remote = new TestingRemoteServiceRegistry(bus, config, definition);
    remote.init();
    remote.run();
    Assert.assertTrue(2 <= remote.getTaskPool().getTaskCount()); // includes complete tasks

    bus.post(new ShutdownEvent());

    remote.getTaskPool().schedule(new Runnable() {
      @Override
      public void run() {
        // TODO Auto-generated method stub

      }
    }, 0, TimeUnit.SECONDS);
    Assert.assertTrue(remote.getTaskPool().isShutdown());
    RegistryUtils.setServiceRegistry(oldRegistry);
  }

  @Test
  public void onPullMicroserviceVersionsInstancesEvent(@Injectable ServiceRegistryConfig config,
      @Injectable MicroserviceDefinition definition, @Mocked MicroserviceVersions microserviceVersions) {
    PullMicroserviceVersionsInstancesEvent event = new PullMicroserviceVersionsInstancesEvent(microserviceVersions, 1);

    ScheduledThreadPoolExecutor taskPool = new MockUp<ScheduledThreadPoolExecutor>() {
      @Mock
      ScheduledFuture<?> schedule(Runnable command,
          long delay,
          TimeUnit unit) {
        Assert.assertEquals(1, delay);
        throw new Error("ok");
      }
    }.getMockInstance();

    expectedException.expect(Error.class);
    expectedException.expectMessage(Matchers.is("ok"));

    EventBus bus = new EventBus();
    RemoteServiceRegistry remote = new TestingRemoteServiceRegistry(bus, config, definition);
    bus.register(remote);
    Deencapsulation.setField(remote, "taskPool", taskPool);
    bus.post(event);
  }
}
