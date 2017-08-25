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

import io.protostuff.runtime.ProtobufFeature;
import io.servicecomb.codec.protobuf.definition.OperationProtobuf;
import io.servicecomb.codec.protobuf.utils.WrapSchema;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.foundation.vertx.client.tcp.TcpData;
import io.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.transport.highway.message.RequestHeader;
import io.servicecomb.transport.highway.message.ResponseHeader;
import io.vertx.core.buffer.Buffer;

public final class HighwayCodec {
  private static HighwayTransport highwayTransport;

  private HighwayCodec() {
  }

  public static void setHighwayTransport(HighwayTransport highwayTransport) {
    HighwayCodec.highwayTransport = highwayTransport;
  }

  public static TcpOutputStream encodeRequest(long msgId, Invocation invocation,
      OperationProtobuf operationProtobuf, ProtobufFeature protobufFeature) throws Exception {
    // 写header
    RequestHeader header = new RequestHeader();
    header.setMsgType(MsgType.REQUEST);
    header.setFlags(0);
    header.setDestMicroservice(invocation.getMicroserviceName());
    header.setSchemaId(invocation.getSchemaId());
    header.setOperationName(invocation.getOperationName());
    header.setContext(invocation.getContext());

    HighwayOutputStream os = new HighwayOutputStream(msgId, protobufFeature);
    os.write(header, operationProtobuf.getRequestSchema(), invocation.getArgs());
    return os;
  }

  public static Invocation decodeRequest(RequestHeader header, OperationProtobuf operationProtobuf,
      Buffer bodyBuffer, ProtobufFeature protobufFeature) throws Exception {
    WrapSchema schema = operationProtobuf.getRequestSchema();
    Object[] args = schema.readObject(bodyBuffer, protobufFeature);

    Invocation invocation =
        InvocationFactory.forProvider(highwayTransport.getEndpoint(),
            operationProtobuf.getOperationMeta(),
            args);
    invocation.setContext(header.getContext());
    return invocation;
  }

  public static RequestHeader readRequestHeader(Buffer headerBuffer,
      ProtobufFeature protobufFeature) throws Exception {
    return RequestHeader.readObject(headerBuffer, protobufFeature);
  }

  public static Buffer encodeResponse(long msgId, ResponseHeader header, WrapSchema bodySchema,
      Object body, ProtobufFeature protobufFeature) throws Exception {
    try (HighwayOutputStream os = new HighwayOutputStream(msgId, protobufFeature)) {
      os.write(header, bodySchema, body);
      return os.getBuffer();
    }
  }

  public static Response decodeResponse(Invocation invocation, OperationProtobuf operationProtobuf,
      TcpData tcpData, ProtobufFeature protobufFeature) throws Exception {
    ResponseHeader header = ResponseHeader.readObject(tcpData.getHeaderBuffer(), protobufFeature);
    if (header.getContext() != null) {
      invocation.getContext().putAll(header.getContext());
    }

    WrapSchema bodySchema = operationProtobuf.findResponseSchema(header.getStatusCode());
    Object body = bodySchema.readObject(tcpData.getBodyBuffer(), protobufFeature);

    Response response = Response.create(header.getStatusCode(), header.getReasonPhrase(), body);
    response.setHeaders(header.getHeaders());

    return response;
  }
}
