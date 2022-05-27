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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.reflect.TypeToken;

public final class ReflectUtils {
  private ReflectUtils() {

  }

  public static void setField(Object instance, String fieldName, Object value) {
    setField(instance.getClass(), instance, fieldName, value);
  }

  public static void setField(Class<?> cls, Object instance, String fieldName, Object value) {
    Field field = ReflectionUtils.findField(cls, fieldName);
    try {
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

  @SuppressWarnings("unchecked")
  public static <T> Class<T> getFieldArgument(Class<?> genericCls, String fieldName) {
    try {
      Type generic = FieldUtils.getField(genericCls, fieldName).getGenericType();
      TypeToken<?> token = TypeToken.of(genericCls).resolveType(generic);
      Type fieldType = token.getType();
      Type argument = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
      if (argument instanceof GenericArrayType) {
        return (Class<T>) TypeToken.of(argument).getRawType();
      }

      return (Class<T>) argument;
    } catch (Throwable e) {
      throw new IllegalStateException("Failed to get generic argument.", e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T constructArrayType(Class<?> cls) {
    return (T) Array.newInstance(cls, 0).getClass();
  }

  public static Class<?> getClassByName(String clsName) {
    try {
      return Class.forName(clsName);
    } catch (ClassNotFoundException e) {
      ClassLoader classLoader = JvmUtils.correctClassLoader(null);
      try {
        return classLoader.loadClass(clsName);
      } catch (ClassNotFoundException e1) {
        return null;
      }
    }
  }
}
