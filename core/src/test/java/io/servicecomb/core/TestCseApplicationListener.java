/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.core;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;

import io.servicecomb.core.BootListener.BootEvent;
import io.servicecomb.core.definition.loader.SchemaListenerManager;
import io.servicecomb.core.endpoint.AbstractEndpointsCache;
import io.servicecomb.core.provider.consumer.ConsumerProviderManager;
import io.servicecomb.core.provider.producer.ProducerProviderManager;
import io.servicecomb.core.transport.TransportManager;
import io.servicecomb.foundation.common.event.EventManager;
import io.servicecomb.foundation.common.utils.ReflectUtils;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.task.MicroserviceInstanceRegisterTask;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestCseApplicationListener {
  @AfterClass
  public static void teardown() {
    AbstractEndpointsCache.init(null, null);
  }

  @Test
  public void testCseApplicationListenerNormal(@Injectable ContextRefreshedEvent event,
      @Injectable AbstractApplicationContext context,
      @Injectable ProducerProviderManager producerProviderManager,
      @Injectable ConsumerProviderManager consumerProviderManager,
      @Injectable TransportManager transportManager,
      @Mocked RegistryUtils ru) throws Exception {
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
      @Mocked RegistryUtils ru) throws Exception {
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
      @Mocked RegistryUtils ru) throws Exception {

    CseApplicationListener cal = new CseApplicationListener();
    cal.setApplicationContext(context);
    cal.onApplicationEvent(event);
  }

  @Test
  public void testCseApplicationListenerShutdown(@Injectable ContextClosedEvent event,
      @Mocked RegistryUtils ru) throws Exception {
    new Expectations() {
      {
        RegistryUtils.destory();
      }
    };
    CseApplicationListener cal = new CseApplicationListener();
    cal.onApplicationEvent(event);
  }
}
