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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class TestCtTypeJavaType {
  Class<?> cls = TestCtTypeJavaType.class;

  JavaType javaType = TypeFactory.defaultInstance().constructType(cls);

  CtType ctType = new CtType(javaType);

  CtTypeJavaType ctTypeJavaType = new CtTypeJavaType(ctType);

  @Test
  public void construct() {
    Assert.assertSame(ctType, ctTypeJavaType.getType());
    Assert.assertEquals(cls.getTypeName(), ctTypeJavaType.buildCanonicalName());
  }

  @Test
  public void getGenericSignature() {
    JavaType listJavaType = TypeFactory.defaultInstance().constructCollectionType(List.class, ctTypeJavaType);
    Assert.assertEquals("Ljava/util/List<Lorg/apache/servicecomb/common/javassist/TestCtTypeJavaType;>;",
        listJavaType.getGenericSignature());
  }

  /**
   * The {@link CtTypeJavaType} with different CtType should holds different hash code.
   */
  @Test
  public void testHashCode() {
    JavaType newJavaType = TypeFactory.defaultInstance().constructType(String.class);
    CtType newCtType = new CtType(newJavaType);
    CtTypeJavaType newCtTypeJavaType = new CtTypeJavaType(newCtType);
    Assert.assertNotEquals(ctTypeJavaType.hashCode(), newCtTypeJavaType.hashCode());

    newJavaType = TypeFactory.defaultInstance().constructType(cls);
    newCtType = new CtType(newJavaType);
    newCtTypeJavaType = new CtTypeJavaType(newCtType);
    Assert.assertEquals(ctTypeJavaType.hashCode(), newCtTypeJavaType.hashCode());
  }

  /**
   * The {@link CtTypeJavaType}s holding different type information should not equal to each others.
   * While those holding the same type information should be equal.
   */
  @Test
  public void testEquals() {
    JavaType newJavaType = TypeFactory.defaultInstance().constructType(String.class);
    CtType newCtType = new CtType(newJavaType);
    CtTypeJavaType newCtTypeJavaType = new CtTypeJavaType(newCtType);
    Assert.assertNotEquals(ctTypeJavaType, newCtTypeJavaType);

    newJavaType = TypeFactory.defaultInstance().constructType(cls);
    newCtType = new CtType(newJavaType);
    newCtTypeJavaType = new CtTypeJavaType(newCtType);
    Assert.assertEquals(ctTypeJavaType, newCtTypeJavaType);

    // test subClass of CtTypeJavaType
    newJavaType = TypeFactory.defaultInstance().constructType(cls);
    newCtType = new CtType(newJavaType);
    newCtTypeJavaType = new CtTypeJavaType(newCtType) {
      private static final long serialVersionUID = 1876189050753964880L;
    };
    Assert.assertEquals(ctTypeJavaType, newCtTypeJavaType);
  }
}
