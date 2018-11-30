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
package org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.internal.schema.FieldSchema;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Type;

public class EnumDeserializerSchema extends FieldSchema {
  private final JavaType javaType;

  private Map<Integer, Enum<?>> enumValues = new HashMap<>();

  public EnumDeserializerSchema(Field protoField, JavaType javaType) {
    super(protoField);
    this.javaType = javaType;

    if (!javaType.isEnumType()) {
      return;
    }

    try {
      Method method = javaType.getRawClass().getMethod("values");
      method.setAccessible(true);
      Object[] values = (Object[]) method.invoke(null);
      for (Object value : values) {
        Enum<?> enumValue = (Enum<?>) value;
        enumValues.put(enumValue.ordinal(), enumValue);
      }
    } catch (Throwable e) {
      throw new IllegalStateException(
          "Failed to collect enum values, class=" + javaType.getRawClass().getName(), e);
    }
  }

  @Override
  public void writeTo(Output output, Object message) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object readFrom(Input input) throws IOException {
    int ordinal = input.readInt32();
    if (javaType.isEnumType()) {
      return enumValues.get(ordinal);
    }

    return ordinal;
  }

  @Override
  public void mergeFrom(Input input, Object message) throws IOException {
    int ordinal = input.readInt32();

    if (javaType.isEnumType()) {
      Enum<?> enumValue = enumValues.get(ordinal);
      if (enumValue != null) {
        setter.set(message, enumValue);
        return;
      }

      throw new IllegalStateException(
          String.format("invalid enum ordinal value %d for %s, proto field=%s:%s",
              ordinal,
              javaType.getRawClass().getName(),
              ((Type) protoField.getParent()).getCanonicalName(),
              protoField.getName()));
    }

    setter.set(message, ordinal);
  }
}
