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

import org.apache.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestJaxrs {
  SwaggerGeneratorContext context = new JaxrsSwaggerGeneratorContext();

  @Test
  public void testMultiDefaultPath() {
    UnitTestSwaggerUtils.testException(
        "Only allowed one default path. org.apache.servicecomb.swagger.generator.jaxrs.MultiDefaultPath:p2",
        context,
        MultiDefaultPath.class);
  }

  @Test
  public void testResponse() throws Exception {
    UnitTestSwaggerUtils.testSwagger("schemas/response.yaml", context, Echo.class, "response");
  }

  @Test
  public void testInvalidResponse() throws Exception {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.jaxrs.Echo:invalidResponse",
        "Use ApiOperation or ApiResponses to declare response type",
        context,
        Echo.class,
        "invalidResponse");
  }

  @Test
  public void testEcho() throws Exception {
    UnitTestSwaggerUtils.testSwagger("schemas/echo.yaml", context, Echo.class, "echo");
  }

  @Test
  public void testForm() throws Exception {
    UnitTestSwaggerUtils.testSwagger("schemas/form.yaml", context, Echo.class, "form");
  }

  @Test
  public void testQuery() throws Exception {
    UnitTestSwaggerUtils.testSwagger("schemas/query.yaml", context, Echo.class, "query");
  }

  @Test
  public void testQueryComplex() throws Exception {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.jaxrs.Echo:queryComplex",
        "not allow complex type for query parameter, method=org.apache.servicecomb.swagger.generator.jaxrs.Echo:queryComplex, paramIdx=0, type=java.util.List<org.apache.servicecomb.swagger.generator.jaxrs.User>",
        context,
        Echo.class,
        "queryComplex");
  }

  @Test
  public void testCookie() throws Exception {
    UnitTestSwaggerUtils.testSwagger("schemas/cookie.yaml", context, Echo.class, "cookie");
  }

  @Test
  public void testEmptyPath() throws Exception {
    UnitTestSwaggerUtils.testSwagger("schemas/emptyPath.yaml", context, Echo.class, "emptyPath");
  }

  @Test
  public void testNonRestful() throws Exception {
    UnitTestSwaggerUtils.testSwagger("schemas/emptyContract.yaml", context, Echo.class, "ignoredNonRestful");
  }

  @Test
  public void testClassMethodNoPath() throws Exception {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.jaxrs.ClassMethodNoPath:p1",
        "Path must not both be empty in class and method",
        context,
        ClassMethodNoPath.class);
  }

  @Test
  public void testComposite() {
    CompositeSwaggerGeneratorContext composite = new CompositeSwaggerGeneratorContext();
    SwaggerGeneratorContext context = composite.selectContext(Echo.class);

    Assert.assertEquals(JaxrsSwaggerGeneratorContext.class, context.getClass());
  }

  @Test
  public void testRawJsonStringMethod() throws Exception {
    UnitTestSwaggerUtils.testSwagger("schemas/rawJsonStringMethod.yaml", context, Echo.class, "rawJsonStringMethod");
  }
}
