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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.foundation.protobuf.internal.TestSchemaBase;
import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot;
import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot.Color;
import org.apache.servicecomb.foundation.protobuf.internal.model.User;
import org.junit.Assert;
import org.junit.Test;

import com.google.protobuf.ByteString;

public class TestRepeatedSchema extends TestSchemaBase {
  public static class RootWithArray {
    public byte[][] bytess;

    public String[] strings;

    public User[] users;
  }

  @Test
  public void fixed32s() throws Exception {
    builder.addAllFixed32SPacked(Arrays.asList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void fixed32sNotPacked() throws Exception {
    builder.addAllFixed32SNotPacked(Arrays.asList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void sfixed32s() throws Exception {
    builder.addAllSfixed32SPacked(Arrays.asList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void sfixed32sNotPacked() throws Exception {
    builder.addAllSfixed32SNotPacked(Arrays.asList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void int32s() throws Exception {
    builder.addAllInt32SPacked(Arrays.asList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void int32sNotPacked() throws Exception {
    builder.addAllInt32SNotPacked(Arrays.asList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void sint32s() throws Exception {
    builder.addAllSint32SPacked(Arrays.asList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void sint32sNotPacked() throws Exception {
    builder.addAllSint32SNotPacked(Arrays.asList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void uint32s() throws Exception {
    builder.addAllUint32SPacked(Arrays.asList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void uint32sNotPacked() throws Exception {
    builder.addAllUint32SNotPacked(Arrays.asList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void fixed64s() throws Exception {
    builder.addAllFixed64SPacked(Arrays.asList(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void fixed64sNotPacked() throws Exception {
    builder.addAllFixed64SNotPacked(Arrays.asList(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void sfixed64s() throws Exception {
    builder.addAllSfixed64SPacked(Arrays.asList(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void sfixed64sNotPacked() throws Exception {
    builder.addAllSfixed64SNotPacked(Arrays.asList(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void int64s() throws Exception {
    builder.addAllInt64SPacked(Arrays.asList(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void int64sNotPacked() throws Exception {
    builder.addAllInt64SNotPacked(Arrays.asList(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void sint64s() throws Exception {
    builder.addAllSint64SPacked(Arrays.asList(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void sint64sNotPacked() throws Exception {
    builder.addAllSint64SNotPacked(Arrays.asList(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void uint64s() throws Exception {
    builder.addAllUint64SPacked(Arrays.asList(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void uint64sNotPacked() throws Exception {
    builder.addAllUint64SNotPacked(Arrays.asList(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void floats() throws Exception {
    builder.addAllFloatsPacked(Arrays.asList(Float.MIN_VALUE, (float) 0.0, Float.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void floatsNotPacked() throws Exception {
    builder.addAllFloatsNotPacked(Arrays.asList(Float.MIN_VALUE, (float) 0.0, Float.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void doubles() throws Exception {
    builder.addAllDoublesPacked(Arrays.asList(Double.MIN_VALUE, 0.0, Double.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void doublesNotPacked() throws Exception {
    builder.addAllDoublesNotPacked(Arrays.asList(Double.MIN_VALUE, 0.0, Double.MAX_VALUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void bools() throws Exception {
    builder.addAllBoolsPacked(Arrays.asList(Boolean.FALSE, Boolean.TRUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void boolsNotPacked() throws Exception {
    builder.addAllBoolsNotPacked(Arrays.asList(Boolean.FALSE, Boolean.TRUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void enums() throws Exception {
    builder.addAllColorsPacked(Arrays.asList(Color.RED, Color.BLUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void enumsNotPacked() throws Exception {
    builder.addAllColorsNotPacked(Arrays.asList(Color.RED, Color.BLUE));
    checkRepeatedWithPrimitive();
  }

  @Test
  public void bytess() throws Throwable {
    List<byte[]> sList = Arrays.asList("v1".getBytes(StandardCharsets.UTF_8), "v2".getBytes(StandardCharsets.UTF_8));
    builder.addAllBytess(Arrays.asList(ByteString.copyFromUtf8("v1"), ByteString.copyFromUtf8("v2")));
    check();

    RootWithArray rootWithArray = new RootWithArray();
    rootWithArray.bytess = (byte[][]) sList.toArray();
    Assert.assertArrayEquals(protobufBytes, rootSerializer.serialize(rootWithArray));
  }

  @Test
  public void strings() throws Throwable {
    List<String> sList = Arrays.asList("v1", "v2");
    builder.addAllStrings(sList);
    check();

    RootWithArray rootWithArray = new RootWithArray();
    rootWithArray.strings = (String[]) sList.toArray();
    Assert.assertArrayEquals(protobufBytes, rootSerializer.serialize(rootWithArray));
  }

  @Test
  public void users() throws Throwable {
    builder.addUsers(ProtobufRoot.User.newBuilder().setName("name1").build());
    builder.addUsers(ProtobufRoot.User.newBuilder().setName("name2").build());

    check();

    RootWithArray rootWithArray = new RootWithArray();
    rootWithArray.users = new User[] {new User("name1"), new User("name2")};
    Assert.assertArrayEquals(protobufBytes, rootSerializer.serialize(rootWithArray));
  }
}
