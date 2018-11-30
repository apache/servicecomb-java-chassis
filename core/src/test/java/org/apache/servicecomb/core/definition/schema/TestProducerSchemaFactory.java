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
package org.apache.servicecomb.core.definition.schema;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.ws.rs.core.Response.Status;
import javax.xml.ws.Holder;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.definition.loader.SchemaLoader;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.apache.servicecomb.core.unittest.UnitTestMeta;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.engine.bootstrap.BootstrapNormal;
import org.apache.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperFactory;
import org.apache.servicecomb.swagger.invocation.converter.ConverterMgr;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.swagger.models.Swagger;
import mockit.Mock;
import mockit.MockUp;

public class TestProducerSchemaFactory {
  private static SwaggerEnvironment swaggerEnv = new BootstrapNormal().boot();

  private static ProducerSchemaFactory producerSchemaFactory = new ProducerSchemaFactory();

  static boolean rejectAdd;

  public static class TestProducerSchemaFactoryImpl {
    public int add(int x, int y) {
      if (rejectAdd) {
        throw new Error("reject add");
      }
      return x + y;
    }

    public CompletableFuture<String> asyncAdd(int x, int y) {
      if (rejectAdd) {
        throw new Error("reject add");
      }
      return CompletableFuture.completedFuture(String.valueOf(x + y));
    }
  }

  static long nanoTime = 123;

