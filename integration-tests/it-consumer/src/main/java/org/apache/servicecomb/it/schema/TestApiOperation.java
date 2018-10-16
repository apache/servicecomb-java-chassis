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

package org.apache.servicecomb.it.schema;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;

public class TestApiOperation {
  MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroserviceMeta();

  protected Operation getOperation(String schemaId, String opName) {
    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta(schemaId);
    return schemaMeta.findOperation(opName).getSwaggerOperation();
  }

  @Test
  public void jaxrs_TestMediaType1() {
    Operation operation = getOperation("apiOperationJaxrsSchema", "testMediaType1");
    Assert.assertThat(operation.getConsumes(), Matchers.contains(MediaType.APPLICATION_JSON));
    Assert.assertThat(operation.getProduces(), Matchers.contains(MediaType.APPLICATION_XML));
  }

  @Test
  public void jaxrs_TestMediaType2() {
    Operation operation = getOperation("apiOperationJaxrsSchema", "testMediaType2");
    Assert.assertThat(operation.getConsumes(), Matchers.contains(MediaType.APPLICATION_JSON));
    Assert.assertThat(operation.getProduces(), Matchers.contains(MediaType.APPLICATION_XML));
  }

  @Test
  public void springMVC_TestMediaType1() {
    Operation operation = getOperation("apiOperationSpringMVCSchema", "testMediaType1");
    Assert.assertThat(operation.getConsumes(), Matchers.contains(MediaType.APPLICATION_JSON));
    Assert.assertThat(operation.getProduces(), Matchers.contains(MediaType.APPLICATION_XML));
  }

  @Test
  public void springMVC_TestMediaType2() {
    Operation operation = getOperation("apiOperationSpringMVCSchema", "testMediaType2");
    Assert.assertThat(operation.getConsumes(), Matchers.contains(MediaType.APPLICATION_JSON));
    Assert.assertThat(operation.getProduces(), Matchers.contains(MediaType.APPLICATION_XML));
  }

  @Test
  public void springMVC_TestSwaggerDefinitionMediaType() {
    Swagger swagger = microserviceMeta.findSchemaMeta("apiOperationSpringMVCSchema").getSwagger();
    Assert.assertThat(swagger.getConsumes(), Matchers.contains(MediaType.APPLICATION_JSON));
    Assert.assertThat(swagger.getProduces(), Matchers.contains(MediaType.APPLICATION_XML));
  }
}
