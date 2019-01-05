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
package org.apache.servicecomb.foundation.protobuf.internal.schema;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.compiler.model.EnumConstant;
import io.protostuff.compiler.model.Field;

public class EnumMeta {
  private Map<String, Integer> enumNameToValueMap = new HashMap<>();

  // key is idl defined enum value
  // value is Enum<?> or null
  private Map<Integer, Enum<?>> enumValues = new HashMap<>();

  public EnumMeta(Field protoField, JavaType javaType) {
    io.protostuff.compiler.model.Enum enumType = (io.protostuff.compiler.model.Enum) protoField.getType();
    for (EnumConstant enumConstant : enumType.getConstants()) {
      enumNameToValueMap.put(enumConstant.getName(), enumConstant.getValue());
      enumValues.put(enumConstant.getValue(), null);
    }

    if (!javaType.isEnumType()) {
      return;
    }

    try {
      Method method = javaType.getRawClass().getMethod("values");
      method.setAccessible(true);
      Object[] values = (Object[]) method.invoke(null);
      for (Object value : values) {
        Enum<?> enumValue = (Enum<?>) value;
        enumValues.put(enumNameToValueMap.get(enumValue.name()), enumValue);
      }
    } catch (Throwable e) {
      throw new IllegalStateException(
          "Failed to collect enum values, class=" + javaType.getRawClass().getName(), e);
    }
  }

  public boolean containsValue(Integer value) {
    return enumValues.containsKey(value);
  }

  public Enum<?> getEnumByValue(int enumValue) {
    return enumValues.get(enumValue);
  }

  public Integer getValueByName(String name) {
    return enumNameToValueMap.get(name);
  }
}
