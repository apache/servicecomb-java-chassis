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

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.foundation.vertx.tcp.TcpConnection;
import org.apache.servicecomb.swagger.invocation.context.VertxTransportContext;
import org.apache.servicecomb.transport.highway.message.RequestHeader;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class HighwayTransportContext implements VertxTransportContext {
  private final Context vertxContext;

  private TcpConnection connection;

  private long msgId;

  private RequestHeader header;

  private Buffer bodyBuffer;

  private OperationProtobuf operationProtobuf;

  private Buffer responseBuffer;

  public HighwayTransportContext() {
    this.vertxContext = Vertx.currentContext();
  }

  @Override
  public Context getVertxContext() {
    return vertxContext;
  }

  public TcpConnection getConnection() {
    return connection;
  }

  public HighwayTransportContext setConnection(TcpConnection connection) {
    this.connection = connection;
    return this;
  }

  public long getMsgId() {
    return msgId;
  }

  public HighwayTransportContext setMsgId(long msgId) {
    this.msgId = msgId;
    return this;
  }

  public RequestHeader getHeader() {
    return header;
  }

  public HighwayTransportContext setHeader(RequestHeader header) {
    this.header = header;
    return this;
  }

  public Buffer getBodyBuffer() {
    return bodyBuffer;
  }

  public HighwayTransportContext setBodyBuffer(Buffer bodyBuffer) {
    this.bodyBuffer = bodyBuffer;
    return this;
  }

  public OperationProtobuf getOperationProtobuf() {
    return operationProtobuf;
  }

  public HighwayTransportContext setOperationProtobuf(
      OperationProtobuf operationProtobuf) {
    this.operationProtobuf = operationProtobuf;
    return this;
  }

  public Buffer getResponseBuffer() {
    return responseBuffer;
  }

  public HighwayTransportContext setResponseBuffer(Buffer responseBuffer) {
    this.responseBuffer = responseBuffer;
    return this;
  }
}
