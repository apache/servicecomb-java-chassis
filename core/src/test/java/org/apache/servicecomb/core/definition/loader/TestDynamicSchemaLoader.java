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
package org.apache.servicecomb.core.definition.loader;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.unittest.UnitTestMeta;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDynamicSchemaLoader {

  private static SchemaLoader loader = new SchemaLoader();

  private static Microservice microservice;

  @BeforeClass
  public static void init() {
    new UnitTestMeta();

    SchemaListenerManager schemaListenerManager = new SchemaListenerManager();
    schemaListenerManager.setSchemaListenerList(Collections.emptyList());

    CseContext context = CseContext.getInstance();
    context.setSchemaLoader(loader);
    context.setSchemaListenerManager(schemaListenerManager);

    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    serviceRegistry.init();

    microservice = serviceRegistry.getMicroservice();
    RegistryUtils.setServiceRegistry(serviceRegistry);
  }

  @AfterClass
  public static void teardown() {
    RegistryUtils.setServiceRegistry(null);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testRegisterSchemas() {
    DynamicSchemaLoader.INSTANCE.registerSchemas("classpath*:test/test/schema.yaml");
    SchemaMeta schemaMeta = SCBEngine.getInstance().getProducerMicroserviceMeta().ensureFindSchemaMeta("schema");
    Assert.assertEquals("cse.gen.app.perfClient.schema", schemaMeta.getPackageName());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testRegisterShemasAcrossApp() {
    //we can not register repeat data
    init();
    //as we can not set microserviceName any more, hence we should use the default name
    DynamicSchemaLoader.INSTANCE
        .registerSchemas(RegistryUtils.getMicroservice().getServiceName(), "classpath*:test/test/schema.yaml");
    SchemaMeta schemaMeta =
        SCBEngine.getInstance().getProducerMicroserviceMeta().ensureFindSchemaMeta("schema");
    Assert.assertEquals("cse.gen.app.perfClient.schema", schemaMeta.getPackageName());
  }

  @Test
  public void testPutSelfBasePathIfAbsent_noUrlPrefix() {
    System.clearProperty(Const.URL_PREFIX);
    microservice.setPaths(new ArrayList<>());

    loader.putSelfBasePathIfAbsent("perfClient", "/test");

    Assert.assertThat(microservice.getPaths().size(), Matchers.is(1));
    Assert.assertThat(microservice.getPaths().get(0).getPath(), Matchers.is("/test"));
  }

  @Test
  public void testPutSelfBasePathIfAbsent_WithUrlPrefix() {
    System.setProperty(Const.URL_PREFIX, "/root/rest");
    microservice.setPaths(new ArrayList<>());

    loader.putSelfBasePathIfAbsent("perfClient", "/test");

    Assert.assertThat(microservice.getPaths().size(), Matchers.is(1));
    Assert.assertThat(microservice.getPaths().get(0).getPath(), Matchers.is("/root/rest/test"));

    System.clearProperty(Const.URL_PREFIX);
  }

  @Test
  public void testPutSelfBasePathIfAbsent_WithUrlPrefix_StartWithUrlPrefix() {
    System.setProperty(Const.URL_PREFIX, "/root/rest");
    microservice.setPaths(new ArrayList<>());

    loader.putSelfBasePathIfAbsent("perfClient", "/root/rest/test");

    Assert.assertThat(microservice.getPaths().size(), Matchers.is(1));
    Assert.assertThat(microservice.getPaths().get(0).getPath(), Matchers.is("/root/rest/test"));

    System.clearProperty(Const.URL_PREFIX);
  }
}
