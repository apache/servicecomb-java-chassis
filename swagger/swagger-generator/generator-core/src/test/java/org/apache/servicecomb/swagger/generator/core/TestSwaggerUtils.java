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

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils;
import org.apache.servicecomb.swagger.generator.core.pojo.TestType1;
import org.apache.servicecomb.swagger.generator.core.pojo.TestType2;
import org.apache.servicecomb.swagger.generator.core.schema.InvalidResponseHeader;
import org.apache.servicecomb.swagger.generator.core.schema.RepeatOperation;
import org.apache.servicecomb.swagger.generator.core.schema.Schema;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
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
    SwaggerGeneratorUtils.collectParameterName(parameter);
  }

  @Test
  public void testGetRawJsonType() {
    io.swagger.models.parameters.Parameter param = Mockito.mock(io.swagger.models.parameters.Parameter.class);
    Map<String, Object> extensions = new HashMap<>();
    when(param.getVendorExtensions()).thenReturn(extensions);

    extensions.put(SwaggerConst.EXT_RAW_JSON_TYPE, true);
    Assert.assertTrue(SwaggerUtils.isRawJsonType(param));

    extensions.put(SwaggerConst.EXT_RAW_JSON_TYPE, "test");
    Assert.assertFalse(SwaggerUtils.isRawJsonType(param));
  }

  @Test
  public void isComplexProperty() {
    Property property = new RefProperty("ref");
    Assert.assertTrue(SwaggerUtils.isComplexProperty(property));
    property = new ObjectProperty();
    Assert.assertTrue(SwaggerUtils.isComplexProperty(property));
    property = new MapProperty();
    Assert.assertTrue(SwaggerUtils.isComplexProperty(property));
    property = new ArrayProperty(new ObjectProperty());
    Assert.assertTrue(SwaggerUtils.isComplexProperty(property));

    property = new ArrayProperty(new StringProperty());
    Assert.assertFalse(SwaggerUtils.isComplexProperty(property));
    property = new StringProperty();
    Assert.assertFalse(SwaggerUtils.isComplexProperty(property));
  }

  private static class AllTypeTest1 {
    TestType1 t1;

    List<TestType1> t2;

    Map<String, TestType1> t3;

    TestType1[] t4;
  }

  private static class AllTypeTest2 {
    TestType2 t1;

    List<TestType2> t2;

    Map<String, TestType2> t3;

    TestType2[] t4;
  }

  @Test
  public void testAddDefinitions() {
    Field[] fields1 = AllTypeTest1.class.getDeclaredFields();
    Field[] fields2 = AllTypeTest2.class.getDeclaredFields();
    for (int i = 0; i < fields1.length; i++) {
      for (int j = 0; j < fields2.length; j++) {
        if (fields1[i].isSynthetic() || fields2[j].isSynthetic()) {
          continue;
        }
        try {
          testExcep(fields1[i].getGenericType(), fields2[j].getGenericType());
          fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
          assertThat(e.getMessage(), containsString("duplicate param model:"));
        }
      }
    }
  }

  private void testExcep(Type f1, Type f2) {
    Swagger swagger = new Swagger();
    SwaggerUtils.addDefinitions(swagger, f1);
    SwaggerUtils.addDefinitions(swagger, f2);
  }
}
