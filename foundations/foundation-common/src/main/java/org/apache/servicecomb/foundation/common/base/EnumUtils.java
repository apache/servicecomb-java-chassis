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
package org.apache.servicecomb.foundation.common.base;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

public interface EnumUtils {
  @SuppressWarnings("unchecked")
  static <T> T readEnum(Field enumField) {
    try {
      return (T) enumField.get(null);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("failed to read enum, field=" + enumField, e);
    }
  }

  static boolean isEnumField(Class<?> cls, Field field) {
    return Modifier.isStatic(field.getModifiers()) && cls.equals(field.getType());
  }

  static Stream<Field> findEnumFields(Class<?> cls) {
    return Arrays.stream(cls.getFields())
        .filter(field -> isEnumField(cls, field));
  }

  static boolean isDynamicEnum(Class<?> cls) {
    return DynamicEnum.class.isAssignableFrom(cls);
  }
}
