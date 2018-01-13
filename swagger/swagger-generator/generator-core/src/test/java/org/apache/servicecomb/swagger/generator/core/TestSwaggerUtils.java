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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.converter.ConverterMgr;
import org.apache.servicecomb.swagger.generator.core.schema.Color;
import org.apache.servicecomb.swagger.generator.core.schema.InvalidResponseHeader;
import org.apache.servicecomb.swagger.generator.core.schema.RepeatOperation;
import org.apache.servicecomb.swagger.generator.core.schema.Schema;
import org.apache.servicecomb.swagger.generator.core.schema.User;
import org.apache.servicecomb.swagger.generator.core.unittest.SwaggerGeneratorForTest;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;

public class TestSwaggerUtils {
  SwaggerGeneratorContext context = new PojoSwaggerGeneratorContext();

  @Test
  public void testConverter() {
    SwaggerGenerator generator = new SwaggerGeneratorForTest(context, null);
    Swagger swagger = generator.getSwagger();
    ParamUtils.addDefinitions(swagger, User.class);
    Model model = swagger.getDefinitions().get("User");
    model.getVendorExtensions().clear();

    JavaType javaType = ConverterMgr.findJavaType(generator, model);
    checkJavaType(swagger, javaType);

    RefModel refModel = new RefModel();
    refModel.setReference("User");
    javaType = ConverterMgr.findJavaType(generator, refModel);
    checkJavaType(swagger, javaType);
  }

  protected void checkJavaType(Swagger swagger, JavaType javaType) {
    Class<?> cls = javaType.getRawClass();
    Field[] fields = cls.getFields();
    Assert.assertEquals("gen.cse.ms.ut.User", cls.getName());
    Assert.assertEquals("name", fields[0].getName());
    Assert.assertEquals(String.class, fields[0].getType());
    Assert.assertEquals("age", fields[1].getName());
    Assert.assertEquals(Integer.class, fields[1].getType());
  }

  private SwaggerGenerator testSchemaMethod(String resultName, String... methodNames) {
    return UnitTestSwaggerUtils.testSwagger("schemas/" + resultName + ".yaml",
        context,
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
  public void testEnum() {
    SwaggerGenerator generator = testSchemaMethod("enum", "testEnum");
    JavassistUtils.detach("gen.cse.ms.ut.SchemaIntf");
    Class<?> intf = ClassUtils.getOrCreateInterface(generator);

    Method method = ReflectUtils.findMethod(intf, "testEnum");
    Class<?> bodyCls = method.getParameterTypes()[0];
    Field[] fields = bodyCls.getFields();
    Assert.assertEquals(Color.class, fields[0].getType());
    Assert.assertEquals(fields[0].getType(), fields[1].getType());
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
  public void testDate() {
    SwaggerGenerator generator = testSchemaMethod("date", "testDate");
    JavassistUtils.detach("gen.cse.ms.ut.SchemaIntf");
    Class<?> intf = ClassUtils.getOrCreateInterface(generator);

    Method method = ReflectUtils.findMethod(intf, "testDate");
    Assert.assertEquals(Date.class, method.getReturnType());
  }

  @Test
  public void testRepeatOperation() {
    UnitTestSwaggerUtils.testException(
        "OperationId must be unique. org.apache.servicecomb.swagger.generator.core.schema.RepeatOperation:add",
        context,
        RepeatOperation.class);
  }

  @Test
  public void testInvalidResponseHeader() {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.core.schema.InvalidResponseHeader:test",
        "invalid responseHeader, ResponseHeaderConfig [name=h, ResponseConfigBase [description=, responseReference=null, responseClass=class java.lang.Void, responseContainer=]]",
        context,
        InvalidResponseHeader.class,
        "test");
  }
}
