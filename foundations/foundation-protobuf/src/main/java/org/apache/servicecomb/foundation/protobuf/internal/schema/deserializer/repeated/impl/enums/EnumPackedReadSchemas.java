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
package org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.enums;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.EnumMeta;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.AbstractReaders;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.RepeatedReadSchemas;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.InputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Type;
import io.protostuff.runtime.FieldSchema;

public class EnumPackedReadSchemas {
  private static class EnumPackedReaders extends AbstractReaders<Enum<?>> {
    private final EnumMeta enumMeta;

    public EnumPackedReaders(Field protoField, JavaType javaType) {
      super(protoField, EnumSchemaUtils.constructEnumArrayClass(javaType));
      this.enumMeta = new EnumMeta(protoField, javaType.getContentType());

      collectionReader = (input, collection) -> {
        while (true) {
          int value = input.readPackedEnum();
          Enum<?> enumValue = enumMeta.getEnumByValue(value);
          if (enumValue == null) {
            throw new IllegalStateException(
                String.format("invalid enum value %d for %s, proto field=%s:%s",
                    value,
                    javaType.getRawClass().getName(),
                    ((Type) protoField.getParent()).getCanonicalName(),
                    protoField.getName()));
          }

          collection.add(enumValue);

          int fieldNumber = input.readFieldNumber();
          if (fieldNumber != this.fieldNumber) {
            return fieldNumber;
          }
        }
      };
    }
  }

  private static class EnumIntPackedSchema<T> extends FieldSchema<T> {
    private final Getter<T, Collection<Integer>> getter;

    private final Setter<T, Collection<Integer>> setter;

    public EnumIntPackedSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
      super(protoField, propertyDescriptor.getJavaType());
      this.getter = propertyDescriptor.getGetter();
      this.setter = propertyDescriptor.getSetter();
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      Collection<Integer> collection = getter.get(message);
      if (collection == null) {
        collection = new ArrayList<>();
        setter.set(message, collection);
      }

      while (true) {
        int value = input.readPackedEnum();
        collection.add(value);

        int fieldNumber = input.readFieldNumber();
        if (fieldNumber != this.fieldNumber) {
          return fieldNumber;
        }
      }
    }
  }

  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    JavaType javaType = propertyDescriptor.getJavaType();
    if (javaType.isJavaLangObject()) {
      return new EnumIntPackedSchema<>(protoField, propertyDescriptor);
    }

    return RepeatedReadSchemas
        .create(protoField, propertyDescriptor, new EnumPackedReaders(protoField, propertyDescriptor.getJavaType()));
  }
}
