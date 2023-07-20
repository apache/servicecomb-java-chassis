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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.RequestRootDeserializer;
import org.apache.servicecomb.codec.protobuf.definition.ResponseRootDeserializer;
import org.apache.servicecomb.codec.protobuf.definition.ResponseRootSerializer;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpData;
import org.apache.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.apache.servicecomb.transport.highway.message.ResponseHeader;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Defaults;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.vertx.core.buffer.Buffer;

@SuppressWarnings("rawtypes")
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
    os.write(header, operationProtobuf.getRequestRootSerializer(), invocation.getSwaggerArguments());
    return os;
  }

  // Proto buffer never serialize default values, put it back in provider.
  // Or will get IllegalArgumentsException for primitive types.
  private static Map<String, Object> addPrimitiveTypeDefaultValues(Invocation invocation,
      Map<String, Object> swaggerArguments) {
    if (invocation.getOperationMeta().getSwaggerProducerOperation() != null && !invocation.isEdge()) {
      List<Parameter> swaggerParameters = invocation.getOperationMeta().getSwaggerOperation()
          .getParameters();
      if (swaggerParameters != null) {
        for (Parameter parameter : swaggerParameters) {
          if (swaggerArguments.get(parameter.getName()) == null) {
            Type type = invocation.getOperationMeta().getSwaggerProducerOperation()
                .getSwaggerParameterType(parameter.getName());
            swaggerArguments.put(parameter.getName(), defaultPrimitiveValue(null, type));
          }
        }
      }

      RequestBody requestBody = invocation.getOperationMeta().getSwaggerOperation().getRequestBody();
      if (requestBody != null && requestBody.getContent() != null
          && requestBody.getContent().get(SwaggerConst.FORM_MEDIA_TYPE) != null
          && requestBody.getContent().get(SwaggerConst.FORM_MEDIA_TYPE).getSchema() != null
          && requestBody.getContent().get(SwaggerConst.FORM_MEDIA_TYPE).getSchema().getProperties() != null) {
        for (Object entry :
            requestBody.getContent().get(SwaggerConst.FORM_MEDIA_TYPE).getSchema().getProperties().entrySet()) {
          Entry<String, Schema> types = (Entry<String, Schema>) entry;
          if (swaggerArguments.get(types.getKey()) == null) {
            Type type = invocation.getOperationMeta().getSwaggerProducerOperation()
                .getSwaggerParameterType(types.getKey());
            swaggerArguments.put(types.getKey(), defaultPrimitiveValue(null, type));
          }
        }
      }
    }
    return swaggerArguments;
  }

  public static void decodeRequest(Invocation invocation, RequestHeader header, OperationProtobuf operationProtobuf,
      Buffer bodyBuffer) throws Exception {
    RequestRootDeserializer<Object> requestDeserializer = operationProtobuf.getRequestRootDeserializer();
    Map<String, Object> swaggerArguments = requestDeserializer.deserialize(bodyBuffer.getBytes());
    addPrimitiveTypeDefaultValues(invocation, swaggerArguments);
    invocation.setSwaggerArguments(swaggerArguments);
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

    ResponseRootDeserializer<Object> bodySchema = operationProtobuf
        .findResponseRootDeserializer(header.getStatusCode());
    JavaType type = invocation.findResponseType(header.getStatusCode());
    Object body = bodySchema
        .deserialize(tcpData.getBodyBuffer().getBytes(), type);

    Response response = Response.create(header.getStatusCode(), header.getReasonPhrase()
        , defaultPrimitiveValue(body, type));
    response.setHeaders(header.toMultiMap());

    return response;
  }

  private static Object defaultPrimitiveValue(Object body, Type type) {
    if (body == null) {
      if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
        return Defaults.defaultValue((Class<?>) type);
      }
      if (type instanceof JavaType && ((JavaType) type).isPrimitive()) {
        return Defaults.defaultValue(((JavaType) type).getRawClass());
      }
    }
    return body;
  }
}
