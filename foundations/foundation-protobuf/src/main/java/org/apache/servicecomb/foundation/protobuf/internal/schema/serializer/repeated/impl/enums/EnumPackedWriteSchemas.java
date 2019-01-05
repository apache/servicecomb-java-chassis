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
package org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.enums;

import java.io.IOException;
import java.util.Collection;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.EnumMeta;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.AbstractWriters;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.RepeatedWriteSchemas;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Type;
import io.protostuff.runtime.FieldSchema;

public class EnumPackedWriteSchemas {
  private static class EnumsPackedWriters extends AbstractWriters<Enum<?>> {
    private final EnumMeta enumMeta;

    @SuppressWarnings("unchecked")
    public EnumsPackedWriters(Field protoField, JavaType javaType) {
      super(protoField, ReflectUtils.constructArrayType(Enum.class));
      this.enumMeta = new EnumMeta(protoField, javaType);

      arrayWriter = (o, value) -> o.writeObject(tag, tagSize, value, (output, array) -> {
        for (Enum<?> element : array) {
          if (element == null) {
            ProtoUtils.throwNotSupportNullElement(protoField);
            return;
          }

          String name = element.name();
          Integer enumValue = enumMeta.getValueByName(name);
          if (enumValue == null) {
            throw new IllegalStateException(
                String.format("invalid enum name %s for proto %s, field=%s:%s",
                    name,
                    protoField.getTypeName(),
                    ((Type) protoField.getParent()).getCanonicalName(),
                    protoField.getName()));
          }

          output.writePackedEnum(enumValue);
        }
      });

      collectionWriter = (o, value) -> o.writeObject(tag, tagSize, value, (output, collection) -> {
        Object first = collection.iterator().next();
        if (first.getClass().isEnum()) {
          writeEnumCollection(output, collection);
          return;
        }

        if (first.getClass() == String.class) {
          writeStringCollection(output, (Collection<String>) (Object) collection);
          return;
        }

        writeIntCollection(output, (Collection<Number>) (Object) collection);
      });

      stringArrayWriter = (o, value) -> o.writeObject(tag, tagSize, value, (output, array) -> {
        for (String element : array) {
          if (element == null) {
            ProtoUtils.throwNotSupportNullElement(protoField);
            return;
          }

          Integer enumValue = enumMeta.getValueByName(element);
          if (enumValue == null) {
            throw new IllegalStateException(
                String.format("invalid enum name %s for proto %s, field=%s:%s",
                    element,
                    protoField.getTypeName(),
                    ((Type) protoField.getParent()).getCanonicalName(),
                    protoField.getName()));
          }

          output.writePackedEnum(enumValue);
        }
      });
    }

    private void writeStringCollection(OutputEx output, Collection<String> collection) throws IOException {
      for (String element : collection) {
        if (element == null) {
          ProtoUtils.throwNotSupportNullElement(protoField);
          return;
        }

        Integer enumValue = enumMeta.getValueByName(element);
        if (enumValue == null) {
          throw new IllegalStateException(
              String.format("invalid enum name %s for proto %s, field=%s:%s",
                  element,
                  protoField.getTypeName(),
                  ((Type) protoField.getParent()).getCanonicalName(),
                  protoField.getName()));
        }

        output.writePackedEnum(enumValue);
      }
    }

    private void writeIntCollection(OutputEx output, Collection<Number> collection) throws IOException {
      for (Number element : collection) {
        if (element == null) {
          ProtoUtils.throwNotSupportNullElement(protoField);
          return;
        }

        output.writePackedInt32(element.intValue());
      }
    }

    private void writeEnumCollection(OutputEx output, Collection<Enum<?>> collection) throws IOException {
      for (Enum<?> element : collection) {
        if (element == null) {
          ProtoUtils.throwNotSupportNullElement(protoField);
          return;
        }

        String name = element.name();
        Integer enumValue = enumMeta.getValueByName(name);
        if (enumValue == null) {
          throw new IllegalStateException(
              String.format("invalid enum name %s for proto %s, field=%s:%s",
                  name,
                  protoField.getTypeName(),
                  ((Type) protoField.getParent()).getCanonicalName(),
                  protoField.getName()));
        }

        output.writePackedEnum(enumValue);
      }
    }
  }

  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    return RepeatedWriteSchemas
        .create(protoField, propertyDescriptor, new EnumsPackedWriters(protoField, propertyDescriptor.getJavaType()));
  }
}
