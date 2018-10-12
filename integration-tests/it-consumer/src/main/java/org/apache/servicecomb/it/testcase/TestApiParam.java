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
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;

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
    Assert.assertEquals("desc of " + opName + " param", parameter.getDescription());
    Assert.assertEquals(paramType, parameter.getIn());
  }

  @Test
  public void pojoModel() {
    check("apiParamPojo", "model", "body");
  }

  @Test
  public void pojoSimple() {
    check("apiParamPojo", "simple", "body");
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

    Assert.assertEquals("multi", ((QueryParameter) parameter).getCollectionFormat());
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

    Assert.assertTrue(parameter.getRequired());
    Assert.assertEquals("modelEx", parameter.getName());
    Assert.assertEquals("v1", ((BodyParameter) parameter).getExamples().get("k1"));
    Assert.assertEquals("v2", ((BodyParameter) parameter).getExamples().get("k2"));
  }

  @Test
  public void springmvcQuery() {
    check("apiParamSpringmvc", "query");

    Assert.assertTrue(parameter.getRequired());
    Assert.assertTrue(parameter.isReadOnly());
    Assert.assertTrue(parameter.getAllowEmptyValue());
    Assert.assertEquals("inputEx", parameter.getName());
    Assert.assertEquals(10L, ((QueryParameter) parameter).getExample());
    Assert.assertNull(((QueryParameter) parameter).getCollectionFormat());
  }

  @Test
  public void springmvcQueryArray() {
    check("apiParamSpringmvc", "queryArr", "query");

    Assert.assertTrue(parameter.getRequired());
    Assert.assertTrue(parameter.isReadOnly());
    Assert.assertTrue(parameter.getAllowEmptyValue());
    Assert.assertEquals("inputEx", parameter.getName());
    Assert.assertEquals("10", ((QueryParameter) parameter).getExample());
    Assert.assertEquals("csv", ((QueryParameter) parameter).getCollectionFormat());
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
