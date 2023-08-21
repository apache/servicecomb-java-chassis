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
package org.apache.servicecomb.codec.protobuf.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.codec.protobuf.utils.ScopedProtobufSchemaManager;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyWrapper;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;

public class TestSchemaCodec {
  ScopedProtobufSchemaManager manager = new ScopedProtobufSchemaManager();

  @Test
  public void test_string_schema_codec() throws Exception {
    OpenAPI openAPI = new OpenAPI();
    StringSchema schema = new StringSchema();
    ProtoMapper protoMapper = manager.getOrCreateProtoMapper(openAPI, "test", "input", schema);
    RootSerializer serializer = protoMapper.getSerializerSchemaManager()
        .createRootSerializer(protoMapper.getProto().getMessage("input"),
            String.class);
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("value", "abcdefg");
    byte[] result = serializer.serialize(arguments);
    RootDeserializer<PropertyWrapper<String>> deserializer = protoMapper.getDeserializerSchemaManager()
        .createRootDeserializer(protoMapper.getProto().getMessage("input"), String.class);
    PropertyWrapper<String> deserializedResult = deserializer.deserialize(result);
    assertEquals("abcdefg", deserializedResult.getValue());
  }

  public static class User {
    public String name;
  }

  @Test
  public void test_object_schema_codec() throws Exception {
    OpenAPI openAPI = new OpenAPI();

    ObjectSchema schema = new ObjectSchema();
    schema.setName("User");
    schema.addProperty("name", new StringSchema());
    openAPI.setComponents(new Components());
    openAPI.getComponents().addSchemas("User", schema);

    ObjectSchema ref = new ObjectSchema();
    ref.set$ref(Components.COMPONENTS_SCHEMAS_REF + "User");

    ProtoMapper protoMapper = manager.getOrCreateProtoMapper(openAPI, "test", "input", ref);
    RootSerializer serializer = protoMapper.getSerializerSchemaManager()
        .createRootSerializer(protoMapper.getProto().getMessage("input"),
            User.class);
    Map<String, Object> arguments = new HashMap<>();
    User user = new User();
    user.name = "abcdefg";
    arguments.put("value", user);
    byte[] result = serializer.serialize(arguments);
    RootDeserializer<PropertyWrapper<User>> deserializer = protoMapper.getDeserializerSchemaManager()
        .createRootDeserializer(protoMapper.getProto().getMessage("input"), User.class);
    PropertyWrapper<User> deserializedResult = deserializer.deserialize(result);
    assertEquals("abcdefg", deserializedResult.getValue().name);
  }
}
