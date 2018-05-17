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
package org.apache.servicecomb.swagger.converter.swaggerToClassGenerator;

import java.lang.reflect.Method;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public class TestSwaggerToClassGenerator_create {
  static TestSwaggerToClassGenerator_base base = new TestSwaggerToClassGenerator_base(true);

  @AfterClass
  public static void tearDown() {
    base.tearDown();
  }

  @Test
  public void recursiveSelf() throws NoSuchFieldException {
    Method method = ReflectUtils.findMethod(base.swaggerIntf, "recursiveSelf");
    Class<?> returnType = method.getReturnType();

    Assert.assertEquals("gen.RecursiveSelfType", returnType.getName());
    Assert.assertEquals("gen.RecursiveSelfType", returnType.getField("field").getType().getName());
  }

  @Test
  public void dependType() throws NoSuchFieldException {
    Method method = ReflectUtils.findMethod(base.swaggerIntf, "dependType");
    Class<?> returnType = method.getReturnType();

    Assert.assertEquals("gen.DependTypeA", returnType.getName());
    Assert.assertEquals("gen.DependTypeB", returnType.getField("b").getType().getName());
  }

  @Test
  public void generic() throws NoSuchFieldException {
    Method method = ReflectUtils.findMethod(base.swaggerIntf, "generic");
    Class<?> returnType = method.getReturnType();

    Assert.assertEquals("gen.GenericRecursiveSelfTypeDependTypeA", returnType.getName());
    Assert.assertEquals("gen.RecursiveSelfType", returnType.getField("v1").getType().getName());
    Assert.assertEquals("gen.DependTypeA", returnType.getField("v2").getType().getName());
  }
}
