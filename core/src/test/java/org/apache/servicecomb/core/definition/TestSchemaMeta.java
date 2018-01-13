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

import org.apache.servicecomb.swagger.generator.core.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.Swagger;
import mockit.Mocked;

public class TestSchemaMeta {
  static interface V1 {
    void a();
  }

  static interface V2 extends V1 {
    void b();
  }

  @Test
  public void testMethodNotExist(@Mocked OperationMeta operationMeta) {
    Swagger swagger = UnitTestSwaggerUtils.generateSwagger(V2.class).getSwagger();
    // make swagger have more operations than interface
    swagger.getInfo().setVendorExtension(SwaggerConst.EXT_JAVA_INTF, V1.class.getName());

    MicroserviceMeta microserviceMeta = new MicroserviceMeta("app:ms");
    SchemaMeta schemaMeta = new SchemaMeta(swagger, microserviceMeta, "schemaId");
    Assert.assertEquals(1, schemaMeta.getOperations().size());
    Assert.assertNotNull(schemaMeta.findOperation("a"));
  }
}
