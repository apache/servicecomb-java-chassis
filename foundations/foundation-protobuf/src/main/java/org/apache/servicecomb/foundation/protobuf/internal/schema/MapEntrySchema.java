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

import io.protostuff.CustomSchema;
import io.protostuff.Input;
import io.protostuff.Output;

public class MapEntrySchema extends CustomSchema<Object> {
  private final FieldSchema keySchema;

  private final FieldSchema valueSchema;

  public MapEntrySchema(FieldSchema keySchema, FieldSchema valueSchema) {
    super(null);
    this.keySchema = keySchema;
    this.valueSchema = valueSchema;
  }

  @Override
  public boolean isInitialized(Object message) {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void mergeFrom(Input input, Object message) throws IOException {
    input.readFieldNumber(null);
    Object key = keySchema.readFrom(input);

    input.readFieldNumber(null);
    Object value = valueSchema.readFrom(input);

    input.readFieldNumber(null);

    ((Map<Object, Object>) message).put(key, value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void writeTo(Output output, Object message) throws IOException {
    keySchema.writeTo(output, ((Entry<Object, Object>) message).getKey());
    valueSchema.writeTo(output, ((Entry<Object, Object>) message).getValue());
  }
}
