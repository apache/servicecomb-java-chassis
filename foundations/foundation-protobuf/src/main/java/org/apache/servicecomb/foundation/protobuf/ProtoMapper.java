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
package org.apache.servicecomb.foundation.protobuf;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.protobuf.internal.bean.BeanDescriptorManager;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.DeserializerSchemaManager;
import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.SerializerSchemaManager;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.Proto;

public class ProtoMapper {
  private final ObjectMapper jsonMapper;

  private final BeanDescriptorManager beanDescriptorManager;

  private final Proto proto;

  private final SerializerSchemaManager serializerSchemaManager;

  private final DeserializerSchemaManager deserializerSchemaManager;

  // key is message canonical name
  // this allowed developer to control any type deserializer
  // otherwise any type will be deserialized to LinkedHashMap
  private final Map<String, JavaType> anyTypes = new ConcurrentHashMapEx<>();

  protected ProtoMapper(ObjectMapper jsonMapper, BeanDescriptorManager beanDescriptorManager, Proto proto) {
    this.jsonMapper = jsonMapper;
    this.beanDescriptorManager = beanDescriptorManager;
    this.proto = proto;

    serializerSchemaManager = new SerializerSchemaManager(this);
    deserializerSchemaManager = new DeserializerSchemaManager(this);
  }

  public Proto getProto() {
    return proto;
  }

  public ObjectMapper getJsonMapper() {
    return jsonMapper;
  }

  public BeanDescriptorManager getBeanDescriptorManager() {
    return beanDescriptorManager;
  }

  public Map<String, JavaType> getAnyTypes() {
    return anyTypes;
  }

  public void addAnyType(String canonicalName, Type type) {
    anyTypes.put(canonicalName, TypeFactory.defaultInstance().constructType(type));
  }

  public Message getMessageFromCanonicaName(String messageCanonicalName) {
    for (Message message : proto.getMessages()) {
      if (message.getCanonicalName().equals(messageCanonicalName)) {
        return message;
      }
    }

    return null;
  }

  public RootSerializer findRootSerializer(String shortMessageName) {
    return serializerSchemaManager.findRootSerializer(shortMessageName);
  }

  public RootSerializer findRootSerializerByCanonical(String canonicalMessageName) {
    return serializerSchemaManager.findRootSerializerByCanonical(canonicalMessageName);
  }

  public RootDeserializer createRootDeserializer(Type type, String shortMessageName) {
    return createRootDeserializer(TypeFactory.defaultInstance().constructType(type), shortMessageName);
  }

  public RootDeserializer createRootDeserializer(JavaType javaType, String shortMessageName) {
    return deserializerSchemaManager.createRootDeserializer(javaType, shortMessageName);
  }

  public RootDeserializer createRootDeserializer(JavaType javaType, Message message) {
    return deserializerSchemaManager.createRootDeserializer(javaType, message);
  }
}
