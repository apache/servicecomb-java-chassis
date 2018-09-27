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
package org.apache.servicecomb.foundation.protobuf.performance.engine;

import java.io.IOException;
import java.net.URL;

import org.apache.servicecomb.foundation.protobuf.internal.model.Root;
import org.apache.servicecomb.foundation.protobuf.internal.model.User;
import org.apache.servicecomb.foundation.protobuf.performance.ProtubufCodecEngine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchemaLoader;

public class Jackson implements ProtubufCodecEngine {
  @JsonIgnoreProperties({"ssMap", "spMap", "any", "anys", "typeRecursive"})
  interface RootMixin {
  }

  @JsonIgnoreProperties({"typeRecursive"})
  interface UserMixin {
  }

  public static ObjectMapper jsonMapper = new ObjectMapper();

  static ProtobufMapper protobufMapper = new ProtobufMapper();

  static URL url = Jackson.class.getClassLoader().getResource("jacksonRoot.proto");

  static ProtobufSchema protobufSchema;

  static {
    protobufMapper.addMixIn(Root.class, RootMixin.class);
    protobufMapper.addMixIn(User.class, UserMixin.class);
    try {
      protobufSchema = ProtobufSchemaLoader.std.load(url);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static ObjectWriter writer = protobufMapper.writer(protobufSchema);

  static ObjectReader reader = protobufMapper.reader(protobufSchema).forType(Root.class);

  @Override
  public byte[] serialize(Object model) throws IOException {
    return writer.writeValueAsBytes(model);
  }

  @Override
  public Object deserialize(byte[] bytes) throws IOException {
    return reader.readValue(bytes);
  }
}
