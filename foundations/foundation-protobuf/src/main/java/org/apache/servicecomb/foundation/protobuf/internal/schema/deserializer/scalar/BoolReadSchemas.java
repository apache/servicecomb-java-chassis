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

import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.InputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class BoolReadSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    JavaType javaType = propertyDescriptor.getJavaType();
    if (boolean.class.equals(javaType.getRawClass())) {
      return new BooleanPrimitiveSchema<>(protoField, propertyDescriptor);
    }

    if (Boolean.class.equals(javaType.getRawClass()) || javaType.isJavaLangObject()) {
      return new BooleanSchema<>(protoField, propertyDescriptor);
    }

    ProtoUtils.throwNotSupportMerge(protoField, propertyDescriptor.getJavaType());
    return null;
  }

  private static class BooleanSchema<T> extends FieldSchema<T> {
    private final Setter<T, Boolean> setter;

    public BooleanSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      boolean value = input.readBool();
      setter.set(message, value);
      return input.readFieldNumber();
    }
  }

  private static class BooleanPrimitiveSchema<T> extends FieldSchema<T> {
    private final Setter<T, Boolean> setter;

    public BooleanPrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.setter = propertyDescriptor.getSetter();
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      boolean value = input.readBool();
      setter.set(message, value);
      return input.readFieldNumber();
    }
  }
}
