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

import org.apache.servicecomb.foundation.common.utils.bean.ByteSetter;
import org.apache.servicecomb.foundation.common.utils.bean.IntSetter;
import org.apache.servicecomb.foundation.common.utils.bean.ShortSetter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.AbstractScalarReadSchemas.AbstractIntSchema;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.InputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class Int32ReadSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    JavaType javaType = propertyDescriptor.getJavaType();
    if (int.class.equals(javaType.getRawClass())) {
      return new IntFieldIntPrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (short.class.equals(javaType.getRawClass())) {
      return new ShortFieldIntPrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (byte.class.equals(javaType.getRawClass())) {
      return new ByteFieldIntPrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (Integer.class.equals(javaType.getRawClass()) || Byte.class.equals(javaType.getRawClass()) || Short.class
        .equals(javaType.getRawClass()) || javaType
        .isJavaLangObject()) {
      return new Int32Schema<>(protoField, propertyDescriptor);
    }

    ProtoUtils.throwNotSupportMerge(protoField, propertyDescriptor.getJavaType());
    return null;
  }

  private static class Int32Schema<T> extends AbstractIntSchema<T> {
    public Int32Schema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      int value = input.readInt32();
      if (Byte.class.equals(javaType.getRawClass())) {
        setter.set(message, (byte) value);
      } else if (Short.class.equals(javaType.getRawClass())) {
        setter.set(message, (short) value);
      } else {
        setter.set(message, value);
      }
      return input.readFieldNumber();
    }
  }

  private static class IntFieldIntPrimitiveSchema<T> extends FieldSchema<T> {
    protected final IntSetter<T> setter;

    public IntFieldIntPrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
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

  private static class ShortFieldIntPrimitiveSchema<T> extends FieldSchema<T> {
    protected final ShortSetter<T> setter;

    public ShortFieldIntPrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      int value = input.readInt32();
      setter.set(message, (short) value);
      return input.readFieldNumber();
    }
  }

  private static class ByteFieldIntPrimitiveSchema<T> extends FieldSchema<T> {
    protected final ByteSetter<T> setter;

    public ByteFieldIntPrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      int value = input.readInt32();
      setter.set(message, (byte) value);
      return input.readFieldNumber();
    }
  }
}
