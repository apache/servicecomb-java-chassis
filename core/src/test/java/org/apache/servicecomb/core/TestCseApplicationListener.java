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

package org.apache.servicecomb.core;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.definition.loader.SchemaListenerManager;
import org.apache.servicecomb.core.endpoint.AbstractEndpointsCache;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfigUtils;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.task.MicroserviceInstanceRegisterTask;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestCseApplicationListener {
  @AfterClass
  public static void teardown() {
    AbstractEndpointsCache.init(null, null);
  }

  @After
  public void cleanup() {
    Deencapsulation.setField(ReferenceConfigUtils.class, "ready", false);
  }

  @Test
  public void testCseApplicationListenerNormal(@Injectable ContextRefreshedEvent event,
      @Injectable AbstractApplicationContext context,
      @Injectable ProducerProviderManager producerProviderManager,
      @Injectable ConsumerProviderManager consumerProviderManager,
      @Injectable TransportManager transportManager,
      @Mocked RegistryUtils ru) {
    Map<String, BootListener> listeners = new HashMap<>();
    BootListener listener = Mockito.mock(BootListener.class);
    listeners.put("test", listener);

    SchemaListenerManager schemaListenerManager = Mockito.mock(SchemaListenerManager.class);
    MicroserviceInstance microserviceInstance = Mockito.mock(MicroserviceInstance.class);

    new Expectations() {
      {
        context.getBeansOfType(BootListener.class);
        result = listeners;
      }
    };
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.init();
        RegistryUtils.getInstanceCacheManager();
        RegistryUtils.run();
        RegistryUtils.getMicroserviceInstance();
        result = microserviceInstance;
      }
    };
    Mockito.when(microserviceInstance.getInstanceId()).thenReturn("testInstanceId");

    CseApplicationListener cal = new CseApplicationListener();
    Deencapsulation.setField(cal, "schemaListenerManager", schemaListenerManager);
    cal.setInitEventClass(ContextRefreshedEvent.class);
    cal.setApplicationContext(context);
    ReflectUtils.setField(cal, "producerProviderManager", producerProviderManager);
    ReflectUtils.setField(cal, "consumerProviderManager", consumerProviderManager);
    ReflectUtils.setField(cal, "transportManager", transportManager);

    cal.onApplicationEvent(event);

    EventManager.post(Mockito.mock(MicroserviceInstanceRegisterTask.class));
    verify(schemaListenerManager).notifySchemaListener();
    verify(listener, times(10)).onBootEvent(Mockito.any(BootEvent.class));
  }

  @Test
  public void testCseApplicationListenerThrowException(@Injectable ContextRefreshedEvent event,
      @Injectable AbstractApplicationContext context,
      @Injectable BootListener listener,
      @Injectable ProducerProviderManager producerProviderManager,
      @Mocked RegistryUtils ru) {
    Map<String, BootListener> listeners = new HashMap<>();
    listeners.put("test", listener);

    CseApplicationListener cal = new CseApplicationListener();
    cal.setApplicationContext(context);
    ReflectUtils.setField(cal, "producerProviderManager", producerProviderManager);
    cal.onApplicationEvent(event);
  }

  @Test
  public void testCseApplicationListenerParentNotnull(@Injectable ContextRefreshedEvent event,
      @Injectable AbstractApplicationContext context,
      @Injectable AbstractApplicationContext pContext,
      @Mocked RegistryUtils ru) {

    CseApplicationListener cal = new CseApplicationListener();
    cal.setApplicationContext(context);
    cal.onApplicationEvent(event);
  }

  @Test
  public void testCseApplicationListenerShutdown(@Injectable ContextClosedEvent event,
      @Mocked RegistryUtils ru) {
    new Expectations() {
      {
        RegistryUtils.destroy();
      }
    };
    CseApplicationListener cal = new CseApplicationListener();
    cal.onApplicationEvent(event);
  }

  @Test
  public void testTriggerAfterRegistryEvent() {
    CseApplicationListener cal = new CseApplicationListener();

    Collection<BootListener> listeners = new ArrayList<>(1);
    BootListener listener = Mockito.mock(BootListener.class);
    listeners.add(listener);
    Deencapsulation.setField(cal, "bootListenerList", listeners);

    MicroserviceInstance microserviceInstance = Mockito.mock(MicroserviceInstance.class);
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroserviceInstance();
        result = microserviceInstance;
      }
    };
    Mockito.when(microserviceInstance.getInstanceId()).thenReturn("testInstanceId");

    Deencapsulation.invoke(cal, "triggerAfterRegistryEvent");

    EventManager.post(Mockito.mock(MicroserviceInstanceRegisterTask.class));

    Deencapsulation.invoke(ReferenceConfigUtils.class, "assertIsReady");

    // AFTER_REGISTRY event should only be sent at the first time of registry success.
    EventManager.post(Mockito.mock(MicroserviceInstanceRegisterTask.class));
    verify(listener, times(1)).onBootEvent(Mockito.any(BootEvent.class));
  }

  @Test
  public void testTriggerAfterRegistryEventOnInstanceIdIsNull() {
    CseApplicationListener cal = new CseApplicationListener();

    Collection<BootListener> listeners = new ArrayList<>(1);
    BootListener listener = Mockito.mock(BootListener.class);
    listeners.add(listener);
    Deencapsulation.setField(cal, "bootListenerList", listeners);

    MicroserviceInstance microserviceInstance = Mockito.mock(MicroserviceInstance.class);
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroserviceInstance();
        result = microserviceInstance;
      }
    };
    Mockito.when(microserviceInstance.getInstanceId()).thenReturn(null).thenReturn("testInstanceId");

    Deencapsulation.invoke(cal, "triggerAfterRegistryEvent");

    EventManager.post(Mockito.mock(MicroserviceInstanceRegisterTask.class));

    try {
      Deencapsulation.invoke(ReferenceConfigUtils.class, "assertIsReady");
      fail("an exception is expected.");
    } catch (Exception e) {
      Assert.assertEquals(IllegalStateException.class, e.getClass());
    }
    verify(listener, times(0)).onBootEvent(Mockito.any(BootEvent.class));

    // AFTER_REGISTRY event should only be sent at the first time of registry success.
    EventManager.post(Mockito.mock(MicroserviceInstanceRegisterTask.class));
    Deencapsulation.invoke(ReferenceConfigUtils.class, "assertIsReady");
    verify(listener, times(1)).onBootEvent(Mockito.any(BootEvent.class));
  }
}
