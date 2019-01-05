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
package org.apache.servicecomb.foundation.protobuf.internal.schema.map;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import io.protostuff.InputEx;
import io.protostuff.OutputEx;
import io.protostuff.SchemaEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class MapSchema<T> extends FieldSchema<T> {
  private final Getter<T, Map<Object, Object>> getter;

  private final Setter<T, Map<Object, Object>> setter;

  private final SchemaEx<Entry<Object, Object>> entrySchema;

  public MapSchema(Field protoField, PropertyDescriptor propertyDescriptor,
      SchemaEx<Entry<Object, Object>> entrySchema) {
    super(protoField, propertyDescriptor.getJavaType());
    this.entrySchema = new MapEntrySchema(entrySchema);
    this.getter = propertyDescriptor.getGetter();
    this.setter = propertyDescriptor.getSetter();
  }

  @Override
  public final int mergeFrom(InputEx input, T message) throws IOException {
    Map<Object, Object> map = getter.get(message);
    if (map == null) {
      map = new LinkedHashMap<>();
      setter.set(message, map);
    }

    Entry<Object, Object> entry = new MapEntry<>();
    while (true) {
      input.mergeObject(entry, entrySchema);
      map.put(entry.getKey(), entry.getValue());
      entry.setValue(null);

      int fieldNumber = input.readFieldNumber();
      if (fieldNumber != this.fieldNumber) {
        return fieldNumber;
      }
    }
  }

  @Override
  public final void getAndWriteTo(OutputEx output, T message) throws IOException {
    Map<Object, Object> map = getter.get(message);
    if (map == null) {
      return;
    }

    writeMap(output, map);
  }

  @SuppressWarnings("unchecked")
  @Override
  public final void writeTo(OutputEx output, Object value) throws IOException {
    writeMap(output, (Map<Object, Object>) value);
  }

  private void writeMap(OutputEx output, Map<Object, Object> map) throws IOException {
    for (Entry<Object, Object> entry : map.entrySet()) {
      if (entry.getKey() == null || entry.getValue() == null) {
        continue;
      }

      output.writeObject(tag, tagSize, entry, entrySchema);
    }
  }
}
