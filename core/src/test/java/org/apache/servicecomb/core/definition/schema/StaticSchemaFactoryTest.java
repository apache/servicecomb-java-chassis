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

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.definition.loader.SchemaLoader;
import org.apache.servicecomb.core.unittest.UnitTestMeta;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.serviceregistry.api.registry.StaticMicroservice;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class StaticSchemaFactoryTest {

  private static final StaticSchemaFactory staticSchemaFactory = new StaticSchemaFactory();

  private static final String APPLICATION_ID_KEY = "APPLICATION_ID";

  private static final String APP_ID_VALUE = "appIdTest";

  @BeforeClass
  public static void beforeClass() {
    System.setProperty(APPLICATION_ID_KEY, APP_ID_VALUE);
    new UnitTestMeta();

    CompositeSwaggerGeneratorContext compositeSwaggerGeneratorContext = new CompositeSwaggerGeneratorContext();
    ReflectUtils.setField(staticSchemaFactory, "compositeSwaggerGeneratorContext", compositeSwaggerGeneratorContext);

    SchemaLoader schemaLoader = new SchemaLoader();
    ReflectUtils.setField(staticSchemaFactory, "schemaLoader", schemaLoader);
  }

  @AfterClass
  public static void afterClass() {
    TestProducerSchemaFactory.teardown();
    System.clearProperty(APPLICATION_ID_KEY);
  }

  @Test
  public void testLoadSchema() {
    String serviceAndSchemaName = "3rdPartyService";
    StaticMicroservice staticMicroservice = new StaticMicroservice();
    staticMicroservice.setSchemaIntfCls(Test3rdPartyServiceIntf.class);
    MicroserviceMeta microserviceMeta = new MicroserviceMeta(serviceAndSchemaName);
    staticSchemaFactory.loadSchema(microserviceMeta, staticMicroservice);

    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta(serviceAndSchemaName);
    Assert.assertEquals(EXPECTED_SCHEMA_CONTENT,
        SwaggerUtils.swaggerToString(schemaMeta.getSwagger()));

    Assert.assertEquals(2, schemaMeta.getOperations().size());
    OperationMeta operationMeta = schemaMeta.ensureFindOperation("add");
    Assert.assertEquals("add", operationMeta.getOperationId());
    Method swaggerProducerMethod = operationMeta.getMethod();
    Class<?>[] parameterTypes = swaggerProducerMethod.getParameterTypes();
    Assert.assertEquals(2, parameterTypes.length);
    Assert.assertEquals(Integer.class, parameterTypes[0]);
    Assert.assertEquals(Integer.class, parameterTypes[1]);
    Assert.assertEquals(Integer.class, swaggerProducerMethod.getGenericReturnType());

    operationMeta = schemaMeta.ensureFindOperation("addAsync");
    Assert.assertEquals("addAsync", operationMeta.getOperationId());
    swaggerProducerMethod = operationMeta.getMethod();
    parameterTypes = swaggerProducerMethod.getParameterTypes();
    Assert.assertEquals(2, parameterTypes.length);
    Assert.assertEquals(Integer.class, parameterTypes[0]);
    Assert.assertEquals(Integer.class, parameterTypes[1]);
    // ensure reactive operation's return type is set correctly, not CompletableFuture<T>
    Assert.assertEquals(Integer.class, swaggerProducerMethod.getGenericReturnType());
  }

  @Path("/3rdParty")
  interface Test3rdPartyServiceIntf {

    @Path("/add")
    @GET
    int add(@QueryParam("x") int x, @QueryParam("y") int y);

    @Path("/addAsync")
    @GET
    CompletableFuture<Integer> addAsync(@QueryParam("x") int x, @QueryParam("y") int y);
  }

  private static final String EXPECTED_SCHEMA_CONTENT = "---\n"
      + "swagger: \"2.0\"\n"
      + "info:\n"
      + "  version: \"1.0.0\"\n"
      + "  title: \"swagger definition for org.apache.servicecomb.core.definition.schema.StaticSchemaFactoryTest$Test3rdPartyServiceIntf\"\n"
      + "  x-java-interface: \"cse.gen.appIdTest._3rdPartyService._3rdPartyService.Test3rdPartyServiceIntfIntf\"\n"
      + "basePath: \"/3rdParty\"\n"
      + "consumes:\n"
      + "- \"application/json\"\n"
      + "produces:\n"
      + "- \"application/json\"\n"
      + "paths:\n"
      + "  /add:\n"
      + "    get:\n"
      + "      operationId: \"add\"\n"
      + "      parameters:\n"
      + "      - name: \"x\"\n"
      + "        in: \"query\"\n"
      + "        required: false\n"
      + "        type: \"integer\"\n"
      + "        default: 0\n"
      + "        format: \"int32\"\n"
      + "      - name: \"y\"\n"
      + "        in: \"query\"\n"
      + "        required: false\n"
      + "        type: \"integer\"\n"
      + "        default: 0\n"
      + "        format: \"int32\"\n"
      + "      responses:\n"
      + "        200:\n"
      + "          description: \"response of 200\"\n"
      + "          schema:\n"
      + "            type: \"integer\"\n"
      + "            format: \"int32\"\n"
      + "  /addAsync:\n"
      + "    get:\n"
      + "      operationId: \"addAsync\"\n"
      + "      parameters:\n"
      + "      - name: \"x\"\n"
      + "        in: \"query\"\n"
      + "        required: false\n"
      + "        type: \"integer\"\n"
      + "        default: 0\n"
      + "        format: \"int32\"\n"
      + "      - name: \"y\"\n"
      + "        in: \"query\"\n"
      + "        required: false\n"
      + "        type: \"integer\"\n"
      + "        default: 0\n"
      + "        format: \"int32\"\n"
      + "      responses:\n"
      + "        200:\n"
      + "          description: \"response of 200\"\n"
      + "          schema:\n"
      + "            type: \"integer\"\n"
      + "            format: \"int32\"\n";
}