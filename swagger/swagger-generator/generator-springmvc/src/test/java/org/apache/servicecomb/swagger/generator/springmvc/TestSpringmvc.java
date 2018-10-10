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

package org.apache.servicecomb.swagger.generator.springmvc;

import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestSpringmvc {
  ClassLoader classLoader = new ClassLoader() {
  };

  SwaggerGeneratorContext context = new SpringmvcSwaggerGeneratorContext();

  @After
  public void tearDown() {
    JavassistUtils.clearByClassLoader(classLoader);
  }

  @Test
  public void testMultiDefaultPath() {
    UnitTestSwaggerUtils.testException(
        "Only allowed one default path. org.apache.servicecomb.swagger.generator.springmvc.MultiDefaultPath:p2",
        context,
        MultiDefaultPath.class);
  }

  @Test
  public void testResponseEntity() {
    UnitTestSwaggerUtils.testSwagger(classLoader, "schemas/responseEntity.yaml", context, MethodResponseEntity.class);
  }

  @Test
  public void testEmptyPath() {
    UnitTestSwaggerUtils.testSwagger(classLoader, "schemas/emptyPath.yaml", context, Echo.class, "emptyPath");
    UnitTestSwaggerUtils.testSwagger(classLoader, "schemas/MethodEmptyPath.yaml",
        context,
        MethodEmptyPath.class);
  }

  @Test
  public void testMixupAnnotations() {
    UnitTestSwaggerUtils
        .testSwagger(classLoader, "schemas/mixupAnnotations.yaml", context, MethodMixupAnnotations.class);
  }

  @Test
  public void testDefaultParameter() {
    UnitTestSwaggerUtils
        .testSwagger(classLoader, "schemas/defaultParameter.yaml", context, MethodDefaultParameter.class);
  }

  @Test
  public void testInheritHttpMethod() {
    UnitTestSwaggerUtils
        .testSwagger(classLoader, "schemas/inheritHttpMethod.yaml", context, Echo.class, "inheritHttpMethod");
  }

  @Test
  public void testRawJsonStringMethod() {
    UnitTestSwaggerUtils
        .testSwagger(classLoader, "schemas/rawJsonStringMethod.yaml", context, Echo.class, "rawJsonStringMethod");
  }

  @Test
  public void testClassMethodNoPath() {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.springmvc.ClassMethodNoPath:noPath",
        "Path must not both be empty in class and method",
        context,
        ClassMethodNoPath.class,
        "noPath");
  }

  @Test
  public void testClassMethodNoHttpMetod() {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.springmvc.ClassMethodNoHttpMethod:noHttpMethod",
        "HttpMethod must not both be empty in class and method",
        context,
        ClassMethodNoHttpMethod.class);
  }

  @Test
  public void testMethodMultiHttpMethod() {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.springmvc.Echo:multiHttpMethod",
        "not allowed multi http method for org.apache.servicecomb.swagger.generator.springmvc.Echo:multiHttpMethod",
        context,
        Echo.class,
        "multiHttpMethod");
  }

  @Test
  public void testClassMultiHttpMethod() {
    UnitTestSwaggerUtils.testException(
        "not allowed multi http method for org.apache.servicecomb.swagger.generator.springmvc.ClassMultiHttpMethod",
        context,
        ClassMultiHttpMethod.class);
  }

  @Test
  public void testMethodMultiPathUsingRequestMapping() {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingRequestMapping",
        "not allowed multi path for org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingRequestMapping",
        context,
        MethodMultiPath.class,
        "usingRequestMapping");
  }

  @Test
  public void testMethodMultiPathUsingGetMapping() {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingGetMapping",
        "not allowed multi path for org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingGetMapping",
        context,
        MethodMultiPath.class,
        "usingGetMapping");
  }

  @Test
  public void testMethodMultiPathUsingPutMapping() {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingPutMapping",
        "not allowed multi path for org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingPutMapping",
        context,
        MethodMultiPath.class,
        "usingPutMapping");
  }

  @Test
  public void testMethodMultiPathUsingPostMapping() {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingPostMapping",
        "not allowed multi path for org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingPostMapping",
        context,
        MethodMultiPath.class,
        "usingPostMapping");
  }

  @Test
  public void testMethodMultiPathUsingPatchMapping() {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingPatchMapping",
        "not allowed multi path for org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingPatchMapping",
        context,
        MethodMultiPath.class,
        "usingPatchMapping");
  }

  @Test
  public void testMethodMultiPathUsingDeleteMapping() {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingDeleteMapping",
        "not allowed multi path for org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingDeleteMapping",
        context,
        MethodMultiPath.class,
        "usingDeleteMapping");
  }

  @Test
  public void testClassMultiPath() {
    UnitTestSwaggerUtils.testException(
        "not support multi path for org.apache.servicecomb.swagger.generator.springmvc.ClassMultiPath",
        context,
        ClassMultiPath.class);
  }

  @Test
  public void testComposite() {
    CompositeSwaggerGeneratorContext composite = new CompositeSwaggerGeneratorContext();
    SwaggerGeneratorContext context = composite.selectContext(Echo.class);

    Assert.assertEquals(SpringmvcSwaggerGeneratorContext.class, context.getClass());
  }

  @Test
  public void testEnumBody() {
    UnitTestSwaggerUtils
        .testSwagger(classLoader, "schemas/enumBody.yaml", context, Echo.class, "enumBody");
  }
}
