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
package org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.impl;

import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.AbstractPrimitiveReaders;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl.longs.LongRepeatedReadSchemas;

import com.fasterxml.jackson.databind.util.PrimitiveArrayBuilder;

import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class Int64PackedReadSchemas {
  private static class Int64PackedReaders extends AbstractPrimitiveReaders<long[], Long> {
    public Int64PackedReaders(Field protoField) {
      super(protoField);

      this.primitiveArrayReader = (input, builderWrapper) -> {
        PrimitiveArrayBuilder<long[]> builder = builderWrapper.getBuilder();
        long[] chunk = builder.resetAndStart();
        int ix = 0;

        while (true) {
          if (ix >= chunk.length) {
            chunk = builder.appendCompletedChunk(chunk, ix);
            ix = 0;
          }
          chunk[ix++] = input.readPackedInt64();

          int fieldNumber = input.readFieldNumber();
          if (fieldNumber != this.fieldNumber) {
            builderWrapper.setArray(builder.completeAndClearBuffer(chunk, ix));
            return fieldNumber;
          }
        }
      };

      this.collectionReader = (input, collection) -> {
        while (true) {
          collection.add(input.readPackedInt64());

          int fieldNumber = input.readFieldNumber();
          if (fieldNumber != this.fieldNumber) {
            return fieldNumber;
          }
        }
      };
    }
  }

  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    return LongRepeatedReadSchemas.create(protoField, propertyDescriptor, new Int64PackedReaders(protoField));
  }
}
