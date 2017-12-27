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
package io.servicecomb.core.definition.schema;

import javax.xml.ws.Holder;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import io.servicecomb.core.Const;
import io.servicecomb.core.Endpoint;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.definition.loader.SchemaLoader;
import io.servicecomb.core.unittest.UnitTestMeta;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.ReflectUtils;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import io.servicecomb.swagger.engine.SwaggerEnvironment;
import io.servicecomb.swagger.engine.SwaggerProducerOperation;
import io.servicecomb.swagger.engine.bootstrap.BootstrapNormal;
import io.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperFactory;
import io.servicecomb.swagger.invocation.converter.ConverterMgr;
import io.servicecomb.swagger.invocation.exception.CommonExceptionData;
import io.servicecomb.swagger.invocation.exception.InvocationException;

public class TestProducerSchemaFactory {
  private static SwaggerEnvironment swaggerEnv = new BootstrapNormal().boot();

  private static ProducerSchemaFactory producerSchemaFactory = new ProducerSchemaFactory();

  private static SchemaMeta schemaMeta;

  public static class TestProducerSchemaFactoryImpl {
    public int add(int x, int y) {
      return x + y;
    }
  }

  @BeforeClass
  public static void init() {
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    RegistryUtils.setServiceRegistry(serviceRegistry);

    ConverterMgr converterMgr = new ConverterMgr();
    ProducerArgumentsMapperFactory producerArgsMapperFactory = new ProducerArgumentsMapperFactory();
    producerArgsMapperFactory.setConverterMgr(converterMgr);

    MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();
    SchemaLoader schemaLoader = new SchemaLoader() {
      @Override
      public void putSelfBasePathIfAbsent(String microserviceName, String basePath) {
      }
    };
    CompositeSwaggerGeneratorContext compositeSwaggerGeneratorContext = new CompositeSwaggerGeneratorContext();

    producerSchemaFactory.setSwaggerEnv(swaggerEnv);
    ReflectUtils.setField(producerSchemaFactory, "microserviceMetaManager", microserviceMetaManager);
    ReflectUtils.setField(producerSchemaFactory, "schemaLoader", schemaLoader);
    ReflectUtils.setField(producerSchemaFactory,
        "compositeSwaggerGeneratorContext",
        compositeSwaggerGeneratorContext);

    BeanUtils.setContext(Mockito.mock(ApplicationContext.class));

    UnitTestMeta.init();

    schemaMeta = producerSchemaFactory.getOrCreateProducerSchema("app:ms",
        "schema",
        TestProducerSchemaFactoryImpl.class,
        new TestProducerSchemaFactoryImpl());
  }

  @AfterClass
  public static void teardown() {
    RegistryUtils.setServiceRegistry(null);
  }

  @Test
  public void testGetOrCreateProducer() throws Exception {
    OperationMeta operationMeta = schemaMeta.ensureFindOperation("add");
    Assert.assertEquals("add", operationMeta.getOperationId());

    SwaggerProducerOperation producerOperation = operationMeta.getExtData(Const.PRODUCER_OPERATION);

    Object addBody = Class.forName("cse.gen.app.ms.schema.addBody").newInstance();
    ReflectUtils.setField(addBody, "x", 1);
    ReflectUtils.setField(addBody, "y", 2);
    Invocation invocation = new Invocation((Endpoint) null, operationMeta, new Object[] {addBody}) {
      @Override
      public String getInvocationQualifiedName() {
        return "";
      }
    };
    Holder<Response> holder = new Holder<>();
    producerOperation.invoke(invocation, resp -> {
      holder.value = resp;
    });
    Assert.assertEquals(3, (int) holder.value.getResult());

    invocation.setSwaggerArguments(new Object[] {1, 2});
    producerOperation.invoke(invocation, resp -> {
      holder.value = resp;
    });
    Assert.assertEquals(true, holder.value.isFailed());
    InvocationException exception = (InvocationException) holder.value.getResult();
    CommonExceptionData data = (CommonExceptionData) exception.getErrorData();
    Assert.assertEquals("Cse Internal Server Error", data.getMessage());
  }
}
