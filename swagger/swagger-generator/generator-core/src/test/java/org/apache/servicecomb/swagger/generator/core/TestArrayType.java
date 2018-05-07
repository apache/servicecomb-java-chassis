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

import java.lang.reflect.Method;

import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.generator.core.schema.ArrayType;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import javassist.CtClass;
import javassist.CtMethod;

public class TestArrayType {
  ClassLoader classLoader = new ClassLoader() {
  };

  @After
  public void tearDown() {
    JavassistUtils.clearByClassLoader(classLoader);
  }

  @Test
  public void test() throws Exception {
    SwaggerGenerator generator = UnitTestSwaggerUtils.generateSwagger(classLoader, ArrayType.class);
    Class<?> cls = ClassUtilsForTest.getOrCreateInterface(generator);
    Method method = ReflectUtils.findMethod(cls, "testBytes");

    Class<?> param = (Class<?>) method.getParameters()[0].getParameterizedType();
    Assert.assertEquals(byte[].class, param.getField("value").getType());
    Assert.assertEquals(byte[].class, method.getReturnType());
  }

  public CtMethod findMethod(CtClass cc, String methodName) {
    for (CtMethod method : cc.getMethods()) {
      if (methodName.equals(method.getName())) {
        return method;
      }
    }

    return null;
  }
}
