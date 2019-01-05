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
import java.util.Collection;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class RepeatedWriteSchemas {
  public static <T, ELE_TYPE> FieldSchema<T> create(Field protoField,
      PropertyDescriptor propertyDescriptor,
      AbstractWriters<ELE_TYPE> writers) {
    JavaType javaType = propertyDescriptor.getJavaType();
    if (writers.arrayClass.isAssignableFrom(javaType.getRawClass())) {
      return new ArraySchema<>(protoField, propertyDescriptor, writers);
    }

    if (Collection.class.isAssignableFrom(javaType.getRawClass())) {
      return new CollectionSchema<>(protoField, propertyDescriptor, writers);
    }

    return new DynamicSchema<>(protoField, propertyDescriptor, writers);
  }

  static class DynamicSchema<T, ELE_TYPE> extends FieldSchema<T> {
    protected final AbstractWriters<ELE_TYPE> writers;

    @SuppressWarnings("unchecked")
    public DynamicSchema(Field protoField, PropertyDescriptor propertyDescriptor,
        AbstractWriters<ELE_TYPE> writers) {
      super(protoField, propertyDescriptor.getJavaType());

      this.writers = writers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void writeTo(OutputEx output, Object value) throws IOException {
      writers.dynamicWriteTo(output, value);
    }
  }

  private static class ArraySchema<T, ELE_TYPE> extends DynamicSchema<T, ELE_TYPE> {
    private final Getter<T, ELE_TYPE[]> getter;

    public ArraySchema(Field protoField, PropertyDescriptor propertyDescriptor,
        AbstractWriters<ELE_TYPE> writers) {
      super(protoField, propertyDescriptor, writers);
      this.getter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      ELE_TYPE[] value = getter.get(message);
      if (value == null) {
        return;
      }

      writers.arrayWriter.writeTo(output, value);
    }
  }

  private static class CollectionSchema<T, ELE_TYPE> extends DynamicSchema<T, ELE_TYPE> {
    private final Getter<T, Collection<ELE_TYPE>> getter;

    public CollectionSchema(Field protoField, PropertyDescriptor propertyDescriptor,
        AbstractWriters<ELE_TYPE> writers) {
      super(protoField, propertyDescriptor, writers);
      this.getter = propertyDescriptor.getGetter();
    }

    @Override
    public final void getAndWriteTo(OutputEx output, T message) throws IOException {
      Collection<ELE_TYPE> value = getter.get(message);
      if (value == null) {
        return;
      }

      writers.collectionWriter.writeTo(output, value);
    }
  }
}
