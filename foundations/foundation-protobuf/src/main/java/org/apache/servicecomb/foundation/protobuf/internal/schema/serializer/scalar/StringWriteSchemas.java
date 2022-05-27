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

import org.apache.servicecomb.foundation.common.utils.bean.CharGetter;
import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class StringWriteSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    if (char.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new CharFieldStringSchema<>(protoField, propertyDescriptor);
    }

    if (String.class.equals(propertyDescriptor.getJavaType().getRawClass())) {
      return new StringSchema<>(protoField, propertyDescriptor);
    }

    return new StringSchema<>(protoField, propertyDescriptor);
  }

  private static class StringDynamicSchema<T> extends FieldSchema<T> {
    public StringDynamicSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
    }

    @Override
    public final void writeTo(OutputEx output, Object value) throws IOException {
      if (value instanceof String) {
        output.writeScalarString(tag, tagSize, (String) value);
        return;
      }

      if (value instanceof String[]) {
        if (((String[]) value).length == 0) {
          return;
        }
        output.writeScalarString(tag, tagSize, ((String[]) value)[0]);
        return;
      }

      if (value instanceof Character) {
        output.writeScalarString(tag, tagSize, String.valueOf((char) value));
        return;
      }

      ProtoUtils.throwNotSupportWrite(protoField, value);
    }
  }

  private static class StringSchema<T> extends StringDynamicSchema<T> {
    protected final Getter<T, Object> getter;

    public StringSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
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

  private static class CharFieldStringSchema<T> extends StringDynamicSchema<T> {
    protected final CharGetter<T> getter;

    public CharFieldStringSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor);

      this.getter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      char value = getter.get(message);
      writeTo(output, value);
    }
  }
}
