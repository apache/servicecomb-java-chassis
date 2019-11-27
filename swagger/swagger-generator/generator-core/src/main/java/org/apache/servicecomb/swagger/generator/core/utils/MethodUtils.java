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

import org.apache.commons.lang3.StringUtils;

import io.swagger.annotations.ApiOperation;

public class MethodUtils {
  /**
   * Get the methods of <code>cls</code> which are valid for generating Swagger schema.
   * @param cls The REST interface class, or so called "controller" class, to be analysed.
   * @return the valid methods to be used to generate Swagger schema, sorted by their Swagger operation name.
   */
  public static List<Method> findProducerMethods(Class<?> cls) {
    Method[] methods = cls.getMethods();
    List<Method> producerMethods = new ArrayList<>(methods.length);

    for (Method m : methods) {
      if (!isSkipMethod(cls, m)) {
        producerMethods.add(m);
      }
    }

    // order of cls.getMethods() is undefined and not stable
    // so we must sort them first to make generation is stable
    producerMethods.sort(Comparator.comparing(MethodUtils::findSwaggerMethodName));
    return producerMethods;
  }

  /**
   * Pick out those methods not proper to be added into the Swagger schema.
   *
   * @param cls the owner class of the <code>method</code>
   * @param method the method to be validate
   * @return true if this method should be abandoned;
   * false if this method should be added in to Swagger schema
   */
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

  /**
   * Get the operationId in schema of this method,
   * no matter whether it should be hidden(see {@link ApiOperation#hidden()}).
   * @return If the operation name is specified via {@link ApiOperation}, use that one.
   * Otherwise the method name is returned.
   */
  public static String findSwaggerMethodName(Method method) {
    ApiOperation apiOperationAnnotation = method.getAnnotation(ApiOperation.class);
    if (apiOperationAnnotation == null || StringUtils.isEmpty(apiOperationAnnotation.nickname())) {
      return method.getName();
    }

    return apiOperationAnnotation.nickname();
  }

  /**
   * @return true if this method should NOT be displayed in schema; otherwise false
   */
  public static boolean isHiddenInSwagger(Method method) {
    ApiOperation apiOperationAnnotation = method.getAnnotation(ApiOperation.class);
    return null != apiOperationAnnotation && apiOperationAnnotation.hidden();
  }
}
