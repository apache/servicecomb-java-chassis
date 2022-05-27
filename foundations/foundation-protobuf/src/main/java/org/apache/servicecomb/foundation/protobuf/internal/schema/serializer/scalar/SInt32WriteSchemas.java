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
import org.apache.servicecomb.foundation.common.utils.bean.IntGetter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class SInt32WriteSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    if (int.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new SInt32PrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (Integer.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new SInt32Schema<>(protoField, propertyDescriptor);
    }

    return new SInt32Schema<>(protoField, propertyDescriptor);
  }

  private static class SInt32DynamicSchema<T> extends FieldSchema<T> {
    public SInt32DynamicSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
    }

    @Override
    public final void writeTo(OutputEx output, Object value) throws IOException {
      if (value instanceof Number) {
        output.writeScalarSInt32(tag, tagSize, ((Number) value).intValue());
        return;
      }

      if (value instanceof String[]) {
        if (((String[]) value).length == 0) {
          return;
        }
        int parsedValue = Integer.parseInt(((String[]) value)[0], 10);
        output.writeScalarSInt32(tag, tagSize, parsedValue);
        return;
      }

      if (value instanceof String) {
        int parsedValue = Integer.parseInt((String) value, 10);
        output.writeScalarSInt32(tag, tagSize, parsedValue);
        return;
      }

      ProtoUtils.throwNotSupportWrite(protoField, value);
    }
  }

  private static class SInt32Schema<T> extends SInt32DynamicSchema<T> {
    protected final Getter<T, Object> getter;

    public SInt32Schema(Field protoField, PropertyDescriptor propertyDescriptor) {
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

  private static class SInt32PrimitiveSchema<T> extends SInt32DynamicSchema<T> {
    private final IntGetter<T> primitiveGetter;

    public SInt32PrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      primitiveGetter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      int value = primitiveGetter.get(message);
      output.writeScalarSInt32(tag, tagSize, value);
    }
  }
}
