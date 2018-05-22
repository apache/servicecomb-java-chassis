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

import org.apache.servicecomb.core.definition.loader.SchemaListenerManager;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Expectations;
import mockit.Injectable;

public class TestSCBEngine {
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

    SchemaListenerManager schemaListenerManager = Mockito.mock(SchemaListenerManager.class);

    VertxUtils.getOrCreateVertxByName("transport", null);

    SCBEngine engine = new SCBEngine();
    engine.setBootListenerList(new ArrayList<>());
    engine.setConsumerProviderManager(consumerProviderManager);
    engine.setProducerProviderManager(producerProviderManager);
    engine.setTransportManager(transportManager);
    engine.setSchemaListenerManager(schemaListenerManager);

    engine.init();

    Assert.assertEquals(SCBStatus.UP, engine.getStatus());

    engine.uninit();

    Assert.assertEquals(SCBStatus.DOWN, engine.getStatus());
  }
}
