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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.apache.commons.lang3.reflect.TypeUtils;

public final class ParamUtils {
  private ParamUtils() {

  }

  public static Type getGenericParameterType(Class<?> mainClass, Method method, Parameter param) {
    Type type = param.getParameterizedType();
    return getGenericParameterType(mainClass, method.getDeclaringClass(), type);
  }

  public static Type getGenericParameterType(Class<?> mainClass, Class<?> declaringClass, Type type) {
    if (type instanceof Class<?> || mainClass == declaringClass) {
      return type;
    }

    if (type instanceof TypeVariable) {
      TypeVariable<?>[] typeVariables = declaringClass.getTypeParameters();
      Type[] actualTypes;
      if (mainClass.getGenericSuperclass() != null) {
        actualTypes = getActualTypes(mainClass.getGenericSuperclass());
      } else {
        actualTypes = new Type[0];
        Type[] interfaceTypes = mainClass.getGenericInterfaces();
        for (Type t : interfaceTypes) {
          Type[] ttTypes = getActualTypes(t);
          Type[] tempTypes = new Type[actualTypes.length + ttTypes.length];
          System.arraycopy(actualTypes, 0, tempTypes, 0, actualTypes.length);
          System.arraycopy(ttTypes, 0, tempTypes, actualTypes.length, ttTypes.length);
          actualTypes = tempTypes;
        }
      }
      if (typeVariables.length != actualTypes.length) {
        throw new IllegalArgumentException(String
            .format("not implement (%s) (%s) (%s), "
                    + "e.g. extends multiple typed interface or too deep inheritance.",
                mainClass.getName(), declaringClass.getName(), type.getTypeName()));
      }
      for (int i = 0; i < typeVariables.length; i++) {
        if (typeVariables[i] == type) {
          return actualTypes[i];
        }
      }
    } else if (type instanceof GenericArrayType) {
      Class<?> t = (Class<?>) getGenericParameterType(mainClass, declaringClass,
          ((GenericArrayType) type).getGenericComponentType());
      return Array.newInstance(t, 0).getClass();
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type[] targetTypes = new Type[parameterizedType.getActualTypeArguments().length];
      for (int i = 0; i < parameterizedType.getActualTypeArguments().length; i++) {
        targetTypes[i] = getGenericParameterType(mainClass, declaringClass,
            parameterizedType.getActualTypeArguments()[i]);
      }
      return TypeUtils.parameterize((Class) parameterizedType.getRawType(), targetTypes);
    }
    throw new IllegalArgumentException(String
        .format("not implement (%s) (%s) (%s)",
            mainClass.getName(), declaringClass.getName(), type.getTypeName()));
  }

  private static Type[] getActualTypes(Type type) {
    if (type instanceof Class<?>) {
      if (((Class<?>) type).getSuperclass() != null) {
        return getActualTypes(((Class<?>) type).getSuperclass());
      } else {
        return getActualTypes(((Class<?>) type).getGenericInterfaces()[0]);
      }
    }
    if (type instanceof ParameterizedType) {
      return ((ParameterizedType) type).getActualTypeArguments();
    }
    return new Type[0];
  }
}
