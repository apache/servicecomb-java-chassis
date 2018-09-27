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
import java.util.Map;
import java.util.Map.Entry;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.compiler.model.Field;

public class MapSchema extends FieldSchema {
  private final MapEntrySchema entrySchema;

  public MapSchema(Field protoField, FieldSchema keySchema, FieldSchema valueSchema) {
    super(protoField);
    this.entrySchema = new MapEntrySchema(keySchema, valueSchema);
  }

  @Override
  public Object readFrom(Input input) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void mergeFrom(Input input, Object message) throws IOException {
    Map<Object, Object> map = getOrCreateFieldValue(message);
    input.mergeObject(map, entrySchema);
  }

  @Override
  public void writeTo(Output output, Object value) throws IOException {
    if (value == null) {
      return;
    }

    @SuppressWarnings("unchecked")
    Map<Object, Object> map = (Map<Object, Object>) value;
    for (Entry<Object, Object> entry : map.entrySet()) {
      if (entry.getKey() == null || entry.getValue() == null) {
        continue;
      }

      output.writeObject(number, entry, entrySchema, true);
    }
  }
}
