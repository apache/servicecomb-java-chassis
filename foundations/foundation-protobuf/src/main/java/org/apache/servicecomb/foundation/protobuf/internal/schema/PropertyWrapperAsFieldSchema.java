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
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyWrapper;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.MessageReadSchema;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.MessageWriteSchema;

import io.protostuff.InputEx;
import io.protostuff.OutputEx;
import io.protostuff.SchemaEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

/**
 *
 */
public class PropertyWrapperAsFieldSchema<T> extends FieldSchema<T> {
  private final SchemaEx<Object> schema;

  private final FieldSchema<Object> fieldSchema;

  protected final Getter<T, Object> getter;

  protected final Setter<T, Object> setter;

  public PropertyWrapperAsFieldSchema(Field protoField, PropertyDescriptor propertyDescriptor,
      SchemaEx<Object> schema) {
    super(protoField, propertyDescriptor.getJavaType());
    this.schema = schema;
    this.getter = propertyDescriptor.getGetter();
    this.setter = propertyDescriptor.getSetter();

    if (schema instanceof MessageWriteSchema) {
      fieldSchema = ((MessageWriteSchema<Object>) schema).getMainPojoFieldMaps().getFieldByNumber(1);
      return;
    }

    fieldSchema = ((MessageReadSchema<Object>) schema).getFieldMap().getFieldByNumber(1);
  }

  @Override
  public void getAndWriteTo(OutputEx output, T message) throws IOException {
    output.writeObject(tag, tagSize, message, fieldSchema::writeTo);
  }

  @Override
  public void writeTo(OutputEx output, Object value) throws IOException {
    output.writeObject(tag, tagSize, value, fieldSchema::writeTo);
  }

  @Override
  public int mergeFrom(InputEx input, T message) throws IOException {
    PropertyWrapper<Object> wrapper = new PropertyWrapper<>();
    input.mergeObject(wrapper, schema);
    setter.set(message, wrapper.getValue());
    return input.readFieldNumber();
  }
}
