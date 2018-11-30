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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.ws.Holder;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.definition.loader.SchemaListenerManager;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestSCBEngine {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void test(@Injectable ProducerProviderManager producerProviderManager,
      @Injectable ConsumerProviderManager consumerProviderManager,
      @Injectable TransportManager transportManager,
      @Injectable AppManager appManager) {
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getServiceRegistry().getAppManager();
        RegistryUtils.getInstanceCacheManager();
        RegistryUtils.run();
        RegistryUtils.destroy();
      }
    };
    AtomicBoolean configDestroy = new AtomicBoolean();
    new MockUp<ConfigUtil>() {
      @Mock
      void destroyConfigCenterConfigurationSource() {
        configDestroy.set(true);
      }
    };

    SchemaListenerManager schemaListenerManager = Mockito.mock(SchemaListenerManager.class);

    VertxUtils.getOrCreateVertxByName("transport", null);

    SCBEngine engine = new SCBEngine();
    engine.setBootListenerList(new ArrayList<>());
    engine.setConsumerProviderManager(consumerProviderManager);
    engine.setProducerProviderManager(producerProviderManager);
    engine.setTransportManager(transportManager);
    engine.setSchemaListenerManager(schemaListenerManager);

    ArchaiusUtils.setProperty(SCBEngine.CFG_KEY_WAIT_UP_TIMEOUT, 0);
    engine.init();
    ArchaiusUtils.updateProperty(SCBEngine.CFG_KEY_WAIT_UP_TIMEOUT, null);

    Assert.assertEquals(SCBStatus.STARTING, engine.getStatus());

    engine.destroy();

    Assert.assertEquals(SCBStatus.DOWN, engine.getStatus());
    Assert.assertTrue(configDestroy.get());
  }

  @Test
  public void createReferenceConfigForInvoke_up(@Mocked ConsumerProviderManager consumerProviderManager) {
    SCBEngine engine = new SCBEngine();
    engine.setStatus(SCBStatus.UP);
    engine.setConsumerProviderManager(consumerProviderManager);

    ReferenceConfig referenceConfig = engine.createReferenceConfigForInvoke(null, null, null);
    Assert.assertTrue(ReferenceConfig.class.isInstance(referenceConfig));
  }

  @Test
  public void createReferenceConfigForInvoke_down(@Mocked ConsumerProviderManager consumerProviderManager) {
    SCBEngine engine = new SCBEngine();
    engine.setStatus(SCBStatus.DOWN);
    engine.setConsumerProviderManager(consumerProviderManager);

    expectedException.expect(InvocationException.class);
    expectedException.expectMessage(
        Matchers
            .is("InvocationException: code=503;msg=CommonExceptionData [message=The request is rejected. Cannot process the request due to STATUS = DOWN]"));
    engine.createReferenceConfigForInvoke(null, null, null);
  }

  @Test
  public void getReferenceConfigForInvoke_up(@Mocked ConsumerProviderManager consumerProviderManager) {
    SCBEngine engine = new SCBEngine();
    engine.setStatus(SCBStatus.UP);
    engine.setConsumerProviderManager(consumerProviderManager);

    ReferenceConfig referenceConfig = engine.getReferenceConfigForInvoke(null);
    Assert.assertTrue(ReferenceConfig.class.isInstance(referenceConfig));
  }

  @Test
  public void getReferenceConfigForInvoke_down(@Mocked ConsumerProviderManager consumerProviderManager) {
    SCBEngine engine = new SCBEngine();
    engine.setStatus(SCBStatus.DOWN);
    engine.setConsumerProviderManager(consumerProviderManager);

    expectedException.expect(InvocationException.class);
    expectedException.expectMessage(
        Matchers
            .is("InvocationException: code=503;msg=CommonExceptionData [message=The request is rejected. Cannot process the request due to STATUS = DOWN]"));
    engine.getReferenceConfigForInvoke(null);
  }

  @Test
  public void setBootListenerList(@Mocked BootListener beanListener, @Mocked BootListener spiListener) {
    new Expectations(SPIServiceUtils.class) {
      {
        beanListener.getOrder();
        result = 1;
        spiListener.getOrder();
        result = 0;
        SPIServiceUtils.getOrLoadSortedService(BootListener.class);
        result = Arrays.asList(spiListener);
      }
    };

    SCBEngine engine = new SCBEngine();
    engine.setBootListenerList(Arrays.asList(beanListener));

    Assert.assertThat(engine.getBootListenerList(), Matchers.contains(spiListener, beanListener));
  }

  @Test
  public void bootEvent_refEngine() {
    Holder<SCBEngine> eventEngine = new Holder<>();
    SCBEngine engine = new SCBEngine();
    engine.setBootListenerList(Arrays.asList(event -> eventEngine.value = event.getScbEngine()));
    engine.triggerEvent(EventType.AFTER_REGISTRY);

    Assert.assertNotNull(eventEngine.value);
  }
}
