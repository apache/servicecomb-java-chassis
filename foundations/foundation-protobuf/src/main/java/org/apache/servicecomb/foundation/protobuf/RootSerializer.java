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
package org.apache.servicecomb.foundation.protobuf;

import java.io.IOException;

import org.apache.servicecomb.foundation.protobuf.internal.schema.serializer.ProtoStreamOutput;

import io.protostuff.runtime.MessageSchema;

public class RootSerializer {
  private MessageSchema schema;

  public RootSerializer(MessageSchema schema) {
    this.schema = schema;
  }

  public MessageSchema getSchema() {
    return schema;
  }

  public void setSchema(MessageSchema schema) {
    this.schema = schema;
  }

  /**
   * not same to standard ProtoStuff mechanism
   * parameter "value" of writeTo is self value, not owner
   * @param value
   * @return
   * @throws Throwable
   */
  public byte[] serialize(Object value) throws IOException {
    ProtoStreamOutput output = new ProtoStreamOutput();
    schema.writeTo(output, value);
    return output.toBytes();
  }
}
