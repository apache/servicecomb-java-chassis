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
package org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated;

import java.io.IOException;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.RepeatedWriteSchemas.DynamicSchema;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class RepeatedPrimitiveWriteSchemas {
  public static <T, PRIMITIVE_ARRAY, PRIMITIVE_WRAPPER> FieldSchema<T> create(Field protoField,
      PropertyDescriptor propertyDescriptor,
      AbstractPrimitiveWriters<PRIMITIVE_ARRAY, PRIMITIVE_WRAPPER> writers) {
    JavaType javaType = propertyDescriptor.getJavaType();
    if (writers.primitiveArrayClass == javaType.getRawClass()) {
      return new PrimitiveArraySchema<>(protoField, propertyDescriptor, writers);
    }

    return RepeatedWriteSchemas.create(protoField, propertyDescriptor, writers);
  }

  private static class PrimitiveArraySchema<T, PRIMITIVE_ARRAY, PRIMITIVE_WRAPPER> extends
      DynamicSchema<T, PRIMITIVE_WRAPPER> {
    private final Getter<T, PRIMITIVE_ARRAY> getter;

    private final AbstractPrimitiveWriters<PRIMITIVE_ARRAY, PRIMITIVE_WRAPPER> primitiveWriters;

    public PrimitiveArraySchema(Field protoField, PropertyDescriptor propertyDescriptor,
        AbstractPrimitiveWriters<PRIMITIVE_ARRAY, PRIMITIVE_WRAPPER> writers) {
      super(protoField, propertyDescriptor, writers);
      this.getter = propertyDescriptor.getGetter();
      this.primitiveWriters = writers;
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      PRIMITIVE_ARRAY value = getter.get(message);
      if (value == null) {
        return;
      }

      primitiveWriters.primitiveArrayWriter.writeTo(output, value);
    }
  }
}
