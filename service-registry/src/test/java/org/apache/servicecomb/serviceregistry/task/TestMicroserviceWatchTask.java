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
package org.apache.servicecomb.serviceregistry.task;

import javax.xml.ws.Holder;

import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.serviceregistry.api.MicroserviceKey;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.WatchAction;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.task.event.ExceptionEvent;
import org.apache.servicecomb.serviceregistry.task.event.RecoveryEvent;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestMicroserviceWatchTask {
  EventBus eventBus = new EventBus();

  MicroserviceWatchTask microserviceWatchTask;

  private void initWatch(ServiceRegistryConfig serviceRegistryConfig,
      ServiceRegistryClient srClient, Microservice microservice) {
    microserviceWatchTask = new MicroserviceWatchTask(eventBus, serviceRegistryConfig, srClient, microservice);
    microserviceWatchTask.taskStatus = TaskStatus.READY;
    new Expectations() {
      {
        serviceRegistryConfig.isWatch();
        result = true;
        microservice.getServiceId();
        result = "serviceId";
      }
    };
  }

  @Test
  public void testWatchOpen(@Mocked ServiceRegistryConfig serviceRegistryConfig,
      @Mocked ServiceRegistryClient srClient,
      @Mocked Microservice microservice) {
    initWatch(serviceRegistryConfig, srClient, microservice);

    new MockUp<ServiceRegistryClient>(srClient) {
      @Mock
      void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback,
          AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {
        onOpen.success(null);
      }
    };

    Holder<Boolean> openHolder = new Holder<>();
    eventBus.register(new Object() {
      @Subscribe
      public void onOpen(RecoveryEvent event) {
        openHolder.value = true;
      }
    });
    Assert.assertNull(openHolder.value);
    microserviceWatchTask.run();
    Assert.assertTrue(openHolder.value);
  }

  @Test
  public void testWatchFailed(@Mocked ServiceRegistryConfig serviceRegistryConfig,
      @Mocked ServiceRegistryClient srClient,
      @Mocked Microservice microservice) {
    initWatch(serviceRegistryConfig, srClient, microservice);

    new MockUp<ServiceRegistryClient>(srClient) {
      @Mock
      void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback,
          AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {
        callback.fail(new Error("test failed"));
      }
    };

    Holder<Throwable> holder = new Holder<>();
    eventBus.register(new Object() {
      @Subscribe
      public void onException(ExceptionEvent event) {
        holder.value = event.getThrowable();
      }
    });
    Assert.assertNull(holder.value);
    microserviceWatchTask.run();
    Assert.assertEquals("test failed", holder.value.getMessage());
  }

  @Test
  public void testWatchInstanceChanged(@Mocked ServiceRegistryConfig serviceRegistryConfig,
      @Mocked ServiceRegistryClient srClient,
      @Mocked Microservice microservice) {
    initWatch(serviceRegistryConfig, srClient, microservice);

    MicroserviceInstanceChangedEvent changedEvent = new MicroserviceInstanceChangedEvent();
    MicroserviceKey key = new MicroserviceKey();
    key.setAppId(microservice.getAppId());
    key.setVersion(microservice.getVersion());
    key.setServiceName(microservice.getServiceName());
    changedEvent.setKey(key);
    changedEvent.setInstance(microservice.getInstance());

    new MockUp<ServiceRegistryClient>(srClient) {
      @Mock
      void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback,
          AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {
        callback.success(changedEvent);
      }
    };

    Holder<MicroserviceInstanceChangedEvent> holder = new Holder<>();
    eventBus.register(new Object() {
      @Subscribe
      public void onException(MicroserviceInstanceChangedEvent event) {
        holder.value = event;
      }
    });

    changedEvent.setAction(WatchAction.CREATE);
    microserviceWatchTask.run();
    Assert.assertEquals(WatchAction.CREATE, holder.value.getAction());

    changedEvent.setAction(WatchAction.DELETE);
    microserviceWatchTask.run();
    Assert.assertEquals(WatchAction.DELETE, holder.value.getAction());

    changedEvent.setAction(WatchAction.UPDATE);
    microserviceWatchTask.run();
    Assert.assertEquals(WatchAction.UPDATE, holder.value.getAction());
  }

  @Test
  public void testNeedToWatch(@Mocked ServiceRegistryConfig serviceRegistryConfig,
      @Mocked ServiceRegistryClient srClient,
      @Mocked Microservice microservice) {
    EventBus eventBus = new EventBus();

    MicroserviceWatchTask microserviceWatchTask =
        new MicroserviceWatchTask(eventBus, serviceRegistryConfig, srClient, microservice);
    microserviceWatchTask.taskStatus = TaskStatus.READY;

    new MockUp<ServiceRegistryClient>(srClient) {
      @Mock
      void watch(String selfMicroserviceId, AsyncResultCallback<MicroserviceInstanceChangedEvent> callback,
          AsyncResultCallback<Void> onOpen, AsyncResultCallback<Void> onClose) {
        throw new Error("called watch");
      }
    };

    new Expectations() {
      {
        serviceRegistryConfig.isWatch();
        result = false;
      }
    };
    // no watch
    try {
      microserviceWatchTask.run();
    } catch (Throwable e) {
      Assert.fail("must do not watch");
    }

    new Expectations() {
      {
        serviceRegistryConfig.isWatch();
        result = true;
      }
    };
    // no watch
    try {
      microserviceWatchTask.run();
    } catch (Throwable e) {
      // ready state, service id can not be null , will always watch
      Assert.assertEquals("called watch", e.getMessage());
    }

    new Expectations() {
      {
        serviceRegistryConfig.isWatch();
        result = false;
      }
    };
    // no watch
    try {
      microserviceWatchTask.run();
    } catch (Throwable e) {
      // ready state, service id can not be null , will always watch
      Assert.assertEquals("called watch", e.getMessage());
    }

    new Expectations() {
      {
        serviceRegistryConfig.isWatch();
        result = true;
        microservice.getServiceId();
        result = "serviceId";
      }
    };
    // watch
    try {
      microserviceWatchTask.run();
      Assert.fail("must watch");
    } catch (Throwable e) {
      Assert.assertEquals("called watch", e.getMessage());
    }
  }
}
