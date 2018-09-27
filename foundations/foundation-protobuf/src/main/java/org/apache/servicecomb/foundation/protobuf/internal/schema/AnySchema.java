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

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Message;

public class AnySchema extends FieldSchema {
  private final ProtoMapper protoMapper;

  private final AnyEntrySchema anyEntrySchema;

  // key is message canonical name
  private final Map<String, RootDeserializer> rootDeserializers = new ConcurrentHashMapEx<>();

  public AnySchema(ProtoMapper protoMapper, Field protoField) {
    super(protoField);
    this.protoMapper = protoMapper;
    this.anyEntrySchema = new AnyEntrySchema(protoMapper);
  }

  @Override
  public Object readFrom(Input input) throws IOException {
    AnyEntry anyEntry = (AnyEntry) input.mergeObject(null, anyEntrySchema);
    if (anyEntry.getTypeUrl().startsWith(ProtoConst.PACK_SCHEMA)) {
      return standardUnpack(anyEntry.getTypeUrl(), anyEntry.getValue());
    }

    return jsonExtendMergeFrom(anyEntry.getTypeUrl(), anyEntry.getValue());
  }

  @Override
  public void mergeFrom(Input input, Object message) throws IOException {
    Object anyValue = readFrom(input);
    setter.set(message, anyValue);
  }

  @SuppressWarnings("unchecked")
  protected Object standardUnpack(String typeUrl, byte[] bytes) throws IOException {
    String msgCanonicalName = typeUrl.substring(ProtoConst.PACK_SCHEMA.length());
    RootDeserializer valueDeserializer = rootDeserializers
        .computeIfAbsent(msgCanonicalName, this::createRootDeserializerFromCanonicaName);
    Object value = valueDeserializer.deserialize(bytes);
    if (value instanceof Map) {
      ((Map<String, Object>) value).put(ProtoConst.JSON_ID_NAME, valueDeserializer.getSchema().messageName());
    }
    return value;
  }

  protected RootDeserializer createRootDeserializerFromCanonicaName(String msgCanonicalName) {
    Message message = protoMapper.getMessageFromCanonicaName(msgCanonicalName);
    if (message == null) {
      throw new IllegalStateException(
          "can not find proto message to create deserializer, name=" + msgCanonicalName);
    }

    JavaType javaType = protoMapper.getAnyTypes().get(msgCanonicalName);
    if (javaType == null) {
      javaType = ProtoConst.MAP_TYPE;
    }
    return protoMapper.createRootDeserializer(javaType, message);
  }

  protected Object jsonExtendMergeFrom(String typeUrl, byte[] bytes) throws IOException {
    return protoMapper.getJsonMapper().readValue(bytes, Object.class);
  }

  @Override
  public void writeTo(Output output, Object value) throws IOException {
    if (value == null) {
      return;
    }

    output.writeObject(number, value, anyEntrySchema, repeated);
  }
}
