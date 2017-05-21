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

package com.huawei.paas.cse.transport.grpc;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.ws.rs.core.Response.Status.Family;

import io.servicecomb.codec.protobuf.definition.OperationProtobuf;
import io.servicecomb.codec.protobuf.definition.ProtobufManager;
import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Response;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.invocation.InvocationFactory;
import com.huawei.paas.foundation.common.utils.JsonUtils;
import com.huawei.paas.foundation.vertx.stream.BufferOutputStream;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufOutput;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年2月9日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public final class GrpcCodec {
    private static final int BUFFER_SIZE = 1024;

    private static GrpcTransport grpcTransport;

    public static void setGrpcTransport(GrpcTransport grpcTransport) {
        GrpcCodec.grpcTransport = grpcTransport;
    }

    private GrpcCodec() {

    }

    public static Buffer encodeRequest(Invocation invocation, OperationProtobuf operationProtobuf) throws Exception {
        try (BufferOutputStream os = new BufferOutputStream()) {
            // 写protobuf数据
            LinkedBuffer linkedBuffer = LinkedBuffer.allocate(BUFFER_SIZE);
            ProtobufOutput output = new ProtobufOutput(linkedBuffer);
            operationProtobuf.getRequestSchema().writeObject(output, invocation.getArgs());

            os.write(0);
            // protobuf输出到流
            LinkedBuffer.writeTo(os, linkedBuffer);

            return os.getBuffer();
        }
    }

    public static Invocation decodeRequest(RoutingContext routingContext,
            OperationMeta operationMeta) throws Exception {
        OperationProtobuf operationProtobuf = ProtobufManager.getOrCreateOperation(operationMeta);

        Buffer bodyBuffer = routingContext.getBody();
        Buffer protoBuffer = bodyBuffer.slice(1, bodyBuffer.length());

        Object[] args = operationProtobuf.getRequestSchema().readObject(protoBuffer);

        Invocation invocation =
            InvocationFactory.forProvider(grpcTransport.getEndpoint(),
                    operationMeta,
                    args);

        String strContext = routingContext.request().getHeader(Const.CSE_CONTEXT);
        @SuppressWarnings("unchecked")
        Map<String, String> context = JsonUtils.readValue(strContext.getBytes(StandardCharsets.UTF_8), Map.class);
        invocation.setContext(context);

        return invocation;
    }

    public static Buffer encodeResponse(Invocation invocation, Response response,
            OperationProtobuf operationProtobuf) throws Exception {
        if (response.isFailed()) {
            throw new Exception("not impl");
        }

        try (BufferOutputStream os = new BufferOutputStream()) {
            os.write(0);

            if (response.getResult() != null) {
                // 写protobuf数据
                LinkedBuffer linkedBuffer = LinkedBuffer.allocate(BUFFER_SIZE);
                ProtobufOutput output = new ProtobufOutput(linkedBuffer);

                operationProtobuf.getResponseSchema().writeObject(output, response.getResult());

                // protobuf输出到流
                LinkedBuffer.writeTo(os, linkedBuffer);
            }

            return os.getBuffer();
        }
    }

    public static Response decodeResponse(Invocation invocation, OperationProtobuf operationProtobuf,
            HttpClientResponse httpResponse,
            Buffer buffer) throws Exception {
        // TODO:grpc的错误信息不在标准http status code上体现，而是有自己的code
        Family family = Family.familyOf(httpResponse.statusCode());
        boolean success = Family.SUCCESSFUL.equals(family);

        if (success) {
            return decodeSuccessResponse(invocation, operationProtobuf, buffer);
        }

        // 异常分支
        return null;
    }

    protected static Response decodeSuccessResponse(Invocation invocation, OperationProtobuf operationProtobuf,
            Buffer buffer) throws Exception {
        Buffer protoBuffer = buffer.slice(1, buffer.length());

        Object result = operationProtobuf.getResponseSchema().readObject(protoBuffer);
        return Response.succResp(result);
    }
}
