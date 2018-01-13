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
import static org.hamcrest.core.Is.is;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;

import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.swagger.generator.core.schema.User;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.models.Swagger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import mockit.Deencapsulation;

@SwaggerDefinition
public class TestClassUtils {
  // copy from ClassUtils
  // do not use JAVA_RESERVED_WORDS in ClassUtils directly
  // because that's the test target.
  private static final Set<String> JAVA_RESERVED_WORDS = new HashSet<>();

  static {
    JAVA_RESERVED_WORDS.addAll(Arrays.asList("true",
        "false",
        "null",
        "abstract",
        "continue",
        "for",
        "new",
        "switch",
        "assert",
        "default",
        "goto",
        "package",
        "synchronized",
        "boolean",
        "do",
        "if",
        "private",
        "this",
        "break",
        "double",
        "implements",
        "protected",
        "throw",
        "byte",
        "else",
        "import",
        "public",
        "throws",
        "case",
        "enum",
        "instanceof",
        "return",
        "transient",
        "catch",
        "extends",
        "int",
        "short",
        "try",
        "char",
        "final",
        "interface",
        "static",
        "void",
        "class",
        "finally",
        "long",
        "strictfp",
        "volatile",
        "const",
        "float",
        "native",
        "super",
        "while"));
  }

  @Test
  public void testHasAnnotation() {
    Assert.assertEquals(true, ClassUtils.hasAnnotation(TestClassUtils.class, SwaggerDefinition.class));
    Assert.assertEquals(true, ClassUtils.hasAnnotation(TestClassUtils.class, Test.class));

    Assert.assertEquals(false, ClassUtils.hasAnnotation(TestClassUtils.class, Path.class));
  }

  public static class Impl {
    public List<User> getUser(List<String> names) {
      return null;
    }
  }

  @Test
  public void testCreateInterface() {
    SwaggerGenerator generator = UnitTestSwaggerUtils.generateSwagger(Impl.class);
    Class<?> intf = ClassUtils.getOrCreateInterface(generator);

    Assert.assertEquals("gen.swagger.ImplIntf", intf.getName());
    Assert.assertEquals(1, intf.getMethods().length);

    Method method = intf.getMethods()[0];
    Assert.assertEquals("getUser", method.getName());

    Assert.assertEquals("gen.swagger.getUser.names", method.getGenericParameterTypes()[0].getTypeName());
    Assert.assertEquals("java.util.List<org.apache.servicecomb.swagger.generator.core.schema.User>",
        method.getGenericReturnType().getTypeName());
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
  public void testCorrectClassNameReservedWords() {
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
  public void testGetOrCreateClass() {
    String className = this.getClass().getCanonicalName();

    Class<?> result = ClassUtils.getOrCreateClass(null, "", new Swagger(), null, className);

    Assert.assertEquals(this.getClass(), result);
  }

  @Test
  public void testGetOrCreateClassOnPropertyIsNull() {
    ClassLoader classLoader = Mockito.mock(ClassLoader.class);
    String className = this.getClass().getCanonicalName();
    ClassPool classPool = Mockito.mock(ClassPool.class);
    CtClass ctClass = Mockito.mock(CtClass.class);

    Map<ClassLoader, ClassPool> classPoolMap = Deencapsulation.getField(JavassistUtils.class, "CLASSPOOLS");
    classPoolMap.put(classLoader, classPool);

    try {
      Mockito.when(classLoader.loadClass(className)).thenReturn(null);
    } catch (ClassNotFoundException e) {
      fail("unexpected exception: " + e);
    }
    Mockito.when(classPool.getOrNull(className)).thenReturn(ctClass);
    try {
      Mockito.when(ctClass.toClass(classLoader, null)).thenReturn(this.getClass());
    } catch (CannotCompileException e) {
      fail("unexpected exception: " + e);
    }

    Class<?> result = ClassUtils.getOrCreateClass(classLoader, "", new Swagger(), null, className);
    Assert.assertEquals(this.getClass(), result);
  }
}
