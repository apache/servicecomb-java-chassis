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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * common utils to convert java types.
 */
public class TypesUtil {
  private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new HashMap<>();

  static {
    PRIMITIVE_TO_WRAPPER.put(byte.class, Byte.class);
    PRIMITIVE_TO_WRAPPER.put(short.class, Short.class);
    PRIMITIVE_TO_WRAPPER.put(int.class, Integer.class);
    PRIMITIVE_TO_WRAPPER.put(long.class, Long.class);
    PRIMITIVE_TO_WRAPPER.put(float.class, Float.class);
    PRIMITIVE_TO_WRAPPER.put(double.class, Double.class);
    PRIMITIVE_TO_WRAPPER.put(boolean.class, Boolean.class);
    PRIMITIVE_TO_WRAPPER.put(char.class, Character.class);
  }


  private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = new HashMap<>();

  static {
    WRAPPER_TO_PRIMITIVE.put(Byte.class, byte.class);
    WRAPPER_TO_PRIMITIVE.put(Short.class, short.class);
    WRAPPER_TO_PRIMITIVE.put(Integer.class, int.class);
    WRAPPER_TO_PRIMITIVE.put(Long.class, long.class);
    WRAPPER_TO_PRIMITIVE.put(Float.class, float.class);
    WRAPPER_TO_PRIMITIVE.put(Double.class, double.class);
    WRAPPER_TO_PRIMITIVE.put(Boolean.class, boolean.class);
    WRAPPER_TO_PRIMITIVE.put(Character.class, char.class);
  }

  public static final JavaType PRIMITIVE_BYTE = TypeFactory.defaultInstance().constructType(byte.class);

  public static final JavaType PRIMITIVE_SHORT = TypeFactory.defaultInstance().constructType(short.class);

  public static final JavaType PRIMITIVE_INT = TypeFactory.defaultInstance().constructType(int.class);

  public static final JavaType PRIMITIVE_LONG = TypeFactory.defaultInstance().constructType(long.class);

  public static final JavaType PRIMITIVE_FLOAT = TypeFactory.defaultInstance().constructType(float.class);

  public static final JavaType PRIMITIVE_DOUBLE = TypeFactory.defaultInstance().constructType(double.class);

  public static final JavaType PRIMITIVE_BOOLEAN = TypeFactory.defaultInstance().constructType(boolean.class);

  public static final JavaType PRIMITIVE_CHAR = TypeFactory.defaultInstance().constructType(char.class);

  public static final JavaType PRIMITIVE_WRAPPER_BYTE = TypeFactory.defaultInstance().constructType(Byte.class);

  public static final JavaType PRIMITIVE_WRAPPER_SHORT = TypeFactory.defaultInstance().constructType(Short.class);

  public static final JavaType PRIMITIVE_WRAPPER_INT = TypeFactory.defaultInstance().constructType(Integer.class);

  public static final JavaType PRIMITIVE_WRAPPER_LONG = TypeFactory.defaultInstance().constructType(Long.class);

  public static final JavaType PRIMITIVE_WRAPPER_FLOAT = TypeFactory.defaultInstance().constructType(Float.class);

  public static final JavaType PRIMITIVE_WRAPPER_DOUBLE = TypeFactory.defaultInstance().constructType(Double.class);

  public static final JavaType PRIMITIVE_WRAPPER_BOOLEAN = TypeFactory.defaultInstance().constructType(Boolean.class);

  public static final JavaType PRIMITIVE_WRAPPER_CHAR = TypeFactory.defaultInstance().constructType(Character.class);


  private static final Map<JavaType, JavaType> PRIMITIVE_TO_WRAPPER_JAVATYPE = new HashMap<>();

  static {
    PRIMITIVE_TO_WRAPPER_JAVATYPE.put(PRIMITIVE_BYTE, PRIMITIVE_WRAPPER_BYTE);
    PRIMITIVE_TO_WRAPPER_JAVATYPE.put(PRIMITIVE_SHORT, PRIMITIVE_WRAPPER_SHORT);
    PRIMITIVE_TO_WRAPPER_JAVATYPE.put(PRIMITIVE_INT, PRIMITIVE_WRAPPER_INT);
    PRIMITIVE_TO_WRAPPER_JAVATYPE.put(PRIMITIVE_LONG, PRIMITIVE_WRAPPER_LONG);
    PRIMITIVE_TO_WRAPPER_JAVATYPE.put(PRIMITIVE_FLOAT, PRIMITIVE_WRAPPER_FLOAT);
    PRIMITIVE_TO_WRAPPER_JAVATYPE.put(PRIMITIVE_DOUBLE, PRIMITIVE_WRAPPER_DOUBLE);
    PRIMITIVE_TO_WRAPPER_JAVATYPE.put(PRIMITIVE_BOOLEAN, PRIMITIVE_WRAPPER_BOOLEAN);
    PRIMITIVE_TO_WRAPPER_JAVATYPE.put(PRIMITIVE_CHAR, PRIMITIVE_WRAPPER_CHAR);
  }

  private static final Map<JavaType, JavaType> WRAPPER_TO_PRIMITIVE_JAVATYPE = new HashMap<>();

  static {
    WRAPPER_TO_PRIMITIVE_JAVATYPE.put(PRIMITIVE_WRAPPER_BYTE, PRIMITIVE_BYTE);
    WRAPPER_TO_PRIMITIVE_JAVATYPE.put(PRIMITIVE_WRAPPER_SHORT, PRIMITIVE_SHORT);
    WRAPPER_TO_PRIMITIVE_JAVATYPE.put(PRIMITIVE_WRAPPER_INT, PRIMITIVE_INT);
    WRAPPER_TO_PRIMITIVE_JAVATYPE.put(PRIMITIVE_WRAPPER_LONG, PRIMITIVE_LONG);
    WRAPPER_TO_PRIMITIVE_JAVATYPE.put(PRIMITIVE_WRAPPER_FLOAT, PRIMITIVE_FLOAT);
    WRAPPER_TO_PRIMITIVE_JAVATYPE.put(PRIMITIVE_WRAPPER_DOUBLE, PRIMITIVE_DOUBLE);
    WRAPPER_TO_PRIMITIVE_JAVATYPE.put(PRIMITIVE_WRAPPER_BOOLEAN, PRIMITIVE_BOOLEAN);
    WRAPPER_TO_PRIMITIVE_JAVATYPE.put(PRIMITIVE_WRAPPER_CHAR, PRIMITIVE_CHAR);
  }

  public static Class<?> primitiveTypeToWrapper(Class<?> primitiveType) {
    return PRIMITIVE_TO_WRAPPER.get(primitiveType);
  }

  public static Class<?> wrapperTypeToPrimitive(Class<?> wrapperType) {
    return WRAPPER_TO_PRIMITIVE.get(wrapperType);
  }

  public static JavaType primitiveJavaTypeToWrapper(JavaType primitiveType) {
    return PRIMITIVE_TO_WRAPPER_JAVATYPE.get(primitiveType);
  }

  public static JavaType wrapperJavaTypeToPrimitive(JavaType wrapperType) {
    return WRAPPER_TO_PRIMITIVE_JAVATYPE.get(wrapperType);
  }
}
