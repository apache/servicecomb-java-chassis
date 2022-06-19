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
package org.apache.servicecomb.it.testcase;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.junit.jupiter.api.Test;

import io.swagger.models.ModelImpl;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import org.junit.jupiter.api.Assertions;

public class TestApiParam {
  MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroserviceMeta();

  Parameter parameter;

  protected void check(String schemaId, String opName) {
    check(schemaId, opName, opName);
  }

  protected void check(String schemaId, String opName, String paramType) {
    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta(schemaId);
    OperationMeta operationMeta = schemaMeta.findOperation(opName);
    parameter = operationMeta.getSwaggerOperation().getParameters().get(0);
    Assertions.assertEquals("desc of " + opName + " param", parameter.getDescription());
    Assertions.assertEquals(paramType, parameter.getIn());
  }

  @Test
  public void pojoModel() {
    check("apiParamPojo", "model", "body");
  }

  @Test
  public void pojoSimple() {
    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta("apiParamPojo");
    OperationMeta operationMeta = schemaMeta.findOperation("simple");
    parameter = operationMeta.getSwaggerOperation().getParameters().get(0);
    ModelImpl model = SwaggerUtils.getModelImpl(schemaMeta.getSwagger(), (BodyParameter) parameter);
    Assertions.assertEquals("desc of simple param", model.getProperties().get("input").getDescription());
  }

  @Test
  public void jaxrsBody() {
    check("apiParamJaxrs", "body");
  }

  @Test
  public void jaxrsQuery() {
    check("apiParamJaxrs", "query");
  }

  @Test
  public void jaxrsQueryArray() {
    check("apiParamJaxrs", "queryArr", "query");

    Assertions.assertEquals("multi", ((QueryParameter) parameter).getCollectionFormat());
  }

  @Test
  public void jaxrsHeader() {
    check("apiParamJaxrs", "header");
  }

  @Test
  public void jaxrsCookie() {
    check("apiParamJaxrs", "cookie");
  }

  @Test
  public void jaxrsForm() {
    check("apiParamJaxrs", "form", "formData");
  }

  @Test
  public void springmvcBody() {
    check("apiParamSpringmvc", "body");

    Assertions.assertTrue(parameter.getRequired());
    Assertions.assertEquals("modelEx", parameter.getName());
    Assertions.assertEquals("v1", ((BodyParameter) parameter).getExamples().get("k1"));
    Assertions.assertEquals("v2", ((BodyParameter) parameter).getExamples().get("k2"));
  }

  @Test
  public void springmvcQuery() {
    check("apiParamSpringmvc", "query");

    Assertions.assertTrue(parameter.getRequired());
    Assertions.assertTrue(parameter.isReadOnly());
    Assertions.assertEquals("inputEx", parameter.getName());
    Assertions.assertEquals(10L, ((QueryParameter) parameter).getExample());
    Assertions.assertNull(((QueryParameter) parameter).getCollectionFormat());
  }

  @Test
  public void springmvcQueryArray() {
    check("apiParamSpringmvc", "queryArr", "query");

    Assertions.assertTrue(parameter.getRequired());
    Assertions.assertTrue(parameter.isReadOnly());
    Assertions.assertEquals("inputEx", parameter.getName());
    Assertions.assertEquals("10", ((QueryParameter) parameter).getExample());
    Assertions.assertEquals("csv", ((QueryParameter) parameter).getCollectionFormat());
  }

  @Test
  public void springmvcHeader() {
    check("apiParamSpringmvc", "header");
  }

  @Test
  public void springmvcCookie() {
    check("apiParamSpringmvc", "cookie");
  }

  @Test
  public void springmvcForm() {
    check("apiParamSpringmvc", "form", "formData");
  }
}
