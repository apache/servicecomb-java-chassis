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

import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.ProtoMapperFactory;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.foundation.protobuf.internal.model.Root;

public class SCB {
  static ProtoMapperFactory factory = new ProtoMapperFactory();

  static ProtoMapper protoMapper = factory.createFromName("protobufRoot.proto");

  static RootSerializer serializer = protoMapper.findRootSerializer("Root");

  static RootDeserializer deserializer = protoMapper.createRootDeserializer(Root.class, "Root");

  static RootDeserializer mapDeserializer = protoMapper.createRootDeserializer(Map.class, "Root");
}
