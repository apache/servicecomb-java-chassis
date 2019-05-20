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

package org.apache.servicecomb.swagger.generator.jaxrs;

import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.apache.servicecomb.swagger.generator.jaxrs.model.ConsumesAndProduces;
import org.junit.Test;

public class TestJaxrs {
  @Test
  public void testMultiDefaultPath() {
    UnitTestSwaggerUtils.testException(
        "Only allowed one default path. method=org.apache.servicecomb.swagger.generator.jaxrs.MultiDefaultPath:p2.",
        MultiDefaultPath.class);
  }

  @Test
  public void testResponse() {
    UnitTestSwaggerUtils.testSwagger("schemas/response.yaml", Echo.class, "response");
  }

  @Test
  public void responseText() {
    UnitTestSwaggerUtils.testSwagger("schemas/responseText.yaml", Echo.class, "responseText");
  }

  @Test
  public void testInvalidResponse() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.jaxrs.Echo:invalidResponse.",
        "Use ApiOperation or ApiResponses to declare response type",
        Echo.class,
        "invalidResponse");
  }

  @Test
  public void testEcho() {
    UnitTestSwaggerUtils.testSwagger("schemas/echo.yaml", Echo.class, "echo");
  }

  @Test
  public void testForm() {
    UnitTestSwaggerUtils.testSwagger("schemas/form.yaml", Echo.class, "form");
  }

  @Test
  public void testQuery() {
    UnitTestSwaggerUtils.testSwagger("schemas/query.yaml", Echo.class, "query");
  }

  @Test
  public void testQueryComplex() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.jaxrs.Echo:queryComplex.",
        "failed to fill parameter, parameterName=querys.",
        "not allow complex type for query parameter, type=java.util.List<org.apache.servicecomb.foundation.test.scaffolding.model.User>.",
        Echo.class,
        "queryComplex");
  }

  @Test
  public void testCookie() {
    UnitTestSwaggerUtils.testSwagger("schemas/cookie.yaml", Echo.class, "cookie");
  }

  @Test
  public void testEmptyPath() {
    UnitTestSwaggerUtils.testSwagger("schemas/emptyPath.yaml", Echo.class, "emptyPath");
  }

  @Test
  public void testNonRestful() {
    UnitTestSwaggerUtils.testSwagger("schemas/emptyContract.yaml", Echo.class, "ignoredNonRestful");
  }

  @Test
  public void testClassMethodNoPath() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.jaxrs.ClassMethodNoPath:p1.",
        "Path must not both be empty in class and method",
        ClassMethodNoPath.class);
  }

  @Test
  public void testRawJsonStringMethod() {
    UnitTestSwaggerUtils.testSwagger("schemas/rawJsonStringMethod.yaml", Echo.class, "rawJsonStringMethod");
  }

  @Test
  public void testEnumBody() {
    UnitTestSwaggerUtils.testSwagger("schemas/enumBody.yaml", Echo.class, "enumBody");
  }

  @Test
  public void consumesAndProduces() {
    UnitTestSwaggerUtils.testSwagger("schemas/consumes.yaml", ConsumesAndProduces.class);
  }

  @Test
  public void aggregatedParam() {
    UnitTestSwaggerUtils.testSwagger("schemas/aggregatedParam.yaml", Echo.class, "aggregatedParam");
  }

  @Test
  public void beanParamDefaultBody() {
    UnitTestSwaggerUtils
        .testSwagger("schemas/beanParamDefaultBody.yaml", Echo.class, "beanParamDefaultBody");
  }

  @Test
  public void beanParamWithJsonIgnoredTagged() {
    UnitTestSwaggerUtils
        .testSwagger("schemas/beanParamWithJsonIgnoredTagged.yaml", Echo.class, "beanParamWithJsonIgnoredTagged");
  }

  @Test
  public void beanParamWithPart() {
    UnitTestSwaggerUtils.testSwagger("schemas/beanParamWithPart.yaml", Echo.class, "beanParamWithPart");
  }

  @Test
  public void nestedListString() {
    UnitTestSwaggerUtils.testSwagger("schemas/nestedListString.yaml", Echo.class, "nestedListString");
  }

  @Test
  public void beanParamComplexField() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.jaxrs.Echo:beanParamComplexField.",
        "failed to fill parameter, parameterName=q.",
        "not allow complex type for query parameter, type=org.apache.servicecomb.swagger.generator.jaxrs.model.AggregatedParam.",
        Echo.class,
        "beanParamComplexField");
  }

  @Test
  public void beanParamComplexSetter() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.jaxrs.Echo:beanParamComplexSetter.",
        "failed to fill parameter, parameterName=h.",
        "not allow complex type for header parameter, type=org.apache.servicecomb.swagger.generator.jaxrs.model.AggregatedParam.",
        Echo.class,
        "beanParamComplexSetter");
  }

  @Test

  public void beanParamInvalidDefaultBody() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.jaxrs.Echo:beanParamInvalidDefaultBody.",
        "defined 2 body parameter.",
        Echo.class,
        "beanParamInvalidDefaultBody");
  }
}
