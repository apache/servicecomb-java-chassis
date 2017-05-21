/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.swagger.generator.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

import io.servicecomb.swagger.generator.core.schema.InvalidResponseHeader;
import io.servicecomb.swagger.generator.core.schema.Schema;
import io.servicecomb.swagger.generator.core.unittest.SwaggerGeneratorForTest;
import io.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import io.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import io.servicecomb.swagger.converter.ConverterMgr;
import io.servicecomb.swagger.generator.core.schema.RepeatOperation;
import io.servicecomb.swagger.generator.core.schema.User;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;
import io.servicecomb.foundation.common.utils.ReflectUtils;

import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author
 * @version  [版本号, 2017年3月17日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TestSwaggerUtils {
    SwaggerGeneratorContext context = new DefaultSwaggerGeneratorContext();

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
    public void testDate() {
        SwaggerGenerator generator = testSchemaMethod("date", "testDate");
        Class<?> intf = ClassUtils.getOrCreateInterface(generator);
        Method method = ReflectUtils.findMethod(intf, "testDate");
        Assert.assertEquals(Date.class, method.getReturnType());
    }

    @Test
    public void testRepeatOperation() {
        UnitTestSwaggerUtils.testException(
                "OperationId must be unique. com.huawei.paas.cse.swagger.generator.core.schema.RepeatOperation:add",
                context,
                RepeatOperation.class);
    }

    @Test
    public void testInvalidResponseHeader() {
        UnitTestSwaggerUtils.testException(
                "invalid responseHeader, ResponseHeaderConfig [name=h, ResponseConfigBase [description=, responseReference=null, responseClass=class java.lang.Void, responseContainer=]]",
                context,
                InvalidResponseHeader.class,
                "test");
    }
}
