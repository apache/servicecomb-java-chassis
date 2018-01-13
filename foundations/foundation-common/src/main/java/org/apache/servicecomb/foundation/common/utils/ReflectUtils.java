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

package org.apache.servicecomb.foundation.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.springframework.util.ReflectionUtils;

public final class ReflectUtils {
  private static final Field MODIFIERS_FIELD =
      ReflectionUtils.findField(Field.class, "modifiers");

  static {
    MODIFIERS_FIELD.setAccessible(true);
  }

  private ReflectUtils() {

  }

  public static void setField(Object instance, String fieldName, Object value) {
    setField(instance.getClass(), instance, fieldName, value);
  }

  public static void setField(Class<?> cls, Object instance, String fieldName, Object value) {
    Field field = ReflectionUtils.findField(cls, fieldName);
    try {
      if ((field.getModifiers() & Modifier.FINAL) != 0) {
        MODIFIERS_FIELD.setInt(field, field.getModifiers() & ~Modifier.FINAL);
      }
      field.setAccessible(true);
      field.set(instance, value);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  // 根据方法名，忽略参数查找method，调用此函数的前提是没有重载
  public static Method findMethod(Class<?> cls, String methodName) {
    for (Method method : cls.getMethods()) {
      if (method.getName().equals(methodName)) {
        return method;
      }
    }

    return null;
  }
}
