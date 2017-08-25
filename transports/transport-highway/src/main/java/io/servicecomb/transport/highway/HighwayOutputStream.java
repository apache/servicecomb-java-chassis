/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.transport.highway;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufOutput;
import io.protostuff.runtime.ProtobufFeature;
import io.servicecomb.codec.protobuf.utils.WrapSchema;
import io.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import io.servicecomb.transport.highway.message.RequestHeader;
import io.servicecomb.transport.highway.message.ResponseHeader;

public class HighwayOutputStream extends TcpOutputStream {
  private ProtobufFeature protobufFeature;

  public HighwayOutputStream(long msgId, ProtobufFeature protobufFeature) {
    super(msgId);
    this.protobufFeature = protobufFeature;
  }

  public void write(RequestHeader header, WrapSchema bodySchema, Object body) throws Exception {
    write(RequestHeader.getRequestHeaderSchema(), header, bodySchema, body);
  }

  public void write(ResponseHeader header, WrapSchema bodySchema, Object body) throws Exception {
    write(ResponseHeader.getResponseHeaderSchema(), header, bodySchema, body);
  }

  public void write(WrapSchema headerSchema, Object header, WrapSchema bodySchema, Object body) throws Exception {
    // 写protobuf数据
    LinkedBuffer linkedBuffer = LinkedBuffer.allocate();
    ProtobufOutput output = new ProtobufOutput(linkedBuffer);

    // 写header
    if (headerSchema != null) {
      headerSchema.writeObject(output, header, protobufFeature);
    }
    int headerSize = output.getSize();

    // 写body
    // void时bodySchema为null
    if (bodySchema != null) {
      bodySchema.writeObject(output, body, protobufFeature);
    }

    writeLength(output.getSize(), headerSize);
    LinkedBuffer.writeTo(this, linkedBuffer);
  }
}
