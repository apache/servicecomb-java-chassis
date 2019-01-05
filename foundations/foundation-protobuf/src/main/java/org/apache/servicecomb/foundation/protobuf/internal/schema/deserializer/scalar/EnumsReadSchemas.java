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
package org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar;

import java.io.IOException;

import org.apache.servicecomb.foundation.common.utils.bean.IntSetter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.EnumMeta;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.InputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Type;
import io.protostuff.runtime.FieldSchema;

public class EnumsReadSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    JavaType javaType = propertyDescriptor.getJavaType();
    if (javaType.isEnumType()) {
      return new EnumSchema<>(protoField, propertyDescriptor);
    }

    if (Integer.class.equals(javaType.getRawClass()) || javaType.isJavaLangObject()) {
      return new IntEnumSchema<>(protoField, propertyDescriptor);
    }

    if (int.class.equals(javaType.getRawClass())) {
      return new IntPrimitiveEnumSchema<>(protoField, propertyDescriptor);
    }

    ProtoUtils.throwNotSupportMerge(protoField, propertyDescriptor.getJavaType());
    return null;
  }

  private static class EnumSchema<T> extends FieldSchema<T> {
    private final Setter<T, Enum<?>> setter;

    private final EnumMeta enumMeta;

    public EnumSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
      enumMeta = new EnumMeta(protoField, javaType);
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      int value = input.readInt32();
      Enum<?> enumValue = enumMeta.getEnumByValue(value);
      if (enumValue != null) {
        setter.set(message, enumValue);
        return input.readFieldNumber();
      }

      throw new IllegalStateException(
          String.format("invalid enum value %d for %s, proto field=%s:%s",
              value,
              javaType.getRawClass().getName(),
              ((Type) protoField.getParent()).getCanonicalName(),
              protoField.getName()));
    }
  }

  private static class IntEnumSchema<T> extends FieldSchema<T> {
    private final Setter<T, Integer> setter;

    public IntEnumSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      int value = input.readInt32();
      setter.set(message, value);
      return input.readFieldNumber();
    }
  }

  private static class IntPrimitiveEnumSchema<T> extends FieldSchema<T> {
    private final IntSetter<T> setter;

    public IntPrimitiveEnumSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      int value = input.readInt32();
      setter.set(message, value);
      return input.readFieldNumber();
    }
  }
}
