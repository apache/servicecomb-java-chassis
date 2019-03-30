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
import org.apache.servicecomb.swagger.generator.springmvc.model.TestProducer;
import org.junit.Test;

public class TestSpringmvc {
  @Test
  public void testMultiDefaultPath() {
    UnitTestSwaggerUtils.testException(
        "Only allowed one default path. method=org.apache.servicecomb.swagger.generator.springmvc.MultiDefaultPath:p2.",
        MultiDefaultPath.class);
  }

  @Test
  public void testResponseEntity() {
    UnitTestSwaggerUtils.testSwagger("schemas/responseEntity.yaml", MethodResponseEntity.class);
  }

  @Test
  public void testEmptyPath() {
    UnitTestSwaggerUtils.testSwagger("schemas/emptyPath.yaml", Echo.class, "emptyPath");
    UnitTestSwaggerUtils.testSwagger("schemas/MethodEmptyPath.yaml", MethodEmptyPath.class);
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
  public void testInheritHttpMethod() {
    UnitTestSwaggerUtils.testSwagger("schemas/inheritHttpMethod.yaml", Echo.class, "inheritHttpMethod");
  }

  @Test
  public void testRawJsonStringMethod() {
    UnitTestSwaggerUtils.testSwagger("schemas/rawJsonStringMethod.yaml", Echo.class, "rawJsonStringMethod");
  }

  @Test
  public void testClassMethodNoPath() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.springmvc.ClassMethodNoPath:noPath.",
        "Path must not both be empty in class and method",
        ClassMethodNoPath.class,
        "noPath");
  }

  @Test
  public void testClassMethodNoHttpMetod() {
    UnitTestSwaggerUtils.testException(
        "HttpMethod must not both be empty in class and method, method=org.apache.servicecomb.swagger.generator.springmvc.ClassMethodNoHttpMethod:noHttpMethod.",
        ClassMethodNoHttpMethod.class);
  }

  @Test
  public void testMethodMultiHttpMethod() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.springmvc.Echo:multiHttpMethod.",
        "not allowed multi http method.",
        Echo.class,
        "multiHttpMethod");
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
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingRequestMapping.",
        "not allowed multi path.",
        MethodMultiPath.class,
        "usingRequestMapping");
  }

  @Test
  public void testMethodMultiPathUsingGetMapping() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingGetMapping.",
        "not allowed multi path.",
        MethodMultiPath.class,
        "usingGetMapping");
  }

  @Test
  public void testMethodMultiPathUsingPutMapping() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingPutMapping.",
        "not allowed multi path.",
        MethodMultiPath.class,
        "usingPutMapping");
  }

  @Test
  public void testMethodMultiPathUsingPostMapping() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingPostMapping.",
        "not allowed multi path.",
        MethodMultiPath.class,
        "usingPostMapping");
  }

  @Test
  public void testMethodMultiPathUsingPatchMapping() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingPatchMapping.",
        "not allowed multi path.",
        MethodMultiPath.class,
        "usingPatchMapping");
  }

  @Test
  public void testMethodMultiPathUsingDeleteMapping() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.springmvc.MethodMultiPath:usingDeleteMapping.",
        "not allowed multi path.",
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
  public void testEnumBody() {
    UnitTestSwaggerUtils.testSwagger("schemas/enumBody.yaml", Echo.class, "enumBody");
  }

  @Test
  public void testAsyncResponseEntity() {
    UnitTestSwaggerUtils.testSwagger("schemas/asyncResponseEntity.yaml", Echo.class, "asyncResponseEntity");
  }

  @Test
  public void testSimpleParam() {
    UnitTestSwaggerUtils.testSwagger("schemas/testSimpleParam.yaml", DefaultParameterSchema.class, "testSimpleParam");
  }

  @Test
  public void testObjectParam() {
    UnitTestSwaggerUtils.testSwagger("schemas/testObjectParam.yaml", DefaultParameterSchema.class, "testObjectParam");
  }

  @Test
  public void testMultiObjParamsWithSameFiledName() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.springmvc.model.DefaultParameterSchema:testMultiObjParamsWithSameFiledName.",
        "not support duplicated parameter, name=name.",
        DefaultParameterSchema.class,
        "testMultiObjParamsWithSameFiledName");
  }

  @Test
  public void testUnsupportedParamType() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.springmvc.model.DefaultParameterSchema:testUnsupportedParamType.",
        "not allow complex type for query parameter, type=java.util.List<org.apache.servicecomb.swagger.generator.springmvc.model.TestParam>.",
        DefaultParameterSchema.class,
        "testUnsupportedParamType");
  }

  @Test
  public void testSingleMediaType() {
    UnitTestSwaggerUtils.testSwagger("schemas/testSingleMediaType.yaml", TestProducer.class, "testSingleMediaType");
  }

  @Test
  public void testMultipleMediaType() {
    UnitTestSwaggerUtils.testSwagger("schemas/testMultipleMediaType.yaml", TestProducer.class, "testMultipleMediaType");
  }

  @Test
  public void testBlankMediaType() {
    UnitTestSwaggerUtils.testSwagger("schemas/testBlankMediaType.yaml", TestProducer.class, "testBlankMediaType");
  }

  @Test
  public void cookie() {
    UnitTestSwaggerUtils.testSwagger("schemas/cookie.yaml", Echo.class, "cookie");
  }

  @Test
  public void part() {
    UnitTestSwaggerUtils.testSwagger("schemas/part.yaml", Echo.class, "part");
  }

  @Test
  public void partArray() {
    UnitTestSwaggerUtils.testSwagger("schemas/partArray.yaml", Echo.class, "partArray");
  }

  @Test
  public void partList() {
    UnitTestSwaggerUtils.testSwagger("schemas/partList.yaml", Echo.class, "partList");
  }

  @Test
  public void partAnnotation() {
    UnitTestSwaggerUtils.testSwagger("schemas/partAnnotation.yaml", Echo.class, "partAnnotation");
  }

  @Test
  public void partArrayAnnotation() {
    UnitTestSwaggerUtils.testSwagger("schemas/partArrayAnnotation.yaml", Echo.class, "partArrayAnnotation");
  }

  @Test
  public void partListAnnotation() {
    UnitTestSwaggerUtils.testSwagger("schemas/partListAnnotation.yaml", Echo.class, "partListAnnotation");
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

  @Test
  public void testResponseEntityOptional() {
    UnitTestSwaggerUtils
        .testSwagger(classLoader, "schemas/testResponseEntityOptional.yaml", context, Echo.class,
            "testResponseEntityOptional");
  }

  @Test
  public void testCompletableFutureResponseEntityOptional() {
    UnitTestSwaggerUtils
        .testSwagger(classLoader, "schemas/testCompletableFutureResponseEntityOptional.yaml", context, Echo.class,
            "testCompletableFutureResponseEntityOptional");
  }
}
