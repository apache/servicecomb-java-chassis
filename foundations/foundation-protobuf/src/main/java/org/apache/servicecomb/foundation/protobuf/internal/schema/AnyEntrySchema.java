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

import java.io.IOException;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;

import io.protostuff.CustomSchema;
import io.protostuff.Input;
import io.protostuff.Output;

public class AnyEntrySchema extends CustomSchema<Object> {
  private final ProtoMapper protoMapper;

  public AnyEntrySchema(ProtoMapper protoMapper) {
    super(null);
    this.protoMapper = protoMapper;
  }

  @Override
  public boolean isInitialized(Object message) {
    return true;
  }

  @Override
  public Object newMessage() {
    return new AnyEntry();
  }

  @Override
  public void mergeFrom(Input input, Object message) throws IOException {
    input.readFieldNumber(null);
    String typeUrl = input.readString();

    input.readFieldNumber(null);
    byte[] bytes = input.readByteArray();

    input.readFieldNumber(null);

    AnyEntry anyEntry = (AnyEntry) message;
    anyEntry.setTypeUrl(typeUrl);
    anyEntry.setValue(bytes);
  }

  protected String getInputActualTypeName(Object input) {
    if (!(input instanceof Map)) {
      return input.getClass().getSimpleName();
    }

    // @JsonTypeInfo(use = Id.NAME)
    Object actualTypeName = ((Map<?, ?>) input).get(ProtoConst.JSON_ID_NAME);
    if (actualTypeName != null && actualTypeName instanceof String) {
      return (String) actualTypeName;
    }

    return null;
  }

  /**
   * <pre>
   * if message is type of CustomGeneric&lt;User&gt;
   * we can not get any information of "User" from message.getClass()
   *
   * when use with ServiceComb
   * proto definition convert from swagger, the proto type will be "CustomGenericUser"
   * is not match to "CustomGeneric"
   * so message will be serialized with json schema
   * </pre>
   * @param output
   * @param message
   * @throws IOException
   */
  @Override
  public void writeTo(Output output, Object message) throws IOException {
    String actualTypeName = getInputActualTypeName(message);
    RootSerializer actualValueSerializer = protoMapper.findRootSerializer(actualTypeName);
    if (actualValueSerializer != null) {
      standardPack(output, message, actualValueSerializer);
      return;
    }

    // not standard, protobuf can not support or not define this type , just extend
    jsonExtend(output, message);
  }

  protected void standardPack(Output output, Object message, RootSerializer actualValueSerializer) throws IOException {
    output.writeString(1,
        ProtoConst.PACK_SCHEMA + actualValueSerializer.getSchema().getMessage().getCanonicalName(),
        false);

    byte[] bytes = actualValueSerializer.serialize(message);
    output.writeByteArray(2, bytes, false);
  }

  protected void jsonExtend(Output output, Object input) throws IOException {
    output.writeString(1, ProtoConst.JSON_SCHEMA + input.getClass().getName(), false);

    byte[] bytes = protoMapper.getJsonMapper().writeValueAsBytes(input);
    output.writeByteArray(2, bytes, false);
  }
}
