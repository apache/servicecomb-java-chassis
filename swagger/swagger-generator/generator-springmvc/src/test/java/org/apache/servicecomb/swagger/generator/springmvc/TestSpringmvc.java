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

import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.apache.servicecomb.swagger.generator.springmvc.model.DefaultParameterSchema;
import org.apache.servicecomb.swagger.generator.springmvc.model.SwaggerTestTarget;
import org.apache.servicecomb.swagger.generator.springmvc.model.SwaggerTestTarget_ValueOverWritePath;
import org.junit.jupiter.api.Test;

public class TestSpringmvc {
  @Test
  public void testMultiDefaultPath() {
    UnitTestSwaggerUtils.testException(
        "Duplicate operation path detected. method=org.apache.servicecomb.swagger.generator.springmvc.MultiDefaultPath:p2.",
        MultiDefaultPath.class);
  }

  @Test
  public void testResponseEntity() {
    UnitTestSwaggerUtils.testSwagger("schemas/responseEntity.yaml", MethodResponseEntity.class);
  }

  @Test
  public void testEcho() {
    UnitTestSwaggerUtils.testSwagger("schemas/echo.yaml", Echo.class);
  }

  @Test
  public void testMixupAnnotations() {
    UnitTestSwaggerUtils.testSwagger("schemas/mixupAnnotations.yaml", MethodMixupAnnotations.class);
  }

  @Test
  public void testDefaultParameter() {
    UnitTestSwaggerUtils.testSwagger("schemas/defaultParameter.yaml", MethodDefaultParameter.class);
  }


  @Test
  public void testClassMethodNoPath() {
    UnitTestSwaggerUtils.testException(
        "Generate swagger operation failed, method=ClassMethodNoPath:noPath, "
            + "cause=Path must not both be empty in class and method",
        ClassMethodNoPath.class,
        "noPath");
  }

  @Test
  public void testClassMethodNoHttpMetod() {
    UnitTestSwaggerUtils
        .testSwagger("schemas/requestMappingHttpMethod.yaml", ClassMethodNoHttpMethod.class, "noHttpMethod");
  }


  @Test
  public void testClassMultiHttpMethod() {
    UnitTestSwaggerUtils.testException(
        "not support multi http method, class=org.apache.servicecomb.swagger.generator.springmvc.ClassMultiHttpMethod.",
        ClassMultiHttpMethod.class);
  }

  @Test
  public void testMethodMultiPathUsingRequestMapping() {
    UnitTestSwaggerUtils.testException(
        "Generate swagger operation failed, method=MethodMultiPath:usingRequestMapping, cause=not allowed multi path.",
        MethodMultiPath.class,
        "usingRequestMapping");
  }

  @Test
  public void testMethodMultiPathUsingGetMapping() {
    UnitTestSwaggerUtils.testException(
        "Generate swagger operation failed, method=MethodMultiPath:usingGetMapping, cause=not allowed multi path.",
        MethodMultiPath.class,
        "usingGetMapping");
  }

  @Test
  public void testMethodMultiPathUsingPutMapping() {
    UnitTestSwaggerUtils.testException(
        "Generate swagger operation failed, method=MethodMultiPath:usingPutMapping, cause=not allowed multi path.",
        MethodMultiPath.class,
        "usingPutMapping");
  }

  @Test
  public void testMethodMultiPathUsingPostMapping() {
    UnitTestSwaggerUtils.testException(
        "Generate swagger operation failed, method=MethodMultiPath:usingPostMapping, cause=not allowed multi path.",
        MethodMultiPath.class,
        "usingPostMapping");
  }

  @Test
  public void testMethodMultiPathUsingPatchMapping() {
    UnitTestSwaggerUtils.testException(
        "Generate swagger operation failed, method=MethodMultiPath:usingPatchMapping, cause=not allowed multi path.",
        MethodMultiPath.class,
        "usingPatchMapping");
  }

  @Test
  public void testMethodMultiPathUsingDeleteMapping() {
    UnitTestSwaggerUtils.testException(
        "Generate swagger operation failed, method=MethodMultiPath:usingDeleteMapping, cause=not allowed multi path.",
        MethodMultiPath.class,
        "usingDeleteMapping");
  }

  @Test
  public void testClassMultiPath() {
    UnitTestSwaggerUtils.testException(
        "not support multi path, class=org.apache.servicecomb.swagger.generator.springmvc.ClassMultiPath.",
        ClassMultiPath.class);
  }


  @Test
  public void testDefaultParameterSchema() {
    UnitTestSwaggerUtils.testSwagger("schemas/DefaultParameterSchema.yaml", DefaultParameterSchema.class);
  }

  @Test
  public void swaggerTestTarget() {
    UnitTestSwaggerUtils.testSwagger("schemas/swaggerTestTarget.yaml", SwaggerTestTarget.class);
  }

  @Test
  public void swaggerTestTarget_ValueOverWritePath() {
    UnitTestSwaggerUtils
        .testSwagger("schemas/swaggerTestTarget_ValueOverWritePath.yaml", SwaggerTestTarget_ValueOverWritePath.class);
  }
}
