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
package org.apache.servicecomb.foundation.protobuf.internal.schema.any;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyWrapper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.protostuff.InputEx;
import io.protostuff.OutputEx;
import io.protostuff.SchemaEx;
import io.protostuff.SchemaWriter;
import io.protostuff.WireFormat;
import io.protostuff.compiler.model.Message;

public class AnyEntrySchema implements SchemaEx<Object> {
  private final ProtoMapper protoMapper;

  // key is message short name
  private final Map<String, SchemaWriter<Object>> anyEntrySserializers = new ConcurrentHashMapEx<>();

  // key is message canonical name
  private final Map<String, RootDeserializer<Object>> rootDeserializers = new ConcurrentHashMapEx<>();

  private final int keyTag = WireFormat.makeTag(1, WireFormat.WIRETYPE_LENGTH_DELIMITED);

  private final int valueTag = WireFormat.makeTag(2, WireFormat.WIRETYPE_LENGTH_DELIMITED);

  private Type anyTargetType;

  public AnyEntrySchema(ProtoMapper protoMapper, Type type) {
    this.protoMapper = protoMapper;
    this.anyTargetType = type;
  }

  @Override
  public void init() {

  }

  @Override
  public Object newMessage() {
    return new PropertyWrapper<>();
  }

  @Override
  public void mergeFrom(InputEx input, Object message) throws IOException {
    input.readFieldNumber();
    String typeUrl = input.readString();

    input.readFieldNumber();
    byte[] bytes = input.readByteArray();

    input.readFieldNumber();

    if (message instanceof PropertyWrapper) {
      if (typeUrl.startsWith(ProtoConst.PACK_SCHEMA)) {
        ((PropertyWrapper) message).setValue(standardUnpack(typeUrl, bytes));
      } else {
        ((PropertyWrapper) message).setValue(jsonExtendMergeFrom(typeUrl, bytes));
      }
    } else if (message instanceof AnyEntry) {
      ((AnyEntry) message).setTypeUrl(typeUrl);
      ((AnyEntry) message).setValue(bytes);
    }
  }

  public Object deserialize(InputEx input) throws IOException {
    AnyEntry anyEntry = new AnyEntry();
    input.mergeObject(anyEntry, this);

    if (anyEntry.getTypeUrl().startsWith(ProtoConst.PACK_SCHEMA)) {
      return standardUnpack(anyEntry.getTypeUrl(), anyEntry.getValue());
    }

    return jsonExtendMergeFrom(anyEntry.getTypeUrl(), anyEntry.getValue());
  }

  @SuppressWarnings("unchecked")
  protected Object standardUnpack(String typeUrl, byte[] bytes) throws IOException {
    String msgCanonicalName = typeUrl.substring(ProtoConst.PACK_SCHEMA.length());
    RootDeserializer<Object> valueDeserializer = rootDeserializers
        .computeIfAbsent(msgCanonicalName, this::createRootDeserializerFromCanonicaName);
    Object value = valueDeserializer.deserialize(bytes);
    if (value instanceof Map) {
      ((Map<String, Object>) value).put(ProtoConst.JSON_ID_NAME, valueDeserializer.getSchema().messageName());
    }
    return value;
  }

  protected RootDeserializer<Object> createRootDeserializerFromCanonicaName(String msgCanonicalName) {
    Message message = protoMapper.getMessageFromCanonicaName(msgCanonicalName);
    if (message == null) {
      throw new IllegalStateException(
          "can not find proto message to create deserializer, name=" + msgCanonicalName);
    }

    JavaType javaType = protoMapper.getAnyTypes()
        .getOrDefault(msgCanonicalName, constructRuntimeType(ProtoConst.MAP_TYPE));
    return protoMapper.createRootDeserializer(message, javaType);
  }

  protected Object jsonExtendMergeFrom(String typeUrl, byte[] bytes) throws IOException {
    try {
      return protoMapper.getJsonMapper()
          .readValue(bytes, Class.forName(typeUrl.substring(ProtoConst.JSON_SCHEMA.length())));
    } catch (ClassNotFoundException e) {
      return protoMapper.getJsonMapper()
          .readValue(bytes, constructRuntimeType(ProtoConst.OBJECT_TYPE));
    }
  }

  private JavaType constructRuntimeType(JavaType defaultType) {
    if (this.anyTargetType == null) {
      return defaultType;
    } else {
      return TypeFactory.defaultInstance().constructType(anyTargetType);
    }
  }

  protected String getInputActualTypeName(Object input) {
    if (!(input instanceof Map)) {
      return input.getClass().getSimpleName();
    }

    // @JsonTypeInfo(use = Id.NAME)
    Object actualTypeName = ((Map<?, ?>) input).get(ProtoConst.JSON_ID_NAME);
    if (actualTypeName instanceof String) {
      return (String) actualTypeName;
    }

    return null;
  }

  /**
   * <pre>
   * if message is type of CustomGeneric&lt;User&gt;
   * we can not get any information of "User" from message.getClass()
   *
   * when use with ServiceComb
   * proto definition convert from swagger, the proto type will be "CustomGenericUser"
   * is not match to "CustomGeneric"
   * so message will be serialized with json schema
   * </pre>
   * @param output
   * @param value
   * @throws IOException
   */
  @Override
  public void writeTo(OutputEx output, Object value) throws IOException {
    String actualTypeName = getInputActualTypeName(value);
    SchemaWriter<Object> entryWriter = actualTypeName == null ? this::jsonExtend : anyEntrySserializers
        .computeIfAbsent(actualTypeName, n -> createEntryWriter(n, value));
    entryWriter.writeTo(output, value);
  }

  private SchemaWriter<Object> createEntryWriter(String actualTypeName, Object _value) {
    Message message = protoMapper.getProto().getMessage(actualTypeName);
    if (message == null) {
      // not standard, protobuf can not support or not define this type , just extend
      return this::jsonExtend;
    }

    // standard pack
    RootSerializer valueSerializer = protoMapper.createRootSerializer(message, _value.getClass());
    String valueCanonicalName = message.getCanonicalName();
    return (output, value) -> {
      standardPack(output, value, valueCanonicalName, valueSerializer);
    };
  }

  protected void standardPack(OutputEx output, Object message, String canonicalName, RootSerializer valueSerializer)
      throws IOException {
    output.writeString(keyTag, 1, ProtoConst.PACK_SCHEMA + canonicalName);

    byte[] bytes = valueSerializer.serialize(message);
    output.writeByteArray(valueTag, 1, bytes);
  }

  protected void jsonExtend(OutputEx output, Object input) throws IOException {
    output.writeString(keyTag, 1, ProtoConst.JSON_SCHEMA + input.getClass().getName());

    byte[] bytes = protoMapper.getJsonMapper().writeValueAsBytes(input);
    output.writeByteArray(valueTag, 1, bytes);
  }
}
