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

package org.apache.servicecomb.provider.pojo;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfigUtils;
import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.engine.bootstrap.BootstrapNormal;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapper;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestInvoker {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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
        + "implement " + BootListener.class.getName() + " and do it after EventType.AFTER_REGISTRY.";

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

  @Test
  public void syncInvoke_normal(@Mocked Invocation invocation,
      @Mocked SwaggerConsumerOperation consumerOperation,
      @Mocked ConsumerResponseMapper mapper) {
    Response response = Response.ok("1");
    new MockUp<InvokerUtils>() {
      @Mock
      Response innerSyncInvoke(Invocation invocation) {
        return response;
      }
    };
    new Expectations() {
      {
        consumerOperation.getResponseMapper();
        result = mapper;
        mapper.mapResponse(response);
        result = 1;
      }
    };

    Invoker invoker = new Invoker("test", null, IPerson.class);
    Object result = invoker.syncInvoke(invocation, consumerOperation);
    Assert.assertEquals(1, result);
  }

  @Test
  public void syncInvoke_failed(@Mocked Invocation invocation,
      @Mocked SwaggerConsumerOperation consumerOperation,
      @Mocked ConsumerResponseMapper mapper) {
    Throwable error = new Error("failed");
    Response response = Response.createConsumerFail(error);
    new MockUp<InvokerUtils>() {
      @Mock
      Response innerSyncInvoke(Invocation invocation) {
        return response;
      }
    };

    expectedException.expect(InvocationException.class);
    expectedException.expectCause(Matchers.sameInstance(error));

    Invoker invoker = new Invoker("test", null, IPerson.class);
    invoker.syncInvoke(invocation, consumerOperation);
  }

  @Test
  public void completableFutureInvoke_normal(@Mocked Invocation invocation,
      @Mocked SwaggerConsumerOperation consumerOperation,
      @Mocked ConsumerResponseMapper mapper) {
    Response response = Response.ok("1");
    new MockUp<InvokerUtils>() {
      @Mock
      void reactiveInvoke(Invocation invocation, AsyncResponse asyncResp) {
        asyncResp.handle(response);;
      }
    };
    new Expectations() {
      {
        consumerOperation.getResponseMapper();
        result = mapper;
        mapper.mapResponse(response);
        result = 1;
      }
    };

    Invoker invoker = new Invoker("test", null, IPerson.class);
    CompletableFuture<Object> future = invoker.completableFutureInvoke(invocation, consumerOperation);
    future.whenComplete((result, ex) -> {
      Assert.assertEquals(1, result);
      Assert.assertEquals(null, ex);
    });
  }

  @Test
  public void completableFutureInvoke_failed(@Mocked Invocation invocation,
      @Mocked SwaggerConsumerOperation consumerOperation,
      @Mocked ConsumerResponseMapper mapper) {
    Throwable error = new Error("failed");
    Response response = Response.createConsumerFail(error);
    new MockUp<InvokerUtils>() {
      @Mock
      void reactiveInvoke(Invocation invocation, AsyncResponse asyncResp) {
        asyncResp.handle(response);;
      }
    };

    Invoker invoker = new Invoker("test", null, IPerson.class);
    CompletableFuture<Object> future = invoker.completableFutureInvoke(invocation, consumerOperation);
    future.whenComplete((result, ex) -> {
      Assert.assertEquals(null, result);
      Assert.assertSame(error, ex);
    });
  }
}
