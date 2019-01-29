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

import org.apache.servicecomb.foundation.common.utils.bean.DoubleGetter;
import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class DoubleWriteSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    if (double.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new DoublePrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (Double.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new DoubleSchema<>(protoField, propertyDescriptor);
    }

    return new DoubleDynamicSchema<>(protoField, propertyDescriptor);
  }

  private static class DoubleDynamicSchema<T> extends FieldSchema<T> {
    public DoubleDynamicSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
    }

    @Override
    public final void writeTo(OutputEx output, Object value) throws IOException {
      if (value instanceof Number) {
        output.writeScalarDouble(tag, tagSize, ((Number) value).doubleValue());
        return;
      }

      if (value instanceof String[]) {
        if (((String[]) value).length == 0) {
          return;
        }
        double parsedValue = Double.parseDouble(((String[]) value)[0]);
        output.writeScalarDouble(tag, tagSize, parsedValue);
        return;
      }

      if (value instanceof String) {
        double parsedValue = Double.parseDouble((String) value);
        output.writeScalarDouble(tag, tagSize, parsedValue);
        return;
      }

      ProtoUtils.throwNotSupportWrite(protoField, value);
    }
  }

  private static class DoubleSchema<T> extends DoubleDynamicSchema<T> {
    protected final Getter<T, Double> getter;

    public DoubleSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      this.getter = javaType.isPrimitive() ? null : propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      Double value = getter.get(message);
      if (value != null) {
        output.writeScalarDouble(tag, tagSize, value);
      }
    }
  }

  private static class DoublePrimitiveSchema<T> extends DoubleDynamicSchema<T> {
    private final DoubleGetter<T> primitiveGetter;

    public DoublePrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      primitiveGetter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      double value = primitiveGetter.get(message);
      output.writeScalarDouble(tag, tagSize, value);
    }
  }
}
