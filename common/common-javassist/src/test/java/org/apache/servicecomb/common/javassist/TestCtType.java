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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class TestCtType {
  CtType ctType;

  @Test
  public void fromCtClass() {
    CtClass ctClass = Mockito.mock(CtClass.class);
    Mockito.when(ctClass.getName()).thenReturn("a.b.c");
    ctType = new CtType(ctClass);

    Assert.assertSame(ctClass, ctType.getCtClass());
    Assert.assertFalse(ctType.hasGenericTypes());
    Assert.assertEquals("La/b/c;", ctType.getGenericSignature());
  }

  @Test
  public void fromCtClass_fullInfo() {
    CtClass ctClass = Mockito.mock(CtClass.class);
    ctType = new CtType(ctClass, true, "Ljava/util/List<[B;>;");

    Assert.assertSame(ctClass, ctType.getCtClass());
    Assert.assertTrue(ctType.hasGenericTypes());
    Assert.assertEquals("Ljava/util/List<[B>;", ctType.getGenericSignature());
  }

  @Test
  public void fromJavaType() throws NotFoundException {
    Class<?> cls = String.class;
    JavaType javaType = TypeFactory.defaultInstance().constructType(cls);
    ctType = new CtType(javaType);

    Assert.assertSame(JavassistUtils.getOrCreateClassPool(cls.getClassLoader())
        .get(cls.getCanonicalName()), ctType.getCtClass());
    Assert.assertFalse(ctType.hasGenericTypes());
    Assert.assertEquals("Ljava/lang/String;", ctType.getGenericSignature());
  }

  @Test
  public void fromJavaType_primitive() throws NotFoundException {
    Class<?> cls = int.class;
    JavaType javaType = TypeFactory.defaultInstance().constructType(cls);
    ctType = new CtType(javaType);

    Assert.assertSame(JavassistUtils.getOrCreateClassPool(Integer.class.getClassLoader())
        .get(Integer.class.getCanonicalName()), ctType.getCtClass());
    Assert.assertFalse(ctType.hasGenericTypes());
    Assert.assertEquals("I;", ctType.getGenericSignature());
  }

  @Test
  public void fromJavaType_bytes() throws NotFoundException {
    Class<?> cls = byte[].class;
    JavaType javaType = TypeFactory.defaultInstance().constructType(cls);
    ctType = new CtType(javaType);

    Assert.assertSame(JavassistUtils.getOrCreateClassPool(cls.getClassLoader())
        .get(cls.getCanonicalName()), ctType.getCtClass());
    Assert.assertFalse(ctType.hasGenericTypes());
    Assert.assertEquals("[B;", ctType.getGenericSignature());
  }

  @Test
  public void fromJavaType_listBytes() throws NotFoundException {
    JavaType javaType = TypeFactory.defaultInstance().constructCollectionType(List.class, byte[].class);
    ctType = new CtType(javaType);

    Assert.assertSame(JavassistUtils.getOrCreateClassPool(List.class.getClassLoader())
        .get(List.class.getCanonicalName()), ctType.getCtClass());
    Assert.assertTrue(ctType.hasGenericTypes());
    Assert.assertEquals("Ljava/util/List<[B>;", ctType.getGenericSignature());
  }

  @Test
  public void fromCtTypeJavaType() throws NotFoundException {
    CtClass ctClass = ClassPool.getDefault().get(String.class.getCanonicalName());
    CtType otherCtType = new CtType(ctClass, false, "Ljava/lang/String;");
    CtTypeJavaType ctTypeJavaType = new CtTypeJavaType(otherCtType);

    ctType = new CtType(ctTypeJavaType);

    Assert.assertSame(ctClass, ctType.getCtClass());
    Assert.assertFalse(ctType.hasGenericTypes());
    Assert.assertEquals("Ljava/lang/String;", ctType.getGenericSignature());
  }

  @Test
  public void fromJavaType_void() {
    final JavaType voidJavaType = TypeFactory.defaultInstance().constructType(void.class);
    CtType voidCtType = new CtType(voidJavaType);

    Assert.assertTrue(voidCtType.getCtClass().isPrimitive());
    Assert.assertEquals("void", voidCtType.getCtClass().getName());
  }
}
