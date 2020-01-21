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

public class FloatWriteSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    if (float.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new FloatPrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (Float.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new FloatSchema<>(protoField, propertyDescriptor);
    }

    return new FloatSchema<>(protoField, propertyDescriptor);
  }

  private static class FloatDynamicSchema<T> extends FieldSchema<T> {
    public FloatDynamicSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
    }

    @Override
    public final void writeTo(OutputEx output, Object value) throws IOException {
      if (value instanceof Number) {
        output.writeScalarFloat(tag, tagSize, ((Number) value).floatValue());
        return;
      }

      if (value instanceof String[]) {
        if (((String[]) value).length == 0) {
          return;
        }
        float parsedValue = Float.parseFloat(((String[]) value)[0]);
        output.writeScalarFloat(tag, tagSize, parsedValue);
        return;
      }

      if (value instanceof String) {
        float parsedValue = Float.parseFloat((String) value);
        output.writeScalarFloat(tag, tagSize, parsedValue);
        return;
      }

      ProtoUtils.throwNotSupportWrite(protoField, value);
    }
  }

  private static class FloatSchema<T> extends FloatDynamicSchema<T> {
    protected final Getter<T, Object> getter;

    public FloatSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      this.getter = javaType.isPrimitive() ? null : propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      Object value = getter.get(message);
      if (value != null) {
        writeTo(output, value);
      }
    }
  }

  private static class FloatPrimitiveSchema<T> extends FloatDynamicSchema<T> {
    private final Getter<T, Float> primitiveGetter;

    public FloatPrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      primitiveGetter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      float value = primitiveGetter.get(message);
      output.writeScalarFloat(tag, tagSize, value);
    }
  }
}
