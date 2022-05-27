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

import org.apache.servicecomb.foundation.protobuf.internal.TestSchemaBase;
import org.apache.servicecomb.foundation.protobuf.internal.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestBoolSchema extends TestSchemaBase {
  public TestBoolSchema() {
    initFields("bool", "objBool");
  }

  @Test
  public void testTrue() throws Throwable {
    // normal
    builder.setBool(true);
    check();

    // equalsIgnoreCase
    doTestFromString("true");
    doTestFromString("trUe");
  }

  @Test
  public void testFalse() throws Throwable {
    // normal
    builder.setBool(false);
    check();

    // all not true is false
    doTestFromString("false");
    doTestFromString("abcd");
  }

  protected void doTestFromString(String value) throws Throwable {
    // string[]
    scbMap = new HashMap<>();
    scbMap.put("bool", new String[] {value});
    Assertions.assertArrayEquals(protobufBytes, rootSerializer.serialize(scbMap));

    // string
    scbMap.put("bool", value);
    Assertions.assertArrayEquals(protobufBytes, rootSerializer.serialize(scbMap));
  }

  @Test
  public void nullOrEmpty() throws Throwable {
    // null
    scbMap = new HashMap<>();
    Assertions.assertEquals(0, rootSerializer.serialize(scbMap).length);

    // empty string[]
    scbMap.put("bool", new String[] {});
    Assertions.assertEquals(0, rootSerializer.serialize(scbMap).length);
  }

  @Test
  public void type_invalid() throws Throwable {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> {
      scbMap = new HashMap<>();
      scbMap.put("bool", new User());
      rootSerializer.serialize(scbMap);
    });
    Assertions.assertEquals("not support serialize from org.apache.servicecomb.foundation.protobuf.internal.model.User to proto bool, field=org.apache.servicecomb.foundation.protobuf.internal.model.Root:bool",
            exception.getMessage());
  }
}

