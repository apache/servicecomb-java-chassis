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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.apache.servicecomb.foundation.protobuf.internal.TestSchemaBase;
import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot;
import org.apache.servicecomb.foundation.protobuf.internal.model.Root;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import com.google.protobuf.Any;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAnySchema extends TestSchemaBase {
  @Test
  public void empty() throws Throwable {
    scbMap = new HashMap<>();
    scbMap.put("any", null);
    Assertions.assertEquals(0, rootSerializer.serialize(scbMap).length);
  }

  @Test
  public void anys_pack() throws IOException {
    builder
        .addAnys(Any.pack(ProtobufRoot.User.newBuilder().setName("n1").build()))
        .addAnys(Any.pack(ProtobufRoot.User.newBuilder().setName("n2").build()));
    check();
  }

  @Test
  public void anys_json() throws IOException {
    Root root = new Root();
    root.setAnys(Arrays.asList("abc", "123"));

    scbRootBytes = rootSerializer.serialize(root);
    root = rootDeserializer.deserialize(scbRootBytes);
    MatcherAssert.assertThat(root.getAnys(), Matchers.contains("abc", "123"));
  }

  @Test
  public void pack() throws Throwable {
    builder.setAny(Any.pack(ProtobufRoot.User.newBuilder().setName("n1").build()));
    check();

    Map<String, Object> map = new HashMap<>();
    map.put(ProtoConst.JSON_ID_NAME, "User");
    map.put("name", "n1");
    Root root = new Root();
    root.setAny(map);
    Assertions.assertArrayEquals(protobufBytes, rootSerializer.serialize(root));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void json_fromMapWithoutType() throws Throwable {
    Map<String, Object> map = new HashMap<>();
    map.put("name", "n1");
    Root root = new Root();
    root.setAny(map);

    scbRootBytes = rootSerializer.serialize(root);
    root = rootDeserializer.deserialize(scbRootBytes);
    MatcherAssert.assertThat(root.getAny(), Matchers.instanceOf(Map.class));
    MatcherAssert.assertThat((Map<? extends String, ? extends String>) root.getAny(), Matchers.hasEntry("name", "n1"));

    RootDeserializer<Map<String, Object>> deserializer = protoMapper.createRootDeserializer("Root", Map.class);
    map = deserializer.deserialize(scbRootBytes);
    MatcherAssert.assertThat((Map<? extends String, ? extends String>) map.get("any"), Matchers.hasEntry("name", "n1"));
  }

  @Test
  public void json() throws Throwable {
    Root root = new Root();
    root.setAny("abc");

    scbRootBytes = rootSerializer.serialize(root);
    root = rootDeserializer.deserialize(scbRootBytes);
    Assertions.assertEquals("abc", root.getAny());
  }
}
