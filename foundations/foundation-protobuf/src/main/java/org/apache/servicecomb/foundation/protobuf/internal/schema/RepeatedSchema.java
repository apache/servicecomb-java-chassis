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
package org.apache.servicecomb.foundation.protobuf.internal.schema;

import java.io.IOException;
import java.util.Collection;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.compiler.model.Field;

public class RepeatedSchema extends FieldSchema {
  private final FieldSchema elementSchema;

  public RepeatedSchema(Field protoField, FieldSchema elementSchema) {
    super(protoField);
    this.elementSchema = elementSchema;
  }

  @Override
  public void writeTo(Output output, Object value) throws IOException {
    if (value == null) {
      return;
    }

    if (value instanceof Collection) {
      @SuppressWarnings("unchecked")
      Collection<Object> list = (Collection<Object>) value;
      for (Object element : list) {
        elementSchema.writeTo(output, element);
      }
      return;
    }

    if (value.getClass().isArray()) {
      for (Object element : (Object[]) value) {
        elementSchema.writeTo(output, element);
      }
      return;
    }

    throwNotSupportValue(value);
  }

  @Override
  public Object readFrom(Input input) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void mergeFrom(Input input, Object message) throws IOException {
    Collection<Object> collection = getOrCreateFieldValue(message);
    collection.add(elementSchema.readFrom(input));
  }
}
