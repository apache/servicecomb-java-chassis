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
package org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.InputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class RepeatedReadSchemas {
  public static <T, ELE_TYPE> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor,
      AbstractReaders<ELE_TYPE> readers) {
    JavaType javaType = propertyDescriptor.getJavaType();
    if (readers.arrayClass.isAssignableFrom(javaType.getRawClass())) {
      return new ArrayRepeatedSchema<>(protoField, propertyDescriptor, readers);
    }

    if (Collection.class.isAssignableFrom(javaType.getRawClass()) || javaType.isJavaLangObject()) {
      return new CollectionRepeatedSchema<>(protoField, propertyDescriptor, readers);
    }

    ProtoUtils.throwNotSupportMerge(protoField, javaType);
    return null;
  }

  static class AbstractRepeatedSchema<T, ELE_TYPE> extends FieldSchema<T> {
    protected final AbstractReaders<ELE_TYPE> readers;

    public AbstractRepeatedSchema(Field protoField, PropertyDescriptor propertyDescriptor,
        AbstractReaders<ELE_TYPE> readers) {
      super(protoField, propertyDescriptor.getJavaType());
      this.readers = readers;
    }
  }

  static class CollectionRepeatedSchema<T, ELE_TYPE> extends AbstractRepeatedSchema<T, ELE_TYPE> {
    private final Getter<T, Collection<ELE_TYPE>> getter;

    private final Setter<T, Collection<ELE_TYPE>> setter;

    public CollectionRepeatedSchema(Field protoField, PropertyDescriptor propertyDescriptor,
        AbstractReaders<ELE_TYPE> readers) {
      super(protoField, propertyDescriptor, readers);
      this.getter = propertyDescriptor.getGetter();
      this.setter = propertyDescriptor.getSetter();
    }

    @Override
    public final int mergeFrom(InputEx input, T message) throws IOException {
      Collection<ELE_TYPE> collection = getter.get(message);
      if (collection == null) {
        collection = new ArrayList<>();
        setter.set(message, collection);
      }

      return readers.collectionReader.read(input, collection);
    }
  }

  static class ArrayRepeatedSchema<T, ELE_TYPE> extends AbstractRepeatedSchema<T, ELE_TYPE> {
    private final Getter<T, ELE_TYPE[]> getter;

    private final Setter<T, ELE_TYPE[]> setter;

    public ArrayRepeatedSchema(Field protoField, PropertyDescriptor propertyDescriptor,
        AbstractReaders<ELE_TYPE> readers) {
      super(protoField, propertyDescriptor, readers);
      this.getter = propertyDescriptor.getGetter();
      this.setter = propertyDescriptor.getSetter();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final int mergeFrom(InputEx input, T message) throws IOException {
      ELE_TYPE[] array = getter.get(message);
      Collection<ELE_TYPE> collection = array == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(array));
      int fieldNumber = readers.collectionReader.read(input, collection);

      ELE_TYPE[] newArray = (ELE_TYPE[]) Array.newInstance(readers.arrayClass.getComponentType(), collection.size());
      setter.set(message, collection.toArray(newArray));
      return fieldNumber;
    }
  }
}
