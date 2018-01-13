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

package org.apache.servicecomb.common.javassist;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import javassist.ClassPool;
import mockit.Deencapsulation;

public class TestJavassistUtils {
  @Test
  public void testField() throws Exception {
    ClassConfig classConfig = new ClassConfig();
    classConfig.setClassName("cse.ut.testField");

    FieldConfig fieldConfig = classConfig.addField("intField", int.class);
    fieldConfig.setGenGetter(true);
    fieldConfig.setGenSetter(true);

    fieldConfig = classConfig.addField("intArrayField", int[].class);
    fieldConfig.setGenGetter(true);
    fieldConfig.setGenSetter(true);

    fieldConfig = classConfig.addField("listStringField",
        TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
    fieldConfig.setGenGetter(true);
    fieldConfig.setGenSetter(true);

    Class<?> cls = JavassistUtils.createClass(classConfig);

    Field field = cls.getField("intField");
    Assert.assertEquals(Integer.class, field.getType());

    Method method = cls.getMethod("getIntField");
    Assert.assertEquals(Integer.class, method.getReturnType());

    field = cls.getField("intArrayField");
    Assert.assertEquals(int[].class, field.getType());

    method = cls.getMethod("getIntArrayField");
    Assert.assertEquals(int[].class, method.getReturnType());

    field = cls.getField("listStringField");
    Assert.assertEquals("java.util.List<java.lang.String>", field.getGenericType().getTypeName());

    method = cls.getMethod("getListStringField");
    Assert.assertEquals("java.util.List<java.lang.String>", method.getGenericReturnType().getTypeName());
  }

  @Test
  public void testAddParameter() {
    ClassConfig classConfig = new ClassConfig();
    classConfig.setIntf(true);
    String intfName = "cse.ut.TestAddParameter";
    classConfig.setClassName(intfName);

    MethodConfig methodConfig = new MethodConfig();
    methodConfig.setName("method");
    methodConfig.setResult(TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
    methodConfig.addParameter("map",
        TypeFactory.defaultInstance().constructMapType(Map.class, String.class, String.class));
    methodConfig.addParameter("set",
        TypeFactory.defaultInstance().constructCollectionType(Set.class, String.class));
    classConfig.addMethod(methodConfig);

    Class<?> intf = JavassistUtils.createClass(classConfig);

    Assert.assertEquals(intfName, intf.getName());
    Method method = ReflectUtils.findMethod(intf, "method");
    Assert.assertEquals("method", method.getName());
    Assert.assertEquals("java.util.List<java.lang.String>", method.getGenericReturnType().getTypeName());

    Type[] types = method.getGenericParameterTypes();
    Assert.assertEquals("java.util.Map<java.lang.String, java.lang.String>", types[0].getTypeName());
    Assert.assertEquals("java.util.Set<java.lang.String>", types[1].getTypeName());
  }

  @Test
  public void testInterface() throws Exception {
    ClassConfig classConfig = new ClassConfig();
    classConfig.setIntf(true);
    String intfName = "cse.ut.TestInterface";
    classConfig.setClassName(intfName);

    String source = "java.util.List method(java.util.Map map, java.util.Set set);";
    String genericSignature =
        "(Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;Ljava/util/Set<Ljava/lang/String;>;)Ljava/util/List<Ljava/lang/String;>;";
    classConfig.addMethod(source, genericSignature);

    Class<?> intf = JavassistUtils.createClass(classConfig);

    Assert.assertEquals(intfName, intf.getName());
    Method method = ReflectUtils.findMethod(intf, "method");
    Assert.assertEquals("method", method.getName());
    Assert.assertEquals("java.util.List<java.lang.String>", method.getGenericReturnType().getTypeName());

    Type[] types = method.getGenericParameterTypes();
    Assert.assertEquals("java.util.Map<java.lang.String, java.lang.String>", types[0].getTypeName());
    Assert.assertEquals("java.util.Set<java.lang.String>", types[1].getTypeName());
  }

  @Test
  public void singleWrapperInt() throws Exception {
    ClassConfig classConfig = new ClassConfig();
    classConfig.setClassName("cse.ut.single.IntWrapper");
    classConfig.addField("intField", TypeFactory.defaultInstance().constructType(int.class));

    JavassistUtils.genSingleWrapperInterface(classConfig);

    Class<?> wrapperClass = JavassistUtils.createClass(classConfig);

    SingleWrapper instance = (SingleWrapper) wrapperClass.newInstance();
    instance.writeField(100);
    int intFieldValue = (int) instance.readField();
    Assert.assertEquals(100, intFieldValue);
  }

  @Test
  public void multiWrapper() throws Exception {
    ClassConfig classConfig = new ClassConfig();
    classConfig.setClassName("cse.ut.multi.Wrapper");
    classConfig.addField("intField", (Type) int.class);
    classConfig.addField("strField", String.class);

    JavassistUtils.genMultiWrapperInterface(classConfig);

    Class<?> wrapperClass = JavassistUtils.createClass(classConfig);

    MultiWrapper instance = (MultiWrapper) wrapperClass.newInstance();
    instance.writeFields(new Object[] {100, "test"});
    Object[] fieldValues = (Object[]) instance.readFields();
    Assert.assertEquals(100, fieldValues[0]);
    Assert.assertEquals("test", fieldValues[1]);
  }

  @Test
  public void testEnum() throws Exception {
    @SuppressWarnings("rawtypes")
    Class<? extends Enum> cls = JavassistUtils.createEnum("cse.ut.EnumAbc", "a", "b");
    Method method = cls.getMethod("values");
    Enum<?>[] values = (Enum<?>[]) method.invoke(null);

    Assert.assertEquals("cse.ut.EnumAbc", cls.getName());
    Assert.assertEquals(2, values.length);
    Assert.assertEquals("a", values[0].name());
    Assert.assertEquals(0, values[0].ordinal());
    Assert.assertEquals("b", values[1].name());
    Assert.assertEquals(1, values[1].ordinal());
  }

  @Test
  public void testGetNameForGenerateCode() {
    JavaType jt = TypeFactory.defaultInstance().constructType(byte[].class);
    String name = JavassistUtils.getNameForGenerateCode(jt);
    Assert.assertEquals("byte[]", name);

    jt = TypeFactory.defaultInstance().constructType(Byte[].class);
    name = JavassistUtils.getNameForGenerateCode(jt);
    Assert.assertEquals("java.lang.Byte[]", name);

    jt = TypeFactory.defaultInstance().constructType(Object[].class);
    name = JavassistUtils.getNameForGenerateCode(jt);
    Assert.assertEquals("java.lang.Object[]", name);
  }

  @Test
  public void managerClassPool() {
    ClassLoader classLoader1 = new ClassLoader() { };
    ClassLoader classLoader2 = new ClassLoader() { };

    ClassPool p1 = Deencapsulation.invoke(JavassistUtils.class, "getOrCreateClassPool", classLoader1);
    ClassPool p2 = Deencapsulation.invoke(JavassistUtils.class, "getOrCreateClassPool", classLoader2);
    Assert.assertNotSame(p1, p2);

    Map<ClassLoader, ClassPool> CLASSPOOLS = Deencapsulation.getField(JavassistUtils.class, "CLASSPOOLS");
    JavassistUtils.clearByClassLoader(classLoader1);
    Assert.assertNull(CLASSPOOLS.get(classLoader1));
  }
}
