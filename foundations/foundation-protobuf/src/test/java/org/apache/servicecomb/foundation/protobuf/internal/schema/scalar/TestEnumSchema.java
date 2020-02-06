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
package org.apache.servicecomb.foundation.protobuf.internal.schema.scalar;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.internal.TestSchemaBase;
import org.apache.servicecomb.foundation.protobuf.internal.model.User;
import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestEnumSchema extends TestSchemaBase {
  @Test
  public void empty() throws Throwable {
    // null
    scbMap = new HashMap<>();
    Assert.assertEquals(0, rootSerializer.serialize(scbMap).length);

    // empty string[]
    scbMap.put("color", new String[] {});
    Assert.assertEquals(0, rootSerializer.serialize(scbMap).length);
  }

  public static class EnumRoot {
    private int color;

    public int getColor() {
      return color;
    }

    public void setColor(int color) {
      this.color = color;
    }
  }

  @Test
  public void normal() throws Throwable {
    builder.setColorValue(2);
    check();

    Map<String, Object> map = new HashMap<>();
    map.put("color", Color.BLUE);
    Assert.assertArrayEquals(protobufBytes, rootSerializer.serialize(map));

    map.put("color", 2);
    Assert.assertArrayEquals(protobufBytes, rootSerializer.serialize(map));

    map.put("color", new String[] {"BLUE"});
    Assert.assertArrayEquals(protobufBytes, rootSerializer.serialize(map));

    map.put("color", "BLUE");
    Assert.assertArrayEquals(protobufBytes, rootSerializer.serialize(map));

    EnumRoot enumRoot = protoMapper.<EnumRoot>createRootDeserializer("Root", EnumRoot.class).deserialize(protobufBytes);
    Assert.assertEquals(2, enumRoot.color);
  }

  enum Sharp {
    ROUND
  }

  @Test
  public void fromInvalidEnum() throws Throwable {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers
        .is("invalid enum name ROUND for proto Color, field=org.apache.servicecomb.foundation.protobuf.internal.model.Root:color"));

    scbMap = new HashMap<>();
    scbMap.put("color", Sharp.ROUND);
    rootSerializer.serialize(scbMap);
  }

  @Test
  public void fromInvalidNumber() throws Throwable {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers
        .is("invalid enum value 3 for proto Color, field=org.apache.servicecomb.foundation.protobuf.internal.model.Root:color"));

    scbMap = new HashMap<>();
    scbMap.put("color", 3);
    rootSerializer.serialize(scbMap);
  }

  @Test
  public void type_invalid() throws Throwable {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers
        .is("not support serialize from org.apache.servicecomb.foundation.protobuf.internal.model.User to proto Color, field=org.apache.servicecomb.foundation.protobuf.internal.model.Root:color"));

    scbMap = new HashMap<>();
    scbMap.put("color", new User());
    rootSerializer.serialize(scbMap);
  }
}
