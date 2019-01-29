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

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;

import io.protostuff.InputEx;
import io.protostuff.OutputEx;
import io.protostuff.SchemaEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class MessageAsFieldSchema<T> extends FieldSchema<T> {
  protected final SchemaEx<Object> schema;

  protected final Getter<T, Object> getter;

  protected final Setter<T, Object> setter;

  public MessageAsFieldSchema(Field protoField, PropertyDescriptor propertyDescriptor, SchemaEx<Object> schema) {
    super(protoField, propertyDescriptor.getJavaType());
    this.schema = schema;
    this.getter = propertyDescriptor.getGetter();
    this.setter = propertyDescriptor.getSetter();
  }

  @Override
  public final void getAndWriteTo(OutputEx output, T message) throws IOException {
    Object value = getter.get(message);
    if (value == null) {
      return;
    }

    output.writeObject(tag, tagSize, value, schema);
  }

  @Override
  public final void writeTo(OutputEx output, Object value) throws IOException {
    output.writeObject(tag, tagSize, value, schema);
  }

  @Override
  public final int mergeFrom(InputEx input, T message) throws IOException {
    Object value = getter.get(message);
    if (value == null) {
      value = schema.newMessage();
      setter.set(message, value);
    }
    input.mergeObject(value, schema);
    return input.readFieldNumber();
  }
}
