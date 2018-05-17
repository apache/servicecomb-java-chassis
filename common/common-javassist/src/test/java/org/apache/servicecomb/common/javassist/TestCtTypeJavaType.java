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
}
