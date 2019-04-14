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

package org.apache.servicecomb.core.definition;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executor;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.classloader.MicroserviceClassLoader;
import org.apache.servicecomb.core.definition.loader.SchemaListenerManager;
import org.apache.servicecomb.core.definition.loader.SchemaLoader;
import org.apache.servicecomb.core.definition.schema.StaticSchemaFactory;
import org.apache.servicecomb.core.definition.schema.StaticSchemaFactoryTest.Test3rdPartyServiceIntf;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import org.apache.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestMicroserviceMeta {
  MicroserviceMeta microserviceMeta = new MicroserviceMeta("app:microservice");

  @Test
  public void isConsumer() {
    Assert.assertTrue(microserviceMeta.isConsumer());
  }

  @Test
  public void classloader() {
    ClassLoader loader = new MicroserviceClassLoader("", "", "");
    microserviceMeta.setClassLoader(loader);
    Assert.assertSame(loader, microserviceMeta.getClassLoader());
  }

  @Test
  public void testGetSchemaMetas() {
    Collection<SchemaMeta> schemaMetas = microserviceMeta.getSchemaMetas();
    Assert.assertNotNull(schemaMetas);
  }

  @Test
  public void testGetExtData() {
    Object data = new Object();
    microserviceMeta.putExtData("pruthi", data);
    Object response = microserviceMeta.getExtData("pruthi");
    Assert.assertNotNull(response);
  }

  @Test
  public void testIntf(@Mocked SchemaMeta sm1, @Mocked SchemaMeta sm2) {
    Class<?> intf = Object.class;
    new Expectations() {
      {
        sm1.getSchemaId();
        result = "a";
        sm2.getSchemaId();
        result = "b";
        sm1.getSwaggerIntf();
        result = intf;
        sm2.getSwaggerIntf();
        result = intf;
      }
    };

    try {
      microserviceMeta.ensureFindSchemaMeta(intf);
      Assert.assertEquals(1, 2);
    } catch (Throwable e) {
      Assert.assertEquals(
          "No schema interface is java.lang.Object.",
          e.getMessage());
    }
    microserviceMeta.regSchemaMeta(sm1);
    Assert.assertEquals(sm1, microserviceMeta.findSchemaMeta(intf));
    Assert.assertEquals(sm1, microserviceMeta.ensureFindSchemaMeta(intf));

    microserviceMeta.regSchemaMeta(sm2);
    Assert.assertEquals(sm1, microserviceMeta.ensureFindSchemaMeta("a"));
    Assert.assertEquals(sm2, microserviceMeta.ensureFindSchemaMeta("b"));
    try {
      microserviceMeta.findSchemaMeta(intf);
      Assert.assertEquals(1, 2);
    } catch (Throwable e) {
      Assert.assertEquals(
          "More than one schema interface is java.lang.Object, please use schemaId to choose a schema.",
          e.getMessage());
    }
  }

  @Test
  public void priorityPropertyManager() {
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    RegistryUtils.setServiceRegistry(serviceRegistry);

    SCBEngine scbEngine = new SCBEngine();
    scbEngine.setStaticSchemaFactory(new StaticSchemaFactory());
    Deencapsulation.setField(scbEngine.getStaticSchemaFactory(), "compositeSwaggerGeneratorContext",
        new CompositeSwaggerGeneratorContext());
    Deencapsulation.setField(scbEngine.getStaticSchemaFactory(), "schemaLoader", new SchemaLoader());
    new Expectations(SCBEngine.class) {
      {
        SCBEngine.getInstance();
        result = scbEngine;
      }
    };
    new Expectations(BeanUtils.class) {
      {
        BeanUtils.getBean(anyString);
        result = (Executor) Runnable::run;
      }
    };

    SchemaListenerManager schemaListenerManager = new SchemaListenerManager();
    new Expectations(CseContext.class) {
      {
        CseContext.getInstance().getSchemaListenerManager();
        result = schemaListenerManager;
      }
    };

    serviceRegistry.registerMicroserviceMappingByEndpoints("ms", "1.0.0", Arrays.asList("rest://localhost:8080"),
        Test3rdPartyServiceIntf.class);
    serviceRegistry.getAppManager().getOrCreateMicroserviceVersions("app", "ms");

    Assert.assertEquals(1,
        serviceRegistry.getAppManager().getOrCreateMicroserviceManager("app").getVersionsByName().size());
    Assert.assertEquals(0, ConfigUtil.getAllDynamicProperties().values().stream()
        .mapToInt(p -> ConfigUtil.getCallbacks(p).size())
        .sum());
    Assert.assertEquals(2, scbEngine.getPriorityPropertyManager().getConfigObjectMap().size());
    Assert.assertEquals(0, scbEngine.getPriorityPropertyManager().getPriorityPropertyMap().size());

    Deencapsulation
        .setField(serviceRegistry.getAppManager().getOrCreateMicroserviceManager("app").getVersionsByName().get("ms"),
            "validated", false);
    // should unregister priority properties
    serviceRegistry.getAppManager().getOrCreateMicroserviceVersions("app", "ms");

    Assert.assertEquals(0,
        serviceRegistry.getAppManager().getOrCreateMicroserviceManager("app").getVersionsByName().size());
    Assert.assertEquals(0, ConfigUtil.getAllDynamicProperties().values().stream()
        .mapToInt(p -> ConfigUtil.getCallbacks(p).size())
        .sum());
    Assert.assertEquals(0, scbEngine.getPriorityPropertyManager().getConfigObjectMap().size());
    Assert.assertEquals(0, scbEngine.getPriorityPropertyManager().getPriorityPropertyMap().size());

    RegistryUtils.setServiceRegistry(null);
  }
}
