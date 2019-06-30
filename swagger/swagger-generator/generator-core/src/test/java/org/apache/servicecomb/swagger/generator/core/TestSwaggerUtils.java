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

package org.apache.servicecomb.swagger.generator.core;

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.collectParameterName;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.generator.core.schema.InvalidResponseHeader;
import org.apache.servicecomb.swagger.generator.core.schema.RepeatOperation;
import org.apache.servicecomb.swagger.generator.core.schema.Schema;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mockit.Expectations;

public class TestSwaggerUtils {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private void testSchemaMethod(String resultName, String... methodNames) {
    UnitTestSwaggerUtils.testSwagger("schemas/" + resultName + ".yaml",
        Schema.class,
        methodNames);
  }

  @Test
  public void testBoolean() {
    testSchemaMethod("boolean", "testboolean");
    testSchemaMethod("booleanObject", "testBoolean");
  }

  @Test
  public void testByte() {
    testSchemaMethod("byte", "testbyte");
    testSchemaMethod("byteObject", "testByte");
  }

  @Test
  public void testShort() {
    testSchemaMethod("short", "testshort");
    testSchemaMethod("shortObject", "testShort");
  }

  @Test
  public void testInt() {
    testSchemaMethod("int", "testint");
    testSchemaMethod("intObject", "testInteger");
  }

  @Test
  public void testLong() {
    testSchemaMethod("long", "testlong");
    testSchemaMethod("longObject", "testLong");
  }

  @Test
  public void testFloat() {
    testSchemaMethod("float", "testfloat");
    testSchemaMethod("floatObject", "testFloat");
  }

  @Test
  public void testDouble() {
    testSchemaMethod("double", "testdouble");
    testSchemaMethod("doubleObject", "testDouble");
  }

  @Test
  public void should_not_lost_ApiParam_description_when_wrap_parameter_to_body() {
    testSchemaMethod("wrapToBodyWithDesc", "wrapToBodyWithDesc");
  }

  @Test
  public void testOneEnum() {
    testSchemaMethod("oneEnum", "testOneEnum");
  }

  @Test
  public void testEnum() {
    testSchemaMethod("enum", "testEnum");
  }

  @Test
  public void testChar() {
    testSchemaMethod("char", "testchar");
    testSchemaMethod("charObject", "testChar");
  }

  @Test
  public void testBytes() {
    testSchemaMethod("bytes", "testbytes");
    testSchemaMethod("bytesObject", "testBytes");
  }

  @Test
  public void testString() {
    testSchemaMethod("string", "testString");
  }

  @Test
  public void testObject() {
    testSchemaMethod("object", "testObject");
  }

  @Test
  public void testArray() {
    testSchemaMethod("array", "testArray");
  }

  @Test
  public void testSet() {
    testSchemaMethod("set", "testSet");
  }

  @Test
  public void testList() {
    testSchemaMethod("list", "testList");
  }

  @Test
  public void nestedListString() {
    testSchemaMethod("nestedListString", "nestedListString");
  }

  @Test
  public void testMap() {
    testSchemaMethod("map", "testMap");
  }

  @Test
  public void testMapList() {
    testSchemaMethod("mapList", "testMapList");
  }

  @Test
  public void testAllType() {
    testSchemaMethod("allType", "testAllType");
  }

  @Test
  public void testMultiParam() {
    testSchemaMethod("multiParam", "testMultiParam");
  }

  @Test
  public void testAllMethod() {
    testSchemaMethod("allMethod");
  }

  @Test
  public void testResponseHeader() {
    testSchemaMethod("responseHeader", "testResponseHeader");
  }

  @Test
  public void testApiResponse() {
    testSchemaMethod("apiResponse", "testApiResponse");
  }

  @Test
  public void testApiOperation() {
    testSchemaMethod("apiOperation", "testApiOperation");
  }

  @Test
  public void testCompletableFuture() {
    testSchemaMethod("completableFuture", "testCompletableFuture");
  }

  @Test
  public void testOptional() {
    testSchemaMethod("testOptional", "testOptional");
  }

  @Test
  public void testCompletableFutureOptional() {
    testSchemaMethod("testCompletableFutureOptional", "testCompletableFutureOptional");
  }

  @Test
  public void testDate() {
    testSchemaMethod("date", "testDate");
  }

  @Test
  public void testPart() {
    testSchemaMethod("part", "part");
  }

  @Test
  public void testPartArray() {
    testSchemaMethod("partArray", "partArray");
  }

  @Test
  public void testPartList() {
    testSchemaMethod("partList", "partList");
  }

  @Test
  public void should_ignore_httpServletRequest() {
    testSchemaMethod("ignoreRequest", "ignoreRequest");
  }

  @Test
  public void testRepeatOperation() {
    UnitTestSwaggerUtils.testException(
        "OperationId must be unique. method=org.apache.servicecomb.swagger.generator.core.schema.RepeatOperation:add.",
        RepeatOperation.class);
  }

  @Test
  public void testInvalidResponseHeader() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.core.schema.InvalidResponseHeader:test.",
        "invalid responseHeader, ResponseHeaderConfig [name=h, ResponseConfigBase [description=, responseReference=null, responseClass=class java.lang.Void, responseContainer=]]",
        InvalidResponseHeader.class,
        "test");
  }

  @Test
  public void noParameterName() {
    Method method = ReflectUtils.findMethod(Schema.class, "testint");
    Parameter parameter = method.getParameters()[0];
    new Expectations(parameter) {
      {
        parameter.isNamePresent();
        result = false;
      }
    };

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(
        "parameter name is not present, method=org.apache.servicecomb.swagger.generator.core.schema.Schema:testint\n"
            + "solution:\n"
            + "  change pom.xml, add compiler argument: -parameters, for example:\n"
            + "    <plugin>\n"
            + "      <groupId>org.apache.maven.plugins</groupId>\n"
            + "      <artifactId>maven-compiler-plugin</artifactId>\n"
            + "      <configuration>\n"
            + "        <compilerArgument>-parameters</compilerArgument>\n"
            + "      </configuration>\n"
            + "    </plugin>");
    collectParameterName(parameter);
  }
}
