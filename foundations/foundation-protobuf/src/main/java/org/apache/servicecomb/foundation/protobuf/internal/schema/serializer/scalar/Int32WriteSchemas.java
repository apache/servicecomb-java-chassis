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

import org.apache.servicecomb.foundation.common.utils.bean.ByteGetter;
import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.IntGetter;
import org.apache.servicecomb.foundation.common.utils.bean.ShortGetter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public final class Int32WriteSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    if (int.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new IntFieldInt32PrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (short.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new ShortFieldInt32PrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (byte.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new ByteFieldInt32PrimitiveSchema<>(protoField, propertyDescriptor);
    }

    return new Int32Schema<>(protoField, propertyDescriptor);
  }

  private static class Int32DynamicSchema<T> extends FieldSchema<T> {
    public Int32DynamicSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
    }

    @Override
    public final void writeTo(OutputEx output, Object value) throws IOException {
      if (value instanceof Number) {
        output.writeScalarInt32(tag, tagSize, ((Number) value).intValue());
        return;
      }

      if (value instanceof String[]) {
        if (((String[]) value).length == 0) {
          return;
        }
        int parsedValue = Integer.parseInt(((String[]) value)[0], 10);
        output.writeScalarInt32(tag, tagSize, parsedValue);
        return;
      }

      if (value instanceof String) {
        if (((String) value).isEmpty()) {
          return;
        }
        int parsedValue = Integer.parseInt((String) value, 10);
        output.writeScalarInt32(tag, tagSize, parsedValue);
        return;
      }

      ProtoUtils.throwNotSupportWrite(protoField, value);
    }
  }

  private static class Int32Schema<T> extends Int32DynamicSchema<T> {
    protected final Getter<T, Object> getter;

    public Int32Schema(Field protoField, PropertyDescriptor propertyDescriptor) {
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

  private static final class IntFieldInt32PrimitiveSchema<T> extends Int32DynamicSchema<T> {
    private final IntGetter<T> primitiveGetter;

    public IntFieldInt32PrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      primitiveGetter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      int value = primitiveGetter.get(message);
      output.writeScalarInt32(tag, tagSize, value);
    }
  }

  private static final class ShortFieldInt32PrimitiveSchema<T> extends Int32DynamicSchema<T> {
    private final ShortGetter<T> primitiveGetter;

    public ShortFieldInt32PrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      primitiveGetter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      short value = primitiveGetter.get(message);
      output.writeScalarInt32(tag, tagSize, value);
    }
  }

  private static final class ByteFieldInt32PrimitiveSchema<T> extends Int32DynamicSchema<T> {
    private final ByteGetter<T> primitiveGetter;

    public ByteFieldInt32PrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      primitiveGetter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      byte value = primitiveGetter.get(message);
      output.writeScalarInt32(tag, tagSize, value);
    }
  }
}
