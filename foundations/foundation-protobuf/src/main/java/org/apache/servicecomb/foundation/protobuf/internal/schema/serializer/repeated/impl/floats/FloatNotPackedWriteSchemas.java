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
package org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.impl.floats;

import java.util.Collection;

import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.AbstractPrimitiveWriters;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.repeated.RepeatedPrimitiveWriteSchemas;

import io.protostuff.OutputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class FloatNotPackedWriteSchemas {
  private static class FloatNotPackedWriters extends AbstractPrimitiveWriters<float[], Float> {
    public FloatNotPackedWriters(Field protoField) {
      super(protoField);

      primitiveArrayWriter = (OutputEx output, float[] array) -> {
        for (float element : array) {
          output.writeFloat(tag, tagSize, element);
        }
      };

      arrayWriter = (OutputEx output, Float[] array) -> {
        for (Float element : array) {
          if (element != null) {
            output.writeFloat(tag, tagSize, element);
            continue;
          }

          ProtoUtils.throwNotSupportNullElement(protoField);
        }
      };

      collectionWriter = (OutputEx output, Collection<Float> collection) -> {
        for (Float element : collection) {
          if (element != null) {
            output.writeFloat(tag, tagSize, element);
            continue;
          }

          ProtoUtils.throwNotSupportNullElement(protoField);
        }
      };

      stringArrayWriter = (OutputEx output, String[] array) -> {
        for (String element : array) {
          if (element != null) {
            float parsedValue = Float.parseFloat(element);
            output.writeFloat(tag, tagSize, parsedValue);
            continue;
          }

          ProtoUtils.throwNotSupportNullElement(protoField);
        }
      };
    }
  }

  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
    return RepeatedPrimitiveWriteSchemas.create(protoField, propertyDescriptor, new FloatNotPackedWriters(protoField));
  }
}
