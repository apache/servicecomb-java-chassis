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
package io.protostuff.runtime;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.ReflectionUtils;

import io.protostuff.ByteArrayInput;
import io.protostuff.ByteBufferInput;
import io.protostuff.Input;
import io.protostuff.MapSchema;
import io.protostuff.MapSchema.MapWrapper;
import io.protostuff.MapSchemaUtils;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.ProtostuffException;
import io.protostuff.Schema;

public class RuntimeMapFieldProtobuf<T> extends RuntimeMapField<T, Object, Object> {
  private static java.lang.reflect.Field entrySchemaField =
      ReflectionUtils.findField(MapSchema.class, "entrySchema");

  static {
    entrySchemaField.setAccessible(true);
  }

  private RuntimeMapField<T, Object, Object> runtimeMapField;

  private java.lang.reflect.Field field;

  private Schema<Entry<Object, Object>> entrySchema;

  @SuppressWarnings("unchecked")
  public RuntimeMapFieldProtobuf(RuntimeMapField<T, Object, Object> runtimeMapField,
      java.lang.reflect.Field field) {
    super(runtimeMapField.type, runtimeMapField.number, runtimeMapField.name, null,
        runtimeMapField.schema.messageFactory);

    entrySchema = (Schema<Entry<Object, Object>>) ReflectionUtils.getField(entrySchemaField,
        runtimeMapField.schema);

    this.runtimeMapField = runtimeMapField;
    this.field = field;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void mergeFrom(Input input, T message) throws IOException {
    if (!ProtobufFeatureUtils.isUseProtobufMapCodec()) {
      runtimeMapField.mergeFrom(input, message);
      return;
    }

    Map<Object, Object> value = null;
    try {
      value = (Map<Object, Object>) field.get(message);
      if (value == null) {
        value = schema.newMessage();
        field.set(message, value);
      }
    } catch (Exception e) {
      throw new ProtostuffException(
          "Failed to get or set map field " + field.getDeclaringClass().getName() + ":" + field.getName(),
          e);
    }

    MapWrapper<Object, Object> mapWrapper = MapSchemaUtils.createMapWrapper(value);
    if (ByteArrayInput.class.isInstance(input)) {
      ((ByteArrayInput) input).readRawVarint32();
    } else if (ByteBufferInput.class.isInstance(input)) {
      ((ByteBufferInput) input).readRawVarint32();
    } else {
      throw new Error("not handler " + input.getClass().getName());
    }

    int keyNumber = input.readFieldNumber(schema);
    if (keyNumber != 1) {
      throw new ProtostuffException(
          "The map was incorrectly serialized, expect key number 1, but be " + keyNumber);
    }
    Object key = kFrom(input, null);

    int valueNumber = input.readFieldNumber(schema);
    if (valueNumber != 2) {
      throw new ProtostuffException(
          "The map was incorrectly serialized, expect value number 2, but be " + valueNumber);
    }
    vPutFrom(input, mapWrapper, key);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void writeTo(Output output, T message) throws IOException {
    if (!ProtobufFeatureUtils.isUseProtobufMapCodec()) {
      runtimeMapField.writeTo(output, message);
      return;
    }

    final Map<Object, Object> existing;
    try {
      existing = (Map<Object, Object>) field.get(message);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    if (existing != null) {
      for (Entry<Object, Object> entry : existing.entrySet()) {
        output.writeObject(number, entry, entrySchema, true);
      }
    }
  }

  @Override
  protected void transfer(Pipe pipe, Input input, Output output,
      boolean repeated) throws IOException {
    runtimeMapField.transfer(pipe, input, output, repeated);
  }

  @Override
  protected Object kFrom(Input input,
      MapWrapper<Object, Object> wrapper) throws IOException {
    return runtimeMapField.kFrom(input, wrapper);
  }

  @Override
  protected void kTo(Output output, int fieldNumber, Object key,
      boolean repeated) throws IOException {
    runtimeMapField.kTo(output, fieldNumber, key, repeated);
  }

  @Override
  protected void kTransfer(Pipe pipe, Input input, Output output,
      int number, boolean repeated) throws IOException {
    runtimeMapField.kTransfer(pipe, input, output, number, repeated);
  }

  @Override
  protected void vPutFrom(Input input,
      MapWrapper<Object, Object> wrapper, Object key) throws IOException {
    runtimeMapField.vPutFrom(input, wrapper, key);
  }

  @Override
  protected void vTo(Output output, int fieldNumber, Object val,
      boolean repeated) throws IOException {
    runtimeMapField.vTo(output, fieldNumber, val, repeated);
  }

  @Override
  protected void vTransfer(Pipe pipe, Input input, Output output,
      int number, boolean repeated) throws IOException {
    runtimeMapField.vTransfer(pipe, input, output, number, repeated);
  }
}
