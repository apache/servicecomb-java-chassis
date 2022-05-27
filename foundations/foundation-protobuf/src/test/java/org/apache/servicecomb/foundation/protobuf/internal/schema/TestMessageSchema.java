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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.internal.TestSchemaBase;
import org.apache.servicecomb.foundation.protobuf.internal.model.CustomGeneric;
import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot;
import org.apache.servicecomb.foundation.protobuf.internal.model.User;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMessageSchema extends TestSchemaBase {
  @Test
  public void empty() throws Throwable {
    check();

    Assertions.assertArrayEquals(protobufBytes, rootSerializer.serialize(null));
  }

  @Test
  public void generic() throws Throwable {
    JavaType javaType = TypeFactory.defaultInstance().constructParametricType(CustomGeneric.class, User.class);
    RootDeserializer<CustomGeneric<User>> genericDeserializer = protoMapper.createRootDeserializer("Root", javaType);

    builder.setUser(ProtobufRoot.User.newBuilder().setName("name1").build());
    check(genericDeserializer, mapRootDeserializer, rootSerializer, false);

    @SuppressWarnings("unchecked")
    CustomGeneric<User> generic = (CustomGeneric<User>) scbRoot;
    Assertions.assertNotNull(generic.user);
  }

  @Test
  public void normal() throws Throwable {
    builder.setString("abc");
    builder.setInt64(1L);
    builder.setUser(ProtobufRoot.User.newBuilder().setName("name").build());

    check();

    // map
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("int64", 1);
    map.put("string", "abc");

    Map<String, Object> userMap = new LinkedHashMap<>();
    userMap.put("name", "name");
    map.put("user", userMap);

    map.put("notExist", null);

    Assertions.assertArrayEquals(protobufBytes, rootSerializer.serialize(map));
  }
}
