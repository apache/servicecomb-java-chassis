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
import java.lang.reflect.ParameterizedType;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.converter.swaggerToClassGenerator.model.DependTypeA;
import org.apache.servicecomb.swagger.converter.swaggerToClassGenerator.model.Generic;
import org.apache.servicecomb.swagger.converter.swaggerToClassGenerator.model.RecursiveSelfType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public class TestSwaggerToClassGenerator_reuse {
  static TestSwaggerToClassGenerator_base base = new TestSwaggerToClassGenerator_base(false);

  @AfterClass
  public static void tearDown() {
    base.tearDown();
  }

  @Test
  public void recursiveSelf() {
    Method method = ReflectUtils.findMethod(base.swaggerIntf, "recursiveSelf");
    Class<?> returnType = method.getReturnType();

    Assert.assertSame(RecursiveSelfType.class, returnType);
  }

  @Test
  public void dependType() {
    Method method = ReflectUtils.findMethod(base.swaggerIntf, "dependType");
    Class<?> returnType = method.getReturnType();

    Assert.assertSame(DependTypeA.class, returnType);
  }

  @Test
  public void generic() {
    Method method = ReflectUtils.findMethod(base.swaggerIntf, "generic");
    ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

    Assert.assertSame(Generic.class, returnType.getRawType());
    Assert.assertSame(RecursiveSelfType.class, returnType.getActualTypeArguments()[0]);
    Assert.assertSame(DependTypeA.class, returnType.getActualTypeArguments()[1]);
  }
}
