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
package org.apache.servicecomb.foundation.protobuf.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.ProtoMapperFactory;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot;
import org.apache.servicecomb.foundation.protobuf.internal.model.Root;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import io.protostuff.compiler.model.Field;

public class TestSchemaBase {
  protected static ProtoMapperFactory factory = new ProtoMapperFactory();

  protected static ProtoMapper protoMapper = factory.createFromName("protobufRoot.proto");

  protected static RootSerializer rootSerializer = protoMapper.createRootSerializer("Root", Root.class);

  protected static RootDeserializer<Root> rootDeserializer = protoMapper.createRootDeserializer("Root", Root.class);

  protected static RootDeserializer<Map<String, Object>> mapRootDeserializer = protoMapper
      .createRootDeserializer("Root", Map.class);

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  protected Object scbRoot;

  protected byte[] scbRootBytes;

  protected Map<String, Object> scbMap;

  protected byte[] scbMapBytes;

  protected ProtobufRoot.Root.Builder builder = ProtobufRoot.Root.newBuilder();

  protected byte[] protobufBytes;

  protected String primitiveFieldName;

  protected Field primitiveProtoField;

  protected String fieldName;

  protected Field protoField;

  protected void initFields(String primitiveFieldName, String fieldName) {
    this.primitiveFieldName = primitiveFieldName;
    if (primitiveFieldName != null) {
      primitiveProtoField = protoMapper.getProto().getMessage("Root").getField(primitiveFieldName);
    }

    initField(fieldName);
  }

  protected void initField(String fieldName) {
    this.fieldName = fieldName;
    if (fieldName != null) {
      protoField = protoMapper.getProto().getMessage("Root").getField(fieldName);
    }
  }

  protected void check() throws IOException {
    check(rootDeserializer, mapRootDeserializer, rootSerializer, false);
  }

  protected void check(boolean print) throws IOException {
    check(rootDeserializer, mapRootDeserializer, rootSerializer, print);
  }

  protected <T> void check(RootDeserializer<T> deserializer, RootDeserializer<Map<String, Object>> mapDeserializer,
      RootSerializer serializer,
      boolean print) throws IOException {
    // 1.standard protobuf serialize to bytes
    protobufBytes = builder.build().toByteArray();
    // 2.weak type deserialize
    scbMap = mapDeserializer.deserialize(protobufBytes);
    // 3.weak type serialize
    scbMapBytes = rootSerializer.serialize(scbMap);
    // 4.strong type deserialize
    scbRoot = deserializer.deserialize(scbMapBytes);
    // 5.strong type serialize
    scbRootBytes = rootSerializer.serialize(scbRoot);

    if (print) {
      System.out.println("scbRoot bytes:" + Hex.encodeHexString(scbRootBytes));
      System.out.println("scbRoot len  :" + scbRootBytes.length);
      System.out.println("scbMap bytes :" + Hex.encodeHexString(scbMapBytes));
      System.out.println("scbMap len   :" + scbMapBytes.length);
      System.out.println("protobuf     :" + Hex.encodeHexString(protobufBytes));
      System.out.println("protobuf len :" + protobufBytes.length);
    }
    Assert.assertArrayEquals(protobufBytes, scbMapBytes);
    Assert.assertArrayEquals(protobufBytes, scbRootBytes);
  }
}
