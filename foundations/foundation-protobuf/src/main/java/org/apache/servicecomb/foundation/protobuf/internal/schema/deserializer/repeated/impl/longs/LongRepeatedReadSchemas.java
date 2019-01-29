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
package org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs;

import java.io.IOException;
import java.util.Arrays;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.AbstractPrimitiveReaders;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.PrimitiveArrayBuilderWrapper;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.RepeatedReadSchemas;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.InputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class LongRepeatedReadSchemas {
  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor,
      AbstractPrimitiveReaders<long[], Long> readers) {
    JavaType javaType = propertyDescriptor.getJavaType();
    if (long[].class.equals(javaType.getRawClass())) {
      return new LongPrimitiveArraySchema<>(protoField, propertyDescriptor, readers);
    }

    return RepeatedReadSchemas.create(protoField, propertyDescriptor, readers);
  }

  static class LongPrimitiveArraySchema<T> extends FieldSchema<T> {
    private final Getter<T, long[]> getter;

    private final Setter<T, long[]> setter;

    private final AbstractPrimitiveReaders<long[], Long> readers;

    public LongPrimitiveArraySchema(Field protoField, PropertyDescriptor propertyDescriptor,
        AbstractPrimitiveReaders<long[], Long> readers) {
      super(protoField, propertyDescriptor.getJavaType());
      this.getter = propertyDescriptor.getGetter();
      this.setter = propertyDescriptor.getSetter();
      this.readers = readers;
    }

    @Override
    public int mergeFrom(InputEx input, T message) throws IOException {
      PrimitiveArrayBuilderWrapper<long[]> builderWrapper = new PrimitiveArrayBuilderWrapper<>(
          input.getArrayBuilders().getLongBuilder());

      int fieldNumber = readers.primitiveArrayReader.read(input, builderWrapper);
      long[] newValue = builderWrapper.getArray();
      newValue = mergeArray(getter.get(message), newValue);
      setter.set(message, newValue);

      return fieldNumber;
    }

    public long[] mergeArray(long[] oldValue, long[] newValue) {
      if (oldValue == null || oldValue.length == 0) {
        return newValue;
      }

      return concatArray(oldValue, newValue);
    }

    private long[] concatArray(long[] oldValue, long[] newValue) {
      int len1 = oldValue.length;
      int len2 = newValue.length;
      long[] result = Arrays.copyOf(oldValue, len1 + len2);
      System.arraycopy(newValue, 0, result, len1, len2);
      return result;
    }
  }
}
