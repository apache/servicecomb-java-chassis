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
import org.apache.servicecomb.swagger.generator.core.schema.RepeatOperation;
import org.apache.servicecomb.swagger.generator.core.schema.Schema;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;

@SuppressWarnings("rawtypes")
public class TestSwaggerUtils {

  @Test
  public void testSchemaMethod() {
    UnitTestSwaggerUtils.testSwagger("schemas/Schema.yaml",
        Schema.class);
  }

  @Test
  public void testRepeatOperation() {
    UnitTestSwaggerUtils.testException(
        "OperationId must be unique. method=org.apache.servicecomb.swagger.generator.core.schema.RepeatOperation:add.",
        RepeatOperation.class);
  }

  @Test
  public void noParameterName() {
    Method method = ReflectUtils.findMethod(Schema.class, "testint");
    Parameter parameter = Mockito.spy(method.getParameters()[0]);
    Mockito.when(parameter.isNamePresent()).thenReturn(false);

    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
        () -> SwaggerGeneratorUtils.collectParameterName(parameter));
    String expectedMsg =
        "parameter name is not present, method=org.apache.servicecomb.swagger.generator.core.schema.Schema:testint\n"
            + "solution:\n"
            + "  change pom.xml, add compiler argument: -parameters, for example:\n"
            + "    <plugin>\n"
            + "      <groupId>org.apache.maven.plugins</groupId>\n"
            + "      <artifactId>maven-compiler-plugin</artifactId>\n"
            + "      <configuration>\n"
            + "        <compilerArgument>-parameters</compilerArgument>\n"
            + "      </configuration>\n"
            + "    </plugin>";
    Assertions.assertEquals(expectedMsg, exception.getMessage());
  }

  @Test
  public void testGetRawJsonType() {
    RequestBody param = Mockito.mock(RequestBody.class);
    Map<String, Object> extensions = new HashMap<>();
    when(param.getExtensions()).thenReturn(extensions);

    extensions.put(SwaggerConst.EXT_RAW_JSON_TYPE, true);
    Assertions.assertTrue(SwaggerUtils.isRawJsonType(param));

    extensions.put(SwaggerConst.EXT_RAW_JSON_TYPE, "test");
    Assertions.assertFalse(SwaggerUtils.isRawJsonType(param));
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
    for (Field value : fields1) {
      for (Field field : fields2) {
        if (value.isSynthetic() || field.isSynthetic()) {
          continue;
        }
        try {
          testExcep(value.getGenericType(), field.getGenericType());
          fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
          MatcherAssert.assertThat(e.getMessage(), containsString("duplicate param model:"));
        }
      }
    }
  }

  private void testExcep(Type f1, Type f2) {
    OpenAPI swagger = new OpenAPI();
    SwaggerUtils.resolveTypeSchemas(swagger, f1);
    SwaggerUtils.resolveTypeSchemas(swagger, f2);
  }

  @Test
  public void test_resolve_type_schemas_correct() {
    OpenAPI openAPI = new OpenAPI();

    io.swagger.v3.oas.models.media.Schema schema = SwaggerUtils.resolveTypeSchemas(openAPI, String.class);
    Assertions.assertTrue(schema instanceof StringSchema);

    openAPI = new OpenAPI();
    schema = SwaggerUtils.resolveTypeSchemas(openAPI, Integer.class);
    Assertions.assertTrue(schema instanceof IntegerSchema);

    openAPI = new OpenAPI();
    schema = SwaggerUtils.resolveTypeSchemas(openAPI, TestType1.class);
    schema = SwaggerUtils.getSchema(openAPI, schema); // resolve reference
    // should be ObjectSchema but swagger is not.
    // <pre> Assertions.assertTrue(schema instanceof ObjectSchema) </pre>
    Assertions.assertEquals("object", schema.getType());
  }
}
