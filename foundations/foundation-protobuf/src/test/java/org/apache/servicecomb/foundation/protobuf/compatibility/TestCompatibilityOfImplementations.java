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

package org.apache.servicecomb.foundation.protobuf.compatibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot;
import org.apache.servicecomb.foundation.protobuf.internal.model.Root;
import org.apache.servicecomb.foundation.protobuf.performance.ProtubufCodecEngine;
import org.apache.servicecomb.foundation.protobuf.performance.engine.Protobuf;
import org.apache.servicecomb.foundation.protobuf.performance.engine.ScbWeak;
import org.junit.Assert;
import org.junit.Test;

public class TestCompatibilityOfImplementations {
  static ProtubufCodecEngine scbWeak = new ScbWeak();

  static ProtubufCodecEngine protobuf = new Protobuf();

  @Test
  @SuppressWarnings("unchecked")
  public void testEmptyCollection() throws Exception {
    ProtobufRoot.Root.Builder builder = ProtobufRoot.Root.newBuilder();
    byte[] values = protobuf.serialize(builder);
    Assert.assertEquals(values.length, 0);
    ProtobufRoot.Root.Builder o = (ProtobufRoot.Root.Builder) protobuf.deserialize(values);
    Assert.assertTrue(o.getFixed32SNotPackedList().isEmpty());

    builder = ProtobufRoot.Root.newBuilder().addFixed32SNotPacked(30);
    values = protobuf.serialize(builder);
    Assert.assertArrayEquals(new byte[] {(byte) -123, (byte) 6, (byte) 30, (byte) 0, (byte) 0, (byte) 0}, values);
    o = (ProtobufRoot.Root.Builder) protobuf.deserialize(values);
    Assert.assertEquals(30, (int) o.getFixed32SNotPackedList().get(0));

    Root root = new Root();
    root.setFixed32sNotPacked(new ArrayList<>());
    values = scbWeak.serialize(root);
    Assert.assertEquals(values.length, 0);
    Map<String, Object> newRootMap = (Map<String, Object>) scbWeak.deserialize(values);
    Assert.assertEquals(null,
        newRootMap.get("fixed32sNotPacked")); // This is different , because depends on default model initializer

    List<Integer> iValues = new ArrayList<>();
    iValues.add(30);
    root.setFixed32sNotPacked(iValues);
    values = scbWeak.serialize(root);
    Assert.assertArrayEquals(new byte[] {(byte) -123, (byte) 6, (byte) 30, (byte) 0, (byte) 0, (byte) 0}, values);
    newRootMap = (Map<String, Object>) scbWeak.deserialize(values);
    Assert.assertEquals(30, (int) ((List<Integer>) newRootMap.get("fixed32sNotPacked")).get(0));
  }
}
