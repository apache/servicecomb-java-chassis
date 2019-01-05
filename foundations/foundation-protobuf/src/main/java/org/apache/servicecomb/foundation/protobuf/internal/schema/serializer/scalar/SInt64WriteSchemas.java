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
import org.apache.servicecomb.foundation.common.utils.bean.LongGetter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class SInt64WriteSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    if (long.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new SInt64PrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (Long.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new SInt64Schema<>(protoField, propertyDescriptor);
    }

    return new SInt64DynamicSchema<>(protoField, propertyDescriptor);
  }

  private static class SInt64DynamicSchema<T> extends FieldSchema<T> {
    public SInt64DynamicSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
    }

    @Override
    public final void writeTo(OutputEx output, Object value) throws IOException {
      if (value instanceof Number) {
        output.writeScalarSInt64(tag, tagSize, ((Number) value).longValue());
        return;
      }

      if (value instanceof String[]) {
        if (((String[]) value).length == 0) {
          return;
        }
        long parsedValue = Long.parseLong(((String[]) value)[0], 10);
        output.writeScalarSInt64(tag, tagSize, parsedValue);
        return;
      }

      if (value instanceof String) {
        long parsedValue = Long.parseLong((String) value, 10);
        output.writeScalarSInt64(tag, tagSize, parsedValue);
        return;
      }

      ProtoUtils.throwNotSupportWrite(protoField, value);
    }
  }

  private static class SInt64Schema<T> extends SInt64DynamicSchema<T> {
    protected final Getter<T, Long> getter;

    public SInt64Schema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      this.getter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      Long value = getter.get(message);
      if (value != null) {
        output.writeScalarSInt64(tag, tagSize, value);
      }
    }
  }

  private static class SInt64PrimitiveSchema<T> extends SInt64DynamicSchema<T> {
    private final LongGetter<T> primitiveGetter;

    public SInt64PrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      primitiveGetter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      long value = primitiveGetter.get(message);
      output.writeScalarSInt64(tag, tagSize, value);
    }
  }
}
