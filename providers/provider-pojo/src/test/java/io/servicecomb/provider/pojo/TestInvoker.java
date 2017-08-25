/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.pojo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import io.servicecomb.core.provider.consumer.ConsumerProviderManager;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.core.provider.consumer.ReferenceConfigUtils;
import io.servicecomb.swagger.engine.SwaggerConsumer;
import io.servicecomb.swagger.engine.bootstrap.BootstrapNormal;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;

public class TestInvoker {
  @Before
  public void setup() {
    ReferenceConfigUtils.setReady(true);
  }

  @After
  public void teardown() {
    ReferenceConfigUtils.setReady(false);
  }

  @Test
  public void testNotReady() throws Throwable {
    String exceptionMessage = "System is not ready for remote calls. "
        + "When beans are making remote calls in initialization, it's better to "
        + "implement io.servicecomb.core.BootListener and do it after EventType.AFTER_REGISTRY.";

    ReferenceConfigUtils.setReady(false);
    Invoker invoker = new Invoker("test", "schemaId", IPerson.class);

    try {
      invoker.invoke(null, null, null);
      Assert.fail("must throw exception");
    } catch (IllegalStateException e) {
      Assert.assertEquals(exceptionMessage, e.getMessage());
    }
  }

  @Test
  public void testNormalSchemaId(@Injectable ConsumerProviderManager manager,
      @Injectable ReferenceConfig config,
      @Injectable MicroserviceMeta microserviceMeta,
      @Injectable ConsumerSchemaFactory factory) {
    new Expectations() {
      {
        manager.getReferenceConfig("test");
        result = config;
        config.getMicroserviceMeta();
        result = microserviceMeta;
        microserviceMeta.ensureFindSchemaMeta("schemaId");
      }
    };
    CseContext.getInstance().setConsumerProviderManager(manager);
    CseContext.getInstance().setConsumerSchemaFactory(factory);
    CseContext.getInstance().setSwaggerEnvironment(new BootstrapNormal().boot());

    Invoker invoker = new Invoker("test", "schemaId", IPerson.class);
    invoker.prepare();

    SwaggerConsumer swaggerConsumer = Deencapsulation.getField(invoker, "swaggerConsumer");
    Assert.assertEquals(IPerson.class, swaggerConsumer.getConsumerIntf());
  }

  @Test
  public void testFindSchemaByConsumerInterface(@Injectable ConsumerProviderManager manager,
      @Injectable ReferenceConfig config,
      @Injectable MicroserviceMeta microserviceMeta,
      @Injectable ConsumerSchemaFactory factory) {
    new Expectations() {
      {
        manager.getReferenceConfig("test");
        result = config;
        config.getMicroserviceMeta();
        result = microserviceMeta;
        microserviceMeta.findSchemaMeta(IPerson.class);
      }
    };
    CseContext.getInstance().setConsumerProviderManager(manager);
    CseContext.getInstance().setConsumerSchemaFactory(factory);
    CseContext.getInstance().setSwaggerEnvironment(new BootstrapNormal().boot());

    Invoker invoker = new Invoker("test", null, IPerson.class);
    invoker.prepare();

    SwaggerConsumer swaggerConsumer = Deencapsulation.getField(invoker, "swaggerConsumer");
    Assert.assertEquals(IPerson.class, swaggerConsumer.getConsumerIntf());
  }

  @Test
  public void testConsumerInterfaceAsSchemaId(@Injectable ConsumerProviderManager manager,
      @Injectable ReferenceConfig config,
      @Injectable MicroserviceMeta microserviceMeta,
      @Injectable ConsumerSchemaFactory factory) {
    new Expectations() {
      {
        manager.getReferenceConfig("test");
        result = config;
        config.getMicroserviceMeta();
        result = microserviceMeta;
        microserviceMeta.findSchemaMeta(IPerson.class);
        result = null;
        microserviceMeta.ensureFindSchemaMeta(IPerson.class.getName());
      }
    };
    CseContext.getInstance().setConsumerProviderManager(manager);
    CseContext.getInstance().setConsumerSchemaFactory(factory);
    CseContext.getInstance().setSwaggerEnvironment(new BootstrapNormal().boot());

    Invoker invoker = new Invoker("test", null, IPerson.class);
    invoker.prepare();

    SwaggerConsumer swaggerConsumer = Deencapsulation.getField(invoker, "swaggerConsumer");
    Assert.assertEquals(IPerson.class, swaggerConsumer.getConsumerIntf());
  }
}
