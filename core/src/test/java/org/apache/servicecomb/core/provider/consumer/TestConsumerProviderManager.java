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

package org.apache.servicecomb.core.provider.consumer;

import static org.junit.Assert.fail;

import java.util.Collections;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersion;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.eventbus.EventBus;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestConsumerProviderManager {
  @Before
  public void setUp() throws Exception {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void tearDown() throws Exception {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void allowedNoProvider(@Mocked ConsumerSchemaFactory consumerSchemaFactory) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.getBeanFactory().registerSingleton(consumerSchemaFactory.getClass().getName(), consumerSchemaFactory);
    context.register(ConsumerProviderManager.class);
    // must not throw exception
    context.refresh();

    context.close();
  }

  private ReferenceConfig mockCreateReferenceConfig() {
    EventBus eventBus = new EventBus();
    AppManager appManager = new AppManager(eventBus);

    ConsumerProviderManager consumerProviderManager = new ConsumerProviderManager();
    consumerProviderManager.setAppManager(appManager);

    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.findServiceInstances(anyString, anyString, DefinitionConst.VERSION_RULE_ALL, null);
        result = Collections.emptyList();
      }
    };

    new MockUp<MicroserviceVersionRule>() {
      @Mock
      MicroserviceVersion getLatestMicroserviceVersion() {
        return Mockito.mock(MicroserviceVersion.class);
      }
    };
    return consumerProviderManager.createReferenceConfig("app:ms");
  }

  @Test
  public void createReferenceConfig_default() {
    ReferenceConfig referenceConfig = mockCreateReferenceConfig();

    Assert.assertEquals("app", referenceConfig.getMicroserviceVersionRule().getAppId());
    Assert.assertEquals("app:ms", referenceConfig.getMicroserviceVersionRule().getMicroserviceName());
    Assert.assertEquals("0.0.0+", referenceConfig.getMicroserviceVersionRule().getVersionRule().getVersionRule());
    Assert.assertEquals(Const.ANY_TRANSPORT, referenceConfig.getTransport());
  }

  @Test
  public void createReferenceConfig_config() {
    ArchaiusUtils.setProperty("servicecomb.references.app:ms.version-rule", "1.0.0+");
    ArchaiusUtils.setProperty("servicecomb.references.app:ms.transport", Const.RESTFUL);

    ReferenceConfig referenceConfig = mockCreateReferenceConfig();

    Assert.assertEquals("app", referenceConfig.getMicroserviceVersionRule().getAppId());
    Assert.assertEquals("app:ms", referenceConfig.getMicroserviceVersionRule().getMicroserviceName());
    Assert.assertEquals("1.0.0+", referenceConfig.getMicroserviceVersionRule().getVersionRule().getVersionRule());
    Assert.assertEquals(Const.RESTFUL, referenceConfig.getTransport());
  }

  @Test
  public void createReferenceConfig_ProviderNotFound() {
    EventBus eventBus = new EventBus();
    AppManager appManager = new AppManager(eventBus);

    ConsumerProviderManager consumerProviderManager = new ConsumerProviderManager();
    consumerProviderManager.setAppManager(appManager);

    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.findServiceInstances(anyString, anyString, DefinitionConst.VERSION_RULE_ALL, null);
        result = Collections.emptyList();
      }
    };

    new MockUp<MicroserviceVersionRule>() {
      @Mock
      String getAppId() {
        return "aId";
      }

      @Mock
      String getMicroserviceName() {
        return "ms";
      }
    };

    try {
      consumerProviderManager.createReferenceConfig("app:ms");
      fail("an IllegalStateException is expected!");
    } catch (Exception e) {
      Assert.assertEquals(IllegalStateException.class, e.getClass());
      Assert.assertEquals(
          "Probably invoke a service before it is registered, or no instance found for it, appId=aId, name=ms",
          e.getMessage());
      e.printStackTrace();
    }
  }
}
