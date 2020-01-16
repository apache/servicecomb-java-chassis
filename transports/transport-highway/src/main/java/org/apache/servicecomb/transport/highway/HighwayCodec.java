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

import java.util.Map;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.ResponseRootSerializer;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.codec.protobuf.definition.RequestRootDeserializer;
import org.apache.servicecomb.codec.protobuf.definition.ResponseRootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
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
    os.write(header, operationProtobuf.getRequestRootSerializer(),
        invocationArgumentsToSwaggerArguments(invocation, operationProtobuf.getOperationMeta(),
            invocation.getArguments()));
    return os;
  }

  private static Map<String, Object> invocationArgumentsToSwaggerArguments(Invocation invocation,
      OperationMeta operationMeta,
      Map<String, Object> invocationArguments) {
    if (operationMeta.getSwaggerConsumerOperation() != null && !invocation.isEdge()
        && !invocation.isWeakInvoke()) {
      return operationMeta.getSwaggerConsumerOperation().getArgumentsMapper()
          .invocationArgumentToSwaggerArguments(invocation,
              invocation.getArguments());
    } else {
      return invocationArguments;
    }
  }

  private static Map<String, Object> swaggerArgumentsToInvocationArguments(Invocation invocation,
      OperationMeta operationMeta,
      Map<String, Object> swaggerArguments) {
    if (operationMeta.getSwaggerProducerOperation() != null && !invocation.isEdge()) {
      return operationMeta.getSwaggerProducerOperation().getArgumentsMapper()
          .swaggerArgumentToInvocationArguments(invocation, swaggerArguments);
    } else {
      // edge
      return swaggerArguments;
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void decodeRequest(Invocation invocation, RequestHeader header, OperationProtobuf operationProtobuf,
      Buffer bodyBuffer) throws Exception {
    RequestRootDeserializer<Object> requestDesirializer = operationProtobuf.getRequestRootDeserializer();
    Map<String, Object> args = requestDesirializer.deserialize(bodyBuffer.getBytes());
    invocation
        .setArguments(swaggerArgumentsToInvocationArguments(invocation, operationProtobuf.getOperationMeta(), args));
    invocation.mergeContext(header.getContext());
  }

  public static RequestHeader readRequestHeader(Buffer headerBuffer) throws Exception {
    return RequestHeader.readObject(headerBuffer);
  }

  public static Buffer encodeResponse(long msgId, ResponseHeader header, ResponseRootSerializer bodySchema,
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

    ResponseRootDeserializer<Object> bodySchema = operationProtobuf.findResponseRootDeserializer(header.getStatusCode());
    Object body = bodySchema.deserialize(tcpData.getBodyBuffer().getBytes(), invocation.findResponseType(header.getStatusCode()));

    Response response = Response.create(header.getStatusCode(), header.getReasonPhrase(), body);
    response.setHeaders(header.getHeaders());

    return response;
  }
}
