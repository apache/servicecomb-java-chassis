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
package org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl;

import java.lang.reflect.Array;

import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.MessageWriteSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.AbstractWriters;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.RepeatedWriteSchemas;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.SchemaWriter;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class PropertyWrapperRepeatedWriteSchemas {
  private static class PropertyWrapperWriters extends AbstractWriters<Object> {
    public PropertyWrapperWriters(Field protoField, SchemaWriter<Object> elementSchema, Class<Object[]> arrayClass) {
      super(protoField, arrayClass);

      FieldSchema<Object> fieldSchema =
          (elementSchema instanceof MessageWriteSchema) ? ((MessageWriteSchema<Object>) elementSchema)
              .getMainPojoFieldMaps()
              .getFieldByNumber(1) : null;
      arrayWriter = (output, array) -> {
        for (Object element : array) {
          if (element != null) {
            output.writeObject(tag, tagSize, element, fieldSchema::writeTo);
            continue;
          }

          ProtoUtils.throwNotSupportNullElement(protoField);
        }
      };

      collectionWriter = (output, collection) -> {
        for (Object element : collection) {
          if (element != null) {
            output.writeObject(tag, tagSize, element, fieldSchema::writeTo);
            continue;
          }

          ProtoUtils.throwNotSupportNullElement(protoField);
        }
      };
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor,
      SchemaWriter<Object> elementSchema) {
    JavaType contentType = propertyDescriptor.getJavaType().getContentType();
    Class<Object> contentClass = contentType == null ? Object.class : (Class<Object>) contentType.getRawClass();
    Class<Object[]> arrayClass = (Class<Object[]>) Array.newInstance(contentClass, 0).getClass();
    return RepeatedWriteSchemas
        .create(protoField, propertyDescriptor, new PropertyWrapperWriters(protoField, elementSchema, arrayClass));
  }
}
