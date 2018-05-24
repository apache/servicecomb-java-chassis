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

package org.apache.servicecomb.codec.protobuf.utils;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.javassist.FieldConfig;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import io.protostuff.ByteArrayInput;
import io.protostuff.Input;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufOutput;

public class TestProtobufSchemaUtils {
  class MyClassLoader extends ClassLoader {
    public MyClassLoader() {
      super(Thread.currentThread().getContextClassLoader());
    }
  }

  //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();//new MyClassLoader();
  ClassLoader classLoader = new MyClassLoader();

  ScopedProtobufSchemaManager scopedProtobufSchemaManager = new ScopedProtobufSchemaManager(classLoader);

  public static class TestMap {
    public Map<String, String> map = new HashMap<>();

    public TestMap() {
      map.put("asdf", "jjj");
    }
  }

  @Test
  public void testMap() throws Exception {
    TestMap tm = new TestMap();
    TestMap tmResult = writeThenRead(tm);
    Assert.assertEquals(tm.map, tmResult.map);

    Map<String, String> map = new HashMap<>();
    map.put("aaa", "bbb");
    testSchema(map);
  }

  @Test
  public void wrapPrimitive() throws Exception {
    Assert.assertNotNull(WrapType.ARGS_WRAP);
    Assert.assertNotNull(WrapType.NORMAL_WRAP);
    testSchema(1);
    testSchema("test");
    testSchema(WrapType.ARGS_WRAP);
    Assert.assertTrue(true);
  }

  @Test
  public void wrapArray() throws Exception {
    Assert.assertNotNull(WrapType.ARGS_WRAP);
    Assert.assertNotNull(WrapType.NORMAL_WRAP);
    testArraySchema(new byte[] {0, 1, 2});
    testArraySchema(new int[] {0, 1, 2});
    testArraySchema(new String[] {"a", "b"});
    testArraySchema(new WrapType[] {WrapType.ARGS_WRAP, WrapType.NORMAL_WRAP});
    Assert.assertTrue(true);
  }

  @Test
  public void notWrap() throws Exception {
    FieldConfig expect = new FieldConfig();
    expect.setName("test");

    FieldConfig result = writeThenRead(expect);
    Assert.assertEquals(expect.getName(), result.getName());
  }

  private void testSchema(Object expect) throws Exception {
    Object result = writeThenRead(expect);
    Assert.assertEquals(expect, result);
  }

  private void testArraySchema(Object expect) throws Exception {
    Object result = writeThenRead(expect);

    int expectLen = Array.getLength(expect);
    Assert.assertEquals(expectLen, Array.getLength(result));
    for (int idx = 0; idx < expectLen; idx++) {
      Assert.assertEquals(Array.get(expect, idx), Array.get(result, idx));
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T writeThenRead(T value) throws Exception {
    WrapSchema schema = scopedProtobufSchemaManager.getOrCreateSchema(value.getClass());

    byte[] bytes = toByteArray(schema, value);
    Object result = toObject(schema, bytes);
    return (T) result;
  }

  private byte[] toByteArray(WrapSchema schema, Object value) throws Exception {
    LinkedBuffer linkedBuffer = LinkedBuffer.allocate();
    ProtobufOutput output = new ProtobufOutput(linkedBuffer);

    schema.writeObject(output, value);
    return output.toByteArray();
  }

  private Object toObject(WrapSchema schema, byte[] bytes) throws Exception {
    Input input = new ByteArrayInput(bytes, false);

    return schema.readObject(input);
  }

  @Test
  public void object() throws Exception {
    WrapSchema schema = scopedProtobufSchemaManager.getOrCreateSchema(Object.class);

    LinkedBuffer linkedBuf = LinkedBuffer.allocate();
    ProtobufOutput output = new ProtobufOutput(linkedBuf);
    schema.writeObject(output, 1);

    Input input = new ByteArrayInput(output.toByteArray(), false);
    Object result = schema.readObject(input);

    Assert.assertEquals(1, result);
    Assert.assertThat(result, Matchers.instanceOf(Integer.class));
  }
}
