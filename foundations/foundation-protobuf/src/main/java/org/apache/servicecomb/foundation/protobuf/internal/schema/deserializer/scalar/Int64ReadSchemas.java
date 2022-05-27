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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.apache.servicecomb.foundation.common.utils.bean.LongSetter;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.AbstractScalarReadSchemas.AbstractLongSchema;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.InputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class Int64ReadSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    JavaType javaType = propertyDescriptor.getJavaType();
    if (long.class.equals(javaType.getRawClass())) {
      return new LongFiledLongPrimitiveSchema<>(protoField, propertyDescriptor);
    }

    return new Int64Schema<>(protoField, propertyDescriptor);
  }

  private static class Int64Schema<T> extends AbstractLongSchema<T> {
    public Int64Schema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      long value = input.readInt64();
      if (Date.class.equals(javaType.getRawClass())) {
        setter.set(message, new Date(value));
      } else if (LocalDate.class.equals(javaType.getRawClass())) {
        setter.set(message, LocalDate.ofEpochDay(value));
      } else if (LocalDateTime.class.equals(javaType.getRawClass())) {
        setter.set(message, LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC));
      } else {
        setter.set(message, value);
      }
      return input.readFieldNumber();
    }
  }

  private static class LongFiledLongPrimitiveSchema<T> extends FieldSchema<T> {
    protected final LongSetter<T> setter;

    public LongFiledLongPrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      long value = input.readInt64();
      setter.set(message, value);
      return input.readFieldNumber();
    }
  }
}
