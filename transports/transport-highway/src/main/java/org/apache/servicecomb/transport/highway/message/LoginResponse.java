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
package org.apache.servicecomb.transport.highway.message;

import org.apache.servicecomb.codec.protobuf.definition.ProtobufManager;
import org.apache.servicecomb.codec.protobuf.utils.WrapSchema;

import io.protostuff.ProtobufOutput;
import io.protostuff.Tag;
import io.vertx.core.buffer.Buffer;

public class LoginResponse {
  private static WrapSchema loginResponseSchema = ProtobufManager.getDefaultScopedProtobufSchemaManager()
      .getOrCreateSchema(LoginResponse.class);

  public static WrapSchema getLoginResponseSchema() {
    return loginResponseSchema;
  }

  public static LoginResponse readObject(Buffer bodyBuffer) throws Exception {
    return loginResponseSchema.readObject(bodyBuffer);
  }

  @Tag(1)
  private String protocol;

  // 压缩算法名字
  @Tag(2)
  private String zipName;

  // no need this flag any more, but tag(3) should be reserved
  // 历史版本中的protoStuff实现的protobuf的map编码与标准的protobuf不兼容
  // 为保持highway的兼容，旧的不兼容编码也要保留
  // 只有LoginRequest/LoginResponse同时为true时，才使用标准protobuf编码
  //@Tag(3)
  //private boolean useProtobufMapCodec;

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getZipName() {
    return zipName;
  }

  public void setZipName(String zipName) {
    this.zipName = zipName;
  }

  public void writeObject(ProtobufOutput output) throws Exception {
    loginResponseSchema.writeObject(output, this);
  }
}
