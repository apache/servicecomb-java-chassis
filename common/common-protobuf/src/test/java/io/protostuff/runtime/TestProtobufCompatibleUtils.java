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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.protostuff.ByteArrayInput;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufOutput;
import io.protostuff.runtime.model.ModelProtobuf;
import io.protostuff.runtime.model.ModelProtostuff;
import io.protostuff.runtime.model.User;

public class TestProtobufCompatibleUtils {
  @Test
  public void test() throws IOException {
    ProtobufCompatibleUtils.init();

    Map<String, String> stringMap = new HashMap<>();
    stringMap.put("k1", "v1");
    stringMap.put("k2", "v2");

    Map<String, User> userMap = new HashMap<>();
    userMap.put("u1", new User("n1"));
    userMap.put("u2", new User("n2"));

    byte[] protostuffBytes = testProtostuff(stringMap, userMap);

    io.protostuff.runtime.model.ModelProtobuf.RequestHeader r =
        ModelProtobuf.RequestHeader.newBuilder()
            .putAllCseContext(stringMap)
            .putUserMap("u1", ModelProtobuf.User.newBuilder().setName("n1").build())
            .putUserMap("u2", ModelProtobuf.User.newBuilder().setName("n2").build())
            .addList("l1")
            .addList("l2")
            .build();

    byte[] protoBufBytes = r.toByteArray();
    Assert.assertArrayEquals(protostuffBytes, protoBufBytes);
  }

  protected byte[] testProtostuff(Map<String, String> map, Map<String, User> userMap) throws IOException {
    ProtobufCompatibleUtils.init();

    RuntimeSchema<ModelProtostuff> schema = RuntimeSchema.createFrom(ModelProtostuff.class);
    ModelProtostuff model = new ModelProtostuff();

    model.setContext(map);
    model.setUserMap(userMap);
    model.getList().add("l1");
    model.getList().add("l2");

    LinkedBuffer linkedBuffer = LinkedBuffer.allocate();
    ProtobufOutput output = new ProtobufOutput(linkedBuffer);

    schema.writeTo(output, model);

    ByteArrayOutputStream s = new ByteArrayOutputStream();
    LinkedBuffer.writeTo(s, linkedBuffer);
    byte[] bytes = s.toByteArray();

    ModelProtostuff newModel = new ModelProtostuff();
    ByteArrayInput bai = new ByteArrayInput(bytes, false);

    schema.mergeFrom(bai, newModel);

    Assert.assertEquals("v1", newModel.getContext().get("k1"));
    Assert.assertEquals("v2", newModel.getContext().get("k2"));
    Assert.assertEquals("n1", newModel.getUserMap().get("u1").getName());
    Assert.assertEquals("n2", newModel.getUserMap().get("u2").getName());
    Assert.assertEquals("l1", newModel.getList().get(0));
    Assert.assertEquals("l2", newModel.getList().get(1));

    return bytes;
  }
}