  @BeforeClass
  public static void init() {
    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return nanoTime;
      }
    };

    new UnitTestMeta();

    ConverterMgr converterMgr = new ConverterMgr();
    ProducerArgumentsMapperFactory producerArgsMapperFactory = new ProducerArgumentsMapperFactory();
    producerArgsMapperFactory.setConverterMgr(converterMgr);

    SchemaLoader schemaLoader = new SchemaLoader();
    CompositeSwaggerGeneratorContext compositeSwaggerGeneratorContext = new CompositeSwaggerGeneratorContext();

    producerSchemaFactory.setSwaggerEnv(swaggerEnv);
    ReflectUtils.setField(producerSchemaFactory, "schemaLoader", schemaLoader);
    ReflectUtils.setField(producerSchemaFactory,
        "compositeSwaggerGeneratorContext",
        compositeSwaggerGeneratorContext);

    Executor reactiveExecutor = new ReactiveExecutor();
    Executor normalExecutor = (cmd) -> {
    };
    new MockUp<BeanUtils>() {
      @SuppressWarnings("unchecked")
      @Mock
      <T> T getBean(String name) {
        if (ExecutorManager.EXECUTOR_REACTIVE.equals(name)) {
          return (T) reactiveExecutor;
        }

        return (T) normalExecutor;
      }
    };
  }

  @AfterClass
  public static void teardown() {
    RegistryUtils.setServiceRegistry(null);
  }

  @Test
  public void testGetOrCreateProducer() throws Exception {
    SchemaMeta schemaMeta = producerSchemaFactory.getOrCreateProducerSchema("schema",
        TestProducerSchemaFactoryImpl.class,
        new TestProducerSchemaFactoryImpl());
    Swagger swagger = schemaMeta.getSwagger();
    Assert.assertEquals(swagger.getBasePath(), "/TestProducerSchemaFactoryImpl");
    OperationMeta operationMeta = schemaMeta.ensureFindOperation("add");
    Assert.assertEquals("add", operationMeta.getOperationId());

    SwaggerProducerOperation producerOperation = operationMeta.getExtData(Const.PRODUCER_OPERATION);
    //we can not set microserviceName any more,use the default name
    Object addBody = Class.forName("cse.gen.app.perfClient.schema.addBody").newInstance();
    ReflectUtils.setField(addBody, "x", 1);
    ReflectUtils.setField(addBody, "y", 2);
    Invocation invocation = new Invocation((Endpoint) null, operationMeta, new Object[] {addBody}) {
      @Override
      public String getInvocationQualifiedName() {
        return "";
      }
    };
    Holder<Response> holder = new Holder<>();
    rejectAdd = false;
    producerOperation.invoke(invocation, resp -> holder.value = resp);
    Assert.assertEquals(3, (int) holder.value.getResult());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartBusinessMethod());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishBusiness());

    nanoTime++;
    rejectAdd = true;
    producerOperation.invoke(invocation, resp -> holder.value = resp);
    Assert.assertEquals(true, holder.value.isFailed());
    InvocationException exception = holder.value.getResult();
    CommonExceptionData data = (CommonExceptionData) exception.getErrorData();
    Assert.assertEquals(ExceptionFactory.PRODUCER_INNER_STATUS_CODE, exception.getStatusCode());
    Assert.assertEquals("Cse Internal Server Error", data.getMessage());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartBusinessMethod());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishBusiness());

    nanoTime++;
    invocation.setSwaggerArguments(new Object[] {1, 2});
    producerOperation.invoke(invocation, resp -> holder.value = resp);
    Assert.assertEquals(true, holder.value.isFailed());
    exception = holder.value.getResult();
    data = (CommonExceptionData) exception.getErrorData();
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), exception.getStatusCode());
    Assert.assertEquals("Parameters not valid or types not match.", data.getMessage());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartBusinessMethod());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishBusiness());
  }

  @Test
  public void testGetOrCreateProducerWithPrefix() {
    ArchaiusUtils.setProperty(org.apache.servicecomb.serviceregistry.api.Const.REGISTER_URL_PREFIX, "true");
    System.setProperty(org.apache.servicecomb.serviceregistry.api.Const.URL_PREFIX, "/pojo/test");
    SchemaMeta schemaMeta = producerSchemaFactory.getOrCreateProducerSchema("schema2",
        TestProducerSchemaFactoryImpl.class,
        new TestProducerSchemaFactoryImpl());
    OperationMeta operationMeta = schemaMeta.ensureFindOperation("add");
    Assert.assertEquals("add", operationMeta.getOperationId());
    Swagger swagger = schemaMeta.getSwagger();
    Assert.assertEquals(swagger.getBasePath(), "/pojo/test/TestProducerSchemaFactoryImpl");

    ArchaiusUtils.resetConfig();
    System.getProperties().remove(org.apache.servicecomb.serviceregistry.api.Const.URL_PREFIX);
  }

  @Test
  public void testCompletableFuture() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    SchemaMeta schemaMeta = producerSchemaFactory.getOrCreateProducerSchema("schema",
        TestProducerSchemaFactoryImpl.class,
        new TestProducerSchemaFactoryImpl());
    OperationMeta operationMeta = schemaMeta.ensureFindOperation("asyncAdd");
    Assert.assertThat(operationMeta.getExecutor(), Matchers.instanceOf(ReactiveExecutor.class));

    SwaggerProducerOperation producerOperation = operationMeta.getExtData(Const.PRODUCER_OPERATION);
    //we can not set microserviceName any more,use the default name
    Object addBody = Class.forName("cse.gen.app.perfClient.schema.asyncAddBody").newInstance();
    ReflectUtils.setField(addBody, "x", 1);
    ReflectUtils.setField(addBody, "y", 2);
    Invocation invocation = new Invocation((Endpoint) null, operationMeta, new Object[] {addBody}) {
      @Override
      public String getInvocationQualifiedName() {
        return "";
      }
    };
    Holder<Response> holder = new Holder<>();
    rejectAdd = false;
    producerOperation.invoke(invocation, resp -> holder.value = resp);
    Assert.assertEquals("3", holder.value.getResult());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartBusinessMethod());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishBusiness());

    nanoTime++;
    rejectAdd = true;
    producerOperation.invoke(invocation, resp -> holder.value = resp);
    Assert.assertEquals(true, holder.value.isFailed());
    InvocationException exception = holder.value.getResult();
    CommonExceptionData data = (CommonExceptionData) exception.getErrorData();
    Assert.assertEquals(ExceptionFactory.PRODUCER_INNER_STATUS_CODE, exception.getStatusCode());
    Assert.assertEquals("Cse Internal Server Error", data.getMessage());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartBusinessMethod());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishBusiness());

    nanoTime++;
    invocation.setSwaggerArguments(new Object[] {1, 2});
    producerOperation.invoke(invocation, resp -> holder.value = resp);
    Assert.assertEquals(true, holder.value.isFailed());
    exception = holder.value.getResult();
    data = (CommonExceptionData) exception.getErrorData();
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), exception.getStatusCode());
    Assert.assertEquals("Parameters not valid or types not match.", data.getMessage());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartBusinessMethod());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishBusiness());
  }
}
