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
package org.apache.servicecomb.foundation.protobuf.internal.schema.serializer;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.internal.TestSchemaBase;
import org.apache.servicecomb.foundation.protobuf.internal.model.User;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestEnumSerializerSchema extends TestSchemaBase {
  public TestEnumSerializerSchema() {
    initField("color");
  }

  @Test
  public void empty() throws Throwable {
    Assert.assertEquals(0, serFieldSchema.writeTo(null).length);
    Assert.assertEquals(0, serFieldSchema.writeTo(new String[0]).length);
  }

  @Test
  public void normal() throws Throwable {
    builder.setColorValue(2);
    check();

    Map<String, Object> map = new HashMap<>();
    map.put("color", 2);
    Assert.assertArrayEquals(protobufBytes, rootSerializer.serialize(map));

    map.put("color", new String[] {"BLUE"});
    Assert.assertArrayEquals(protobufBytes, rootSerializer.serialize(map));

    map.put("color", "BLUE");
    Assert.assertArrayEquals(protobufBytes, rootSerializer.serialize(map));
  }

  enum Sharp {
    ROUND
  }

  @Test
  public void fromInvalidEnum() throws Throwable {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers
        .is("invalid enum name ROUND for proto Color, field=org.apache.servicecomb.foundation.protobuf.internal.model.Root:color"));

    serFieldSchema.writeTo(Sharp.ROUND);
  }

  @Test
  public void fromInvalidNumber() throws Throwable {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers
        .is("invalid enum value 3 for proto Color, field=org.apache.servicecomb.foundation.protobuf.internal.model.Root:color"));

    serFieldSchema.writeTo(3);
  }

  @Test
  public void type_invalid() throws Throwable {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers
        .is("not support serialize from org.apache.servicecomb.foundation.protobuf.internal.model.User to proto Color, field=org.apache.servicecomb.foundation.protobuf.internal.model.Root:color"));

    serFieldSchema.writeTo(new User());
  }
}
