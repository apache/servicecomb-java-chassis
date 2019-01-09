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
package org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.impl;

import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyWrapper;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.AbstractReaders;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.repeated.RepeatedReadSchemas;

import io.protostuff.SchemaReader;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class PropertyWrapperRepeatedReadSchemas {
  private static class PropertyWrapperReaders extends AbstractReaders<Object> {
    public PropertyWrapperReaders(Field protoField, SchemaReader<Object> elementSchema) {
      super(protoField);

      collectionReader = (input, collection) -> {
        while (true) {
          PropertyWrapper<Object> wrapper = new PropertyWrapper<>();
          input.mergeObject(wrapper, elementSchema);
          collection.add(wrapper.getValue());

          int fieldNumber = input.readFieldNumber();
          if (fieldNumber != this.fieldNumber) {
            return fieldNumber;
          }
        }
      };
    }
  }

  public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor,
      SchemaReader<Object> elementSchema) {
    return RepeatedReadSchemas
        .create(protoField, propertyDescriptor, new PropertyWrapperReaders(protoField, elementSchema));
  }
}
