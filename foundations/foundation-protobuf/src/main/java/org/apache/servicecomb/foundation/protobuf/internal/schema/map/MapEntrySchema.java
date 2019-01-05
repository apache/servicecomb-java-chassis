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
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.MessageReadSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.MessageWriteSchema;

import io.protostuff.InputEx;
import io.protostuff.OutputEx;
import io.protostuff.SchemaEx;
import io.protostuff.runtime.FieldSchema;

public class MapEntrySchema implements SchemaEx<Entry<Object, Object>> {
  private final FieldSchema<Entry<Object, Object>> keySchema;

  private final FieldSchema<Entry<Object, Object>> valueSchema;

  public MapEntrySchema(SchemaEx<Entry<Object, Object>> entrySchema) {
    if (entrySchema instanceof MessageWriteSchema) {
      keySchema = ((MessageWriteSchema<Entry<Object, Object>>) entrySchema).getMainPojoFieldMaps().getFieldByNumber(1);
      valueSchema = ((MessageWriteSchema<Entry<Object, Object>>) entrySchema).getMainPojoFieldMaps()
          .getFieldByNumber(2);
      return;
    }

    keySchema = ((MessageReadSchema<Entry<Object, Object>>) entrySchema).getFieldMap().getFieldByNumber(1);
    valueSchema = ((MessageReadSchema<Entry<Object, Object>>) entrySchema).getFieldMap().getFieldByNumber(2);
  }

  @Override
  public void init() {

  }

  @Override
  public void mergeFrom(InputEx input, Entry<Object, Object> message) throws IOException {
    input.readFieldNumber();
    keySchema.mergeFrom(input, message);
    valueSchema.mergeFrom(input, message);
  }

  @Override
  public void writeTo(OutputEx output, Entry<Object, Object> value) throws IOException {
    keySchema.writeTo(output, value.getKey());
    valueSchema.writeTo(output, value.getValue());
  }
}
