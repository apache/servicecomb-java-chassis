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

import java.nio.ByteBuffer;

import io.protostuff.ByteBufferInput;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.runtime.ProtobufFeature;
import io.protostuff.runtime.ProtobufFeatureUtils;
import io.vertx.core.buffer.Buffer;

public interface WrapSchema {
  @SuppressWarnings("unchecked")
  default <T> T readObject(Buffer buffer, ProtobufFeature protobufFeature) throws Exception {
    if (buffer == null || buffer.length() == 0) {
      // void以及函数入参为null的场景
      // 空串时,protobuf至少为编码为1字节
      return (T) readFromEmpty();
    }

    ByteBuffer nioBuffer = buffer.getByteBuf().nioBuffer();
    Input input = new ByteBufferInput(nioBuffer, false);

    ProtobufFeatureUtils.setProtobufFeature(protobufFeature);
    try {
      return (T) readObject(input);
    } finally {
      ProtobufFeatureUtils.removeProtobufFeature();
    }
  }

  default void writeObject(Output output, Object value, ProtobufFeature protobufFeature) throws Exception {
    ProtobufFeatureUtils.setProtobufFeature(protobufFeature);
    try {
      writeObject(output, value);
    } finally {
      ProtobufFeatureUtils.removeProtobufFeature();
    }
  }

  Object readFromEmpty();

  Object readObject(Input input) throws Exception;

  void writeObject(Output output, Object value) throws Exception;
}
