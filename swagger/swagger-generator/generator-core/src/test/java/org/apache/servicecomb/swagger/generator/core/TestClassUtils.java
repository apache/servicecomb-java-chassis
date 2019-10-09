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

import static org.hamcrest.core.Is.is;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.ws.rs.Path;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.servicecomb.foundation.test.scaffolding.model.User;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.models.parameters.PathParameter;

@SwaggerDefinition
public class TestClassUtils {
  ClassLoader classLoader = new ClassLoader() {
  };

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void getClassByName_exist() {
    String clsName = String.class.getName();

    Assert.assertSame(String.class, ClassUtils.getClassByName(null, clsName));
    Assert.assertSame(String.class, ClassUtils.getClassByName(classLoader, clsName));
  }

  @Test
  public void getClassByName_notExist() {
    String clsName = "notExist";

    Assert.assertNull(ClassUtils.getClassByName(null, clsName));
    Assert.assertNull(ClassUtils.getClassByName(classLoader, clsName));
  }

  public static class Impl {
    public List<User> getUser(List<String> p1, List<User> p2) {
      return null;
    }
  }

  @Test
  public void getOrCreateBodyClass() throws NoSuchFieldException {
    SwaggerGenerator generator = UnitTestSwaggerUtils.generateSwagger(Impl.class);
    OperationGenerator operationGenerator = generator.getOperationGeneratorMap().get("getUser");

    Class<?> cls = ClassUtils.getOrCreateBodyClass(operationGenerator, null);
    Assert.assertEquals("gen.swagger.getUserBody", cls.getName());
    Assert.assertEquals("java.util.List<java.lang.String>", cls.getField("p1").getGenericType().getTypeName());
    Assert.assertEquals("java.util.List<org.apache.servicecomb.foundation.test.scaffolding.model.User>",
        cls.getField("p2").getGenericType().getTypeName());
  }

  @Test
  public void testHasAnnotation() {
    Assert.assertTrue(ClassUtils.hasAnnotation(TestClassUtils.class, SwaggerDefinition.class));
    Assert.assertTrue(ClassUtils.hasAnnotation(TestClassUtils.class, Test.class));

    Assert.assertFalse(ClassUtils.hasAnnotation(TestClassUtils.class, Path.class));
  }

  @Test
  public void isRawJsonType() {
    PathParameter param = new PathParameter();

    Assert.assertFalse(ClassUtils.isRawJsonType(param));

    param.setVendorExtension(SwaggerConst.EXT_RAW_JSON_TYPE, Boolean.FALSE);
    Assert.assertFalse(ClassUtils.isRawJsonType(param));

    param.setVendorExtension(SwaggerConst.EXT_RAW_JSON_TYPE, Boolean.TRUE);
    Assert.assertTrue(ClassUtils.isRawJsonType(param));
  }

  @Test
  public void correctMethodParameterName_normal() {
    String name = "name";
    Assert.assertSame(name, ClassUtils.correctMethodParameterName(name));
  }

  @Test
  public void correctMethodParameterName_update() {
    String name = "name.-";
    Assert.assertEquals("name__", ClassUtils.correctMethodParameterName(name));
  }

  @Test
  public void testCorrectClassNameInTheCrossed() {
    String result = ClassUtils.correctClassName("a-b");
    Assert.assertThat(result, is("a_b"));

    result = ClassUtils.correctClassName("a.a-b");
    Assert.assertThat(result, is("a.a_b"));
  }

  @Test
  public void testCorrectClassNameStartWithNumber() {
    String result = ClassUtils.correctClassName("100");
    Assert.assertThat(result, is("_100"));

    result = ClassUtils.correctClassName("a.100");
    Assert.assertThat(result, is("a._100"));
  }

  @Test
  public void testCorrectClassNameReservedWords() throws IllegalAccessException {
    @SuppressWarnings("unchecked")
    Set<String> JAVA_RESERVED_WORDS = (Set<String>) FieldUtils.readStaticField(SourceVersion.class, "keywords", true);
    String name = String.join(".", JAVA_RESERVED_WORDS);
    String expectResult = "_" + String.join("._", JAVA_RESERVED_WORDS);

    String result = ClassUtils.correctClassName(name);
    Assert.assertThat(result, is(expectResult));
  }

  @Test
  public void testCorrectClassNameEmptyPart() {
    String result = ClassUtils.correctClassName("..a..a..");
    Assert.assertThat(result, is("_._.a._.a._._"));
  }

  @Test
  public void testCorrectClassNameCanonical() {
    String result = ClassUtils.correctClassName("java.util.List<java.lang.String>[");
    Assert.assertThat(result, is("java.util.List_java.lang.String_array_"));
  }

  @Test
  public void testCorrectClassNameStringArray() {
    String result = ClassUtils.correctClassName("[Ljava/lang/String;");
    Assert.assertThat(result, is("array_Ljava/lang/String_"));
  }

  @Test
  public void testCorrectClassNameNormal() {
    String result = ClassUtils.correctClassName("String");
    Assert.assertThat(result, is("String"));
  }

  @Test
  public void getClassName_noName() {
    Assert.assertNull(ClassUtils.getClassName(null));

    Map<String, Object> vendorExtensions = new HashMap<>();
    Assert.assertNull(ClassUtils.getClassName(vendorExtensions));
  }

  @Test
  public void getClassName_normal() {
    Map<String, Object> vendorExtensions = new HashMap<>();
    vendorExtensions.put(SwaggerConst.EXT_JAVA_CLASS, String.class.getName());

    Assert.assertSame(String.class.getName(), ClassUtils.getClassName(vendorExtensions));
  }

  @Test
  public void getInterfaceName_noName() {
    Map<String, Object> vendorExtensions = new HashMap<>();

    Assert.assertNull(ClassUtils.getInterfaceName(vendorExtensions));
  }

  @Test
  public void getInterfaceName_normal() {
    Map<String, Object> vendorExtensions = new HashMap<>();
    vendorExtensions.put(SwaggerConst.EXT_JAVA_INTF, String.class.getName());

    Assert.assertSame(String.class.getName(), ClassUtils.getInterfaceName(vendorExtensions));
  }

  @Test
  public void getRawClassName_empty() {
    Assert.assertNull(ClassUtils.getRawClassName(null));
    Assert.assertNull(ClassUtils.getRawClassName(""));
  }

  @Test
  public void getRawClassName_normal() {
    Assert.assertEquals(String.class.getName(), ClassUtils.getRawClassName(String.class.getName()));
  }

  @Test
  public void getRawClassName_generic() {
    Assert.assertEquals("abc", ClassUtils.getRawClassName("abc<d>"));
  }

  @Test
  public void getRawClassName_invalid() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("Invalid class canonical: <abc>"));

    ClassUtils.getRawClassName("<abc>");
  }
}
