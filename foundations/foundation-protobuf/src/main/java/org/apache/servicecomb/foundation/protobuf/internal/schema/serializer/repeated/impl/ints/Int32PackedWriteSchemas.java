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
package org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.ints;

import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.AbstractPrimitiveWriters;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.RepeatedPrimitiveWriteSchemas;

import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class Int32PackedWriteSchemas {
  private static class Int32PackedWriters extends AbstractPrimitiveWriters<int[], Integer> {
    public Int32PackedWriters(Field protoField) {
      super(protoField);

      primitiveArrayWriter = (o, value) ->
          o.writeObject(tag, tagSize, value, (output, array) -> {
            for (int element : array) {
              output.writePackedInt32(element);
            }
          });

      arrayWriter = (o, value) ->
          o.writeObject(tag, tagSize, value, (output, array) -> {
            for (Integer element : array) {
              if (element != null) {
                output.writePackedInt32(element);
                continue;
              }

              ProtoUtils.throwNotSupportNullElement(protoField);
            }
          });

      collectionWriter = (o, value) ->
          o.writeObject(tag, tagSize, value, (output, collection) -> {
            for (Integer element : collection) {
              if (element != null) {
                output.writePackedInt32(element);
                continue;
              }

              ProtoUtils.throwNotSupportNullElement(protoField);
            }
          });

      stringArrayWriter = (o, value) ->
          o.writeObject(tag, tagSize, value, (output, array) -> {
            for (String element : array) {
              if (element != null) {
                int parsedValue = Integer.parseInt(element, 10);
                output.writePackedInt32(parsedValue);
                continue;
              }

              ProtoUtils.throwNotSupportNullElement(protoField);
            }
          });
    }
  }

  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    return RepeatedPrimitiveWriteSchemas.create(protoField, propertyDescriptor, new Int32PackedWriters(protoField));
  }
}
