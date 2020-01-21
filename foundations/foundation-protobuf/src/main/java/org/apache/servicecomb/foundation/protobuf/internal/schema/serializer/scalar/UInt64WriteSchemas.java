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

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class UInt64WriteSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    if (long.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new UInt64PrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (Long.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new UInt64Schema<>(protoField, propertyDescriptor);
    }

    return new UInt64Schema<>(protoField, propertyDescriptor);
  }

  private static class UInt64DynamicSchema<T> extends FieldSchema<T> {
    public UInt64DynamicSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
    }

    @Override
    public final void writeTo(OutputEx output, Object value) throws IOException {
      if (value instanceof Number) {
        output.writeScalarUInt64(tag, tagSize, ((Number) value).longValue());
        return;
      }

      if (value instanceof String[]) {
        if (((String[]) value).length == 0) {
          return;
        }
        long parsedValue = Long.parseLong(((String[]) value)[0], 10);
        output.writeScalarUInt64(tag, tagSize, parsedValue);
        return;
      }

      if (value instanceof String) {
        long parsedValue = Long.parseLong((String) value, 10);
        output.writeScalarUInt64(tag, tagSize, parsedValue);
        return;
      }

      ProtoUtils.throwNotSupportWrite(protoField, value);
    }
  }

  private static class UInt64Schema<T> extends UInt64DynamicSchema<T> {
    protected final Getter<T, Object> getter;

    public UInt64Schema(Field protoField, PropertyDescriptor propertyDescriptor) {
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

  private static class UInt64PrimitiveSchema<T> extends UInt64DynamicSchema<T> {
    private final Getter<T, Long> primitiveGetter;

    public UInt64PrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      primitiveGetter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      long value = primitiveGetter.get(message);
      output.writeScalarUInt64(tag, tagSize, value);
    }
  }
}
