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
package org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.scalar;

import java.io.IOException;

import org.apache.servicecomb.foundation.common.base.DynamicEnum;
import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.EnumMeta;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Type;
import io.protostuff.runtime.FieldSchema;

public class EnumWriteSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    if (propertyDescriptor.getJavaType().isEnumType()) {
      return new EnumSchema<>(protoField, propertyDescriptor);
    }

    return new EnumSchema<>(protoField, propertyDescriptor);
  }

  private static class EnumDynamicSchema<T> extends FieldSchema<T> {
    private EnumMeta enumMeta;

    public EnumDynamicSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.enumMeta = new EnumMeta(protoField, javaType);
    }

    protected final void numberWrite(OutputEx output, Number value) throws IOException {
      int enumValue = value.intValue();
      if (!enumMeta.containsValue(enumValue)) {
        throw new IllegalStateException(
            String.format("invalid enum value %d for proto %s, field=%s:%s",
                enumValue,
                protoField.getTypeName(),
                ((Type) protoField.getParent()).getCanonicalName(),
                protoField.getName()));
      }

      output.writeScalarInt32(tag, tagSize, enumValue);
    }

    protected final void stringWrite(OutputEx output, String enumName) throws IOException {
      Integer enumValue = enumMeta.getValueByName(enumName);
      if (enumValue == null) {
        throw new IllegalStateException(
            String.format("invalid enum name %s for proto %s, field=%s:%s",
                enumName,
                protoField.getTypeName(),
                ((Type) protoField.getParent()).getCanonicalName(),
                protoField.getName()));
      }

      output.writeScalarInt32(tag, tagSize, enumValue);
    }

    @Override
    public final void writeTo(OutputEx output, Object value) throws IOException {
      if (value instanceof Enum) {
        stringWrite(output, ((Enum<?>) value).name());
        return;
      }

      if (value instanceof DynamicEnum) {
        // protobuf can not support unknown enum, because protobuf encode enum as tag value
        writeTo(output, ((DynamicEnum<?>) value).getValue());
        return;
      }

      if (value instanceof Number) {
        // need to check if it is a valid number
        // because maybe come from http request
        numberWrite(output, ((Number) value).intValue());
        return;
      }

      if (value instanceof String[]) {
        if (((String[]) value).length == 0) {
          return;
        }

        stringWrite(output, ((String[]) value)[0]);
        return;
      }

      if (value instanceof String) {
        stringWrite(output, (String) value);
        return;
      }

      ProtoUtils.throwNotSupportWrite(protoField, value);
    }
  }

  private static class EnumSchema<T> extends EnumDynamicSchema<T> {
    protected final Getter<T, Object> getter;

    public EnumSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      this.getter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      Object value = getter.get(message);
      if (value != null) {
        writeTo(output, value);
      }
    }
  }
}
