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

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;
import io.protostuff.compiler.model.Field;

public class MessageAsFieldSchema extends FieldSchema {
  private Schema<Object> schema;

  public MessageAsFieldSchema(Field protoField, Schema<Object> schema) {
    super(protoField);
    this.schema = schema;
  }

  @Override
  public void writeTo(Output output, Object value) throws IOException {
    output.writeObject(number, value, schema, false);
  }

  @Override
  public Object readFrom(Input input) throws IOException {
    return input.mergeObject(null, schema);
  }

  @Override
  public void mergeFrom(Input input, Object message) throws IOException {
    Object existing = getter.get(message);
    Object fieldValue = input.mergeObject(existing, schema);
    setter.set(message, fieldValue);
  }
}
