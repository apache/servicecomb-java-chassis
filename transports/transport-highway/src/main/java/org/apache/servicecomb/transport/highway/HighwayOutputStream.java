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
package org.apache.servicecomb.transport.highway;

import org.apache.servicecomb.codec.protobuf.definition.RequestRootSerializer;
import org.apache.servicecomb.codec.protobuf.definition.ResponseRootSerializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.apache.servicecomb.transport.highway.message.ResponseHeader;

public class HighwayOutputStream extends TcpOutputStream {
  public HighwayOutputStream(long msgId) {
    super(msgId);
  }

  public void write(RequestHeader header, RequestRootSerializer requestRootSerializer, Object body) throws Exception {
    write(RequestHeader.getRootSerializer().serialize(header), requestRootSerializer.serialize(body));
  }

  public void write(ResponseHeader header, ResponseRootSerializer responseRootSerializer, Object body) throws Exception {
    write(ResponseHeader.getRootSerializer().serialize(header), responseRootSerializer.serialize(body));
  }

  public void write(RequestHeader header, RootSerializer bodySchema, Object body) throws Exception {
    write(RequestHeader.getRootSerializer(), header, bodySchema, body);
  }

  public void write(ResponseHeader header, RootSerializer bodySchema, Object body) throws Exception {
    write(ResponseHeader.getRootSerializer(), header, bodySchema, body);
  }

  public void write(RootSerializer headerSchema, Object header, RootSerializer bodySchema, Object body)
      throws Exception {
    byte[] headerBytes = new byte[0];
    byte[] bodyBytes = new byte[0];

    // 写header
    if (headerSchema != null) {
      headerBytes = headerSchema.serialize(header);
    }

    // 写body
    // void时bodySchema为null
    if (bodySchema != null) {
      bodyBytes = bodySchema.serialize(body);
    }

    write(headerBytes, bodyBytes);
  }

  private void write(byte[] headerBytes, byte[] bodyBytes)
      throws Exception {
    int headerLength = 0;
    int totalLength = 0;

    if (headerBytes != null) {
      headerLength = headerBytes.length;
      totalLength = totalLength + headerLength;
    }

    if (bodyBytes != null) {
      totalLength = totalLength + bodyBytes.length;
    }

    this.writeLength(totalLength, headerLength);
    this.write(headerBytes);
    this.write(bodyBytes);
  }
}
