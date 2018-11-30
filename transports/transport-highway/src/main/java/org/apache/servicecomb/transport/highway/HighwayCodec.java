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
import org.apache.servicecomb.codec.protobuf.utils.WrapSchema;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpData;
import org.apache.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.apache.servicecomb.transport.highway.message.ResponseHeader;

import io.vertx.core.buffer.Buffer;

public final class HighwayCodec {
  private HighwayCodec() {
  }

  public static TcpOutputStream encodeRequest(long msgId, Invocation invocation,
      OperationProtobuf operationProtobuf) throws Exception {
    // 写header
    RequestHeader header = new RequestHeader();
    header.setMsgType(MsgType.REQUEST);
    header.setFlags(0);
    header.setDestMicroservice(invocation.getMicroserviceName());
    header.setSchemaId(invocation.getSchemaId());
    header.setOperationName(invocation.getOperationName());
    header.setContext(invocation.getContext());

    HighwayOutputStream os = new HighwayOutputStream(msgId);
    os.write(header, operationProtobuf.getRequestSchema(), invocation.getArgs());
    return os;
  }

  public static void decodeRequest(Invocation invocation, RequestHeader header, OperationProtobuf operationProtobuf,
      Buffer bodyBuffer) throws Exception {
    WrapSchema schema = operationProtobuf.getRequestSchema();
    Object[] args = schema.readObject(bodyBuffer);

    invocation.setSwaggerArguments(args);
    invocation.setContext(header.getContext());
  }

  public static RequestHeader readRequestHeader(Buffer headerBuffer) throws Exception {
    return RequestHeader.readObject(headerBuffer);
  }

  public static Buffer encodeResponse(long msgId, ResponseHeader header, WrapSchema bodySchema,
      Object body) throws Exception {
    try (HighwayOutputStream os = new HighwayOutputStream(msgId)) {
      os.write(header, bodySchema, body);
      return os.getBuffer();
    }
  }

  public static Response decodeResponse(Invocation invocation, OperationProtobuf operationProtobuf, TcpData tcpData)
      throws Exception {
    ResponseHeader header = ResponseHeader.readObject(tcpData.getHeaderBuffer());
    if (header.getContext() != null) {
      invocation.getContext().putAll(header.getContext());
    }

    WrapSchema bodySchema = operationProtobuf.findResponseSchema(header.getStatusCode());
    Object body = bodySchema.readObject(tcpData.getBodyBuffer());

    Response response = Response.create(header.getStatusCode(), header.getReasonPhrase(), body);
    response.setHeaders(header.getHeaders());

    return response;
  }
}
