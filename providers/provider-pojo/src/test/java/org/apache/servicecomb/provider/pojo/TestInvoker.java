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

import java.io.File;

import javax.servlet.http.Part;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.vertx.http.ReadStreamPart;
import org.apache.servicecomb.provider.pojo.definition.PojoConsumerMeta;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

import mockit.Deencapsulation;

public class TestInvoker {
//  @Rule
//  public ExpectedException expectedException = ExpectedException.none();
//
//  SCBEngine scbEngine = new SCBEngine();
//
//  @Before
//  public void setup() {
//    new MockUp<SCBEngine>() {
//      @Mock
//      SCBEngine getInstance() {
//        return scbEngine;
//      }
//    };
//    scbEngine.setStatus(SCBStatus.UP);
//  }
//
//  @Test
//  public void testNormalSchemaId(@Injectable ConsumerProviderManager manager,
//      @Injectable ReferenceConfig config,
//      @Injectable MicroserviceMeta microserviceMeta) {
//    new Expectations() {
//      {
//        manager.getReferenceConfig("test");
//        result = config;
//        config.getMicroserviceMeta();
//        result = microserviceMeta;
//      }
//    };
//    scbEngine.setConsumerProviderManager(manager);
//    CseContext.getInstance().setSwaggerEnvironment(new BootstrapNormal().boot());
//
//    Invoker invoker = new Invoker("test", "schemaId", IPerson.class);
//    InvokerMeta invokerMeta = invoker.createInvokerMeta();
//
//    Assert.assertEquals(IPerson.class, invokerMeta.swaggerConsumer.getConsumerIntf());
//  }
//
//  @Test
//  public void testFindSchemaByConsumerInterface(@Injectable ConsumerProviderManager manager,
//      @Injectable ReferenceConfig config,
//      @Injectable MicroserviceMeta microserviceMeta) {
//    new Expectations() {
//      {
//        manager.getReferenceConfig("test");
//        result = config;
//        config.getMicroserviceMeta();
//        result = microserviceMeta;
//        microserviceMeta.findSchemaMeta(IPerson.class);
//      }
//    };
//    scbEngine.setConsumerProviderManager(manager);
//    CseContext.getInstance().setSwaggerEnvironment(new BootstrapNormal().boot());
//
//    Invoker invoker = new Invoker("test", null, IPerson.class);
//    InvokerMeta invokerMeta = invoker.createInvokerMeta();
//
//    Assert.assertEquals(IPerson.class, invokerMeta.swaggerConsumer.getConsumerIntf());
//  }
//
//  @Test
//  public void testConsumerInterfaceAsSchemaId(@Injectable ConsumerProviderManager manager,
//      @Injectable ReferenceConfig config,
//      @Injectable MicroserviceMeta microserviceMeta) {
//    new Expectations() {
//      {
//        manager.getReferenceConfig("test");
//        result = config;
//        config.getMicroserviceMeta();
//        result = microserviceMeta;
//        microserviceMeta.findSchemaMeta(IPerson.class);
//        result = null;
//      }
//    };
//    scbEngine.setConsumerProviderManager(manager);
//    CseContext.getInstance().setSwaggerEnvironment(new BootstrapNormal().boot());
//
//    Invoker invoker = new Invoker("test", null, IPerson.class);
//    InvokerMeta invokerMeta = invoker.createInvokerMeta();
//    Assert.assertEquals(IPerson.class, invokerMeta.swaggerConsumer.getConsumerIntf());
//  }
//
//  @Test
//  public void syncInvoke_normal(@Mocked Invocation invocation,
//      @Mocked SwaggerConsumerOperation consumerOperation,
//      @Mocked ConsumerResponseMapper mapper) {
//    Response response = Response.ok("1");
//    new MockUp<InvokerUtils>() {
//      @Mock
//      Response innerSyncInvoke(Invocation invocation) {
//        return response;
//      }
//    };
//    new Expectations() {
//      {
//        consumerOperation.getResponseMapper();
//        result = mapper;
//        mapper.mapResponse(response);
//        result = 1;
//      }
//    };
//
//    Invoker invoker = new Invoker("test", null, IPerson.class);
//    Object result = invoker.syncInvoke(invocation, consumerOperation);
//    Assert.assertEquals(1, result);
//  }
//
//  @Test
//  public void syncInvoke_failed(@Mocked Invocation invocation,
//      @Mocked SwaggerConsumerOperation consumerOperation,
//      @Mocked ConsumerResponseMapper mapper) {
//    Throwable error = new RuntimeExceptionWithoutStackTrace("failed");
//    Response response = Response.createConsumerFail(error);
//    new MockUp<InvokerUtils>() {
//      @Mock
//      Response innerSyncInvoke(Invocation invocation) {
//        return response;
//      }
//    };
//
//    expectedException.expect(InvocationException.class);
//    expectedException.expectCause(Matchers.sameInstance(error));
//
//    Invoker invoker = new Invoker("test", null, IPerson.class);
//    invoker.syncInvoke(invocation, consumerOperation);
//  }
//
//  @Test
//  public void completableFutureInvoke_normal(@Mocked Invocation invocation,
//      @Mocked SwaggerConsumerOperation consumerOperation,
//      @Mocked ConsumerResponseMapper mapper) {
//    Response response = Response.ok("1");
//    new MockUp<InvokerUtils>() {
//      @Mock
//      void reactiveInvoke(Invocation invocation, AsyncResponse asyncResp) {
//        asyncResp.handle(response);
//      }
//    };
//    new Expectations() {
//      {
//        consumerOperation.getResponseMapper();
//        result = mapper;
//        mapper.mapResponse(response);
//        result = 1;
//      }
//    };
//
//    Invoker invoker = new Invoker("test", null, IPerson.class);
//    CompletableFuture<Object> future = invoker.completableFutureInvoke(invocation, consumerOperation);
//    future.whenComplete((result, ex) -> {
//      Assert.assertEquals(1, result);
//      Assert.assertEquals(null, ex);
//    });
//
//    Assert.assertThat(future, Matchers.instanceOf(InvocationContextCompletableFuture.class));
//  }
//
//  @Test
//  public void completableFutureInvoke_failed(@Mocked Invocation invocation,
//      @Mocked SwaggerConsumerOperation consumerOperation,
//      @Mocked ConsumerResponseMapper mapper) {
//    Throwable error = new RuntimeExceptionWithoutStackTrace("failed");
//    Response response = Response.createConsumerFail(error);
//    new MockUp<InvokerUtils>() {
//      @Mock
//      void reactiveInvoke(Invocation invocation, AsyncResponse asyncResp) {
//        asyncResp.handle(response);
//      }
//    };
//
//    Invoker invoker = new Invoker("test", null, IPerson.class);
//    CompletableFuture<Object> future = invoker.completableFutureInvoke(invocation, consumerOperation);
//    future.whenComplete((result, ex) -> {
//      Assert.assertEquals(null, result);
//      Assert.assertSame(error, ex);
//    });
//  }
//
//  @Test
//  public void createInvokerMeta_schemaNotInContract(@Injectable ConsumerProviderManager manager,
//      @Injectable ReferenceConfig config,
//      @Injectable MicroserviceMeta microserviceMeta) {
//    new Expectations() {
//      {
//        manager.getReferenceConfig("test");
//        result = config;
//        config.getMicroserviceMeta();
//        result = microserviceMeta;
//        microserviceMeta.findSchemaMeta("schemaId");
//        result = null;
//      }
//    };
//    scbEngine.setConsumerProviderManager(manager);
//    CseContext.getInstance().setSwaggerEnvironment(new BootstrapNormal().boot());
//
//    Invoker invoker = new Invoker("test", "schemaId", IPerson.class);
//
//    expectedException.expect(IllegalStateException.class);
//    expectedException.expectMessage(Matchers
//        .is("Schema not exist, microserviceName=test, schemaId=schemaId, consumer interface=org.apache.servicecomb.provider.pojo.IPerson; "
//            + "new producer not running or not deployed."));
//    invoker.createInvokerMeta();
//  }
//
//  @Test
//  public void invoke_methodNotInContract(@Mocked SwaggerConsumer swaggerConsumer,
//      @Mocked ReferenceConfig referenceConfig, @Mocked MicroserviceMeta microserviceMeta) {
//    Invoker invoker = new Invoker("test", null, IPerson.class);
//    InvokerMeta invokerMeta = new InvokerMeta(referenceConfig, microserviceMeta, null, swaggerConsumer);
//    Deencapsulation.setField(invoker, "invokerMeta", invokerMeta);
//    new Expectations() {
//      {
//        swaggerConsumer.findOperation(anyString);
//        result = null;
//        referenceConfig.getMicroserviceMeta();
//        result = microserviceMeta;
//      }
//    };
//
//    expectedException.expect(IllegalStateException.class);
//    expectedException.expectMessage(Matchers
//        .is("Consumer method org.apache.servicecomb.provider.pojo.IPerson:trim not exist in contract, "
//            + "microserviceName=test, schemaId=null; new producer not running or not deployed."));
//    invoker.invoke(null, ReflectUtils.findMethod(String.class, "trim"), null);
//  }

  public interface DownloadIntf {
    ReadStreamPart download();
  }

  public class DownloadSchema {
    public File download() {
      return null;
    }
  }

  @Test
  public void should_generate_response_meta_for_download() {
    SCBEngine scbEngine = new SCBBootstrap().useLocalRegistry().createSCBEngineForTest()
        .addProducerMeta("download", new DownloadSchema()).run();
    Invoker invoker = new Invoker(scbEngine.getProducerMicroserviceMeta().getMicroserviceName(), "download",
        DownloadIntf.class);
    Deencapsulation.invoke(invoker, "ensureStatusUp");
    PojoConsumerMeta meta = Deencapsulation.invoke(invoker, "refreshMeta");

    JavaType javaType = meta.findOperationMeta("download").getResponsesType();
    Assert.assertSame(Part.class, javaType.getRawClass());

    scbEngine.destroy();
  }
}
