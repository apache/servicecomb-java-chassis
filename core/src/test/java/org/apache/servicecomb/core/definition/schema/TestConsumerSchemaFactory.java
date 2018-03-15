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

import java.util.Arrays;

import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.MicroserviceMetaManager;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.definition.loader.SchemaListener;
import org.apache.servicecomb.core.definition.loader.SchemaListenerManager;
import org.apache.servicecomb.core.definition.loader.SchemaLoader;
import org.apache.servicecomb.core.unittest.UnitTestMeta;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import org.apache.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestConsumerSchemaFactory {
  private static ConsumerSchemaFactory consumerSchemaFactory = new ConsumerSchemaFactory();

  private static SchemaListener schemaListener = new SchemaListener() {

    @Override
    public void onSchemaLoaded(SchemaMeta... schemaMetas) {

    }
  };

  static interface Intf {
    int add(int x, int y);
  }

  class TestConsumerSchemaFactoryImpl {
    public int add(int x, int y) {
      return x + y;
    }
  }

  @BeforeClass
  public static void init() {
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    RegistryUtils.setServiceRegistry(serviceRegistry);

    SchemaListenerManager schemaListenerManager = new SchemaListenerManager();
    schemaListenerManager.setSchemaListenerList(Arrays.asList(schemaListener));

    MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();
    SchemaLoader schemaLoader = new SchemaLoader() {
      @Override
      public void putSelfBasePathIfAbsent(String microserviceName, String basePath) {
      }
    };
    CompositeSwaggerGeneratorContext compositeSwaggerGeneratorContext = new CompositeSwaggerGeneratorContext();

    ReflectUtils.setField(consumerSchemaFactory, "schemaListenerManager", schemaListenerManager);
    ReflectUtils.setField(consumerSchemaFactory, "microserviceMetaManager", microserviceMetaManager);
    ReflectUtils.setField(consumerSchemaFactory, "schemaLoader", schemaLoader);
    ReflectUtils.setField(consumerSchemaFactory,
        "compositeSwaggerGeneratorContext",
        compositeSwaggerGeneratorContext);

    SchemaMeta schemaMeta = new UnitTestMeta().getOrCreateSchemaMeta(TestConsumerSchemaFactoryImpl.class);
    String content = UnitTestSwaggerUtils.pretty(schemaMeta.getSwagger());

    Microservice microservice = new Microservice();
    microservice.setAppId("app");
    microservice.setServiceId("0");
    microservice.setServiceName("ms");
    microservice.setVersion("1.0.0");
    microservice.addSchema("schema", content);
    serviceRegistry.getServiceRegistryClient().registerMicroservice(microservice);

    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setServiceId("0");
    instance.setInstanceId("0");
    serviceRegistry.getServiceRegistryClient().registerMicroserviceInstance(instance);
  }

  @AfterClass
  public static void teardown() {
    RegistryUtils.setServiceRegistry(null);
  }

  @Test
  public void testGetOrCreateConsumer() {
    MicroserviceMeta microserviceMeta =
        consumerSchemaFactory.getOrCreateMicroserviceMeta("ms", "latest");
    OperationMeta operationMeta = microserviceMeta.ensureFindOperation("schema.add");
    Assert.assertEquals("add", operationMeta.getOperationId());
  }
}
