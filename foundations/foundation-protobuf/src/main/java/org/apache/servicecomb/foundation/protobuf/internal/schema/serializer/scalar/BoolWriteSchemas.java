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

import org.apache.servicecomb.foundation.common.utils.bean.BoolGetter;
import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class BoolWriteSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    if (boolean.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new BooleanPrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (Boolean.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new BooleanSchema<>(protoField, propertyDescriptor);
    }

    return new BooleanDynamicSchema<>(protoField, propertyDescriptor);
  }

  private static class BooleanDynamicSchema<T> extends FieldSchema<T> {
    public BooleanDynamicSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
    }

    @Override
    public final void writeTo(OutputEx output, Object value) throws IOException {
      if (value instanceof Boolean) {
        output.writeScalarBool(tag, tagSize, (boolean) value);
        return;
      }

      if (value instanceof Number) {
        output.writeScalarBool(tag, tagSize, ((Number) value).longValue() == 1);
        return;
      }

      if (value instanceof String[]) {
        if (((String[]) value).length == 0) {
          return;
        }
        boolean parsedValue = Boolean.parseBoolean(((String[]) value)[0]);
        output.writeScalarBool(tag, tagSize, parsedValue);
        return;
      }

      if (value instanceof String) {
        boolean parsedValue = Boolean.parseBoolean((String) value);
        output.writeScalarBool(tag, tagSize, parsedValue);
        return;
      }

      ProtoUtils.throwNotSupportWrite(protoField, value);
    }
  }

  private static class BooleanSchema<T> extends BooleanDynamicSchema<T> {
    protected final Getter<T, Boolean> getter;

    public BooleanSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      this.getter = javaType.isPrimitive() ? null : propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      Boolean value = getter.get(message);
      if (value != null) {
        output.writeScalarBool(tag, tagSize, value);
      }
    }
  }

  private static class BooleanPrimitiveSchema<T> extends BooleanDynamicSchema<T> {
    private final BoolGetter<T> primitiveGetter;

    public BooleanPrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      primitiveGetter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      boolean value = primitiveGetter.get(message);
      output.writeScalarBool(tag, tagSize, value);
    }
  }
}
