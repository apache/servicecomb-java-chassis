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
import java.util.List;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.MessageSchema;

/**
 *
 */
public class PropertyWrapMessageAsFieldSchema extends MessageAsFieldSchema {
  private PropertyWrapMessageSchema propertyWrapMessageSchema;

  private FieldSchema fieldSchema;

  public PropertyWrapMessageAsFieldSchema(Field protoField, Schema<Object> schema) {
    super(protoField, schema);

    List<io.protostuff.runtime.Field<Object>> list = ((MessageSchema) schema).getFields();
    this.fieldSchema = (FieldSchema) list.get(0);

    propertyWrapMessageSchema = new PropertyWrapMessageSchema();
    propertyWrapMessageSchema.setFieldSchema(fieldSchema);
  }

  @Override
  public void writeTo(Output output, Object value) throws IOException {
    output.writeObject(number, value, propertyWrapMessageSchema, false);
  }

  @Override
  public Object readFrom(Input input) throws IOException {
    Object instance = schema.newMessage();
    input.mergeObject(instance, schema);
    return fieldSchema.getter.get(instance);
  }

  @Override
  public void mergeFrom(Input input, Object message) throws IOException {
    throw new UnsupportedOperationException();
  }
}
