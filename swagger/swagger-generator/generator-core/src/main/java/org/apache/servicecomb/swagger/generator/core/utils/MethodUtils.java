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

package org.apache.servicecomb.swagger.generator.core.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.swagger.annotations.ApiOperation;

public class MethodUtils {
  public static List<Method> findProducerMethods(Class<?> cls) {
    Method[] methods = cls.getMethods();
    List<Method> producerMethods = new ArrayList<>(methods.length);

    for (Method m : methods) {
      if (!isSkipMethod(cls, m)) {
        producerMethods.add(m);
      }
    }

    producerMethods.sort(Comparator.comparing(Method::getName));
    return producerMethods;
  }

  public static boolean isSkipMethod(Class<?> cls, Method method) {
    if (method.getDeclaringClass() == Object.class) {
      return true;
    }
    if (method.getDeclaringClass().isInterface()
        && !cls.isInterface()) {
      // inherited template methods
      return true;
    }
    // skip static method
    int modifiers = method.getModifiers();
    if (Modifier.isStatic(modifiers)) {
      return true;
    }
    // skip bridge method
    if (method.isBridge()) {
      return true;
    }

    ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
    if (apiOperation != null && apiOperation.hidden()) {
      return apiOperation.hidden();
    }

    return false;
  }
}
