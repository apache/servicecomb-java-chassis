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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.codec.protobuf.definition.OperationProtobuf;
import io.servicecomb.codec.protobuf.utils.ProtobufSchemaUtils;
import io.servicecomb.codec.protobuf.utils.WrapSchema;
import com.huawei.paas.cse.core.Const;
import com.huawei.paas.cse.core.Invocation;
import com.huawei.paas.cse.core.Response;
import com.huawei.paas.cse.core.definition.OperationMeta;
import com.huawei.paas.cse.core.definition.SchemaMeta;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class TestGrpcCodec {

    @Test
    public void testDecodeRequest() {
        boolean status = false;
        try {
            RoutingContext routingContext = Mockito.mock(RoutingContext.class);
            OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
            OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);
            WrapSchema schema = ProtobufSchemaUtils.getOrCreateSchema(int.class);
            Mockito.when(operationProtobuf.getRequestSchema()).thenReturn(schema);
            Mockito.when(operationMeta.getExtData("protobuf")).thenReturn(operationProtobuf);
            Buffer bodyBuffer = Mockito.mock(Buffer.class);
            Mockito.when(routingContext.getBody()).thenReturn(bodyBuffer);
            HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
            Mockito.when(routingContext.request()).thenReturn(request);
            SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
            Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
            Mockito.when(request.getHeader(Const.CSE_CONTEXT)).thenReturn("{\"name\":\"test\"}");

            GrpcCodec.setGrpcTransport(new GrpcTransport());
            GrpcCodec.decodeRequest(routingContext, operationMeta);

        } catch (Exception e) {
            status = true;
        }
        Assert.assertFalse(status);

    }

    @Test
    public void testEncodeResponse() {
        boolean status = false;
        try {
            OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);
            WrapSchema schema = ProtobufSchemaUtils.getOrCreateSchema(int.class);
            Mockito.when(operationProtobuf.getRequestSchema()).thenReturn(schema);
            HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
            Mockito.when(request.getHeader(Const.CSE_CONTEXT)).thenReturn("{\"name\":\"test\"}");
            Invocation invocation = Mockito.mock(Invocation.class);
            Response response = Mockito.mock(Response.class);
            Mockito.when(response.getResult()).thenReturn("test");
            Mockito.when(operationProtobuf.getResponseSchema()).thenReturn(Mockito.mock(WrapSchema.class));

            GrpcCodec.encodeResponse(invocation, response, operationProtobuf);
        } catch (Exception e) {
            status = true;
        }
        Assert.assertFalse(status);

    }

    @Test
    public void testDecodeResponse() {
        boolean status = false;
        try {
            RoutingContext routingContext = Mockito.mock(RoutingContext.class);
            OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
            OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);
            WrapSchema schema = ProtobufSchemaUtils.getOrCreateSchema(int.class);
            Mockito.when(operationProtobuf.getRequestSchema()).thenReturn(schema);
            Mockito.when(operationMeta.getExtData("protobuf")).thenReturn(operationProtobuf);
            Buffer bodyBuffer = Mockito.mock(Buffer.class);
            Mockito.when(routingContext.getBody()).thenReturn(bodyBuffer);
            HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
            Mockito.when(routingContext.request()).thenReturn(request);
            Mockito.when(request.getHeader(Const.CSE_CONTEXT)).thenReturn("{\"name\":\"test\"}");
            Invocation invocation = Mockito.mock(Invocation.class);
            Mockito.when(operationProtobuf.getResponseSchema()).thenReturn(Mockito.mock(WrapSchema.class));
            HttpClientResponse httpResponse = Mockito.mock(HttpClientResponse.class);
            Buffer buffer = Mockito.mock(Buffer.class);

            GrpcCodec.decodeResponse(invocation, operationProtobuf, httpResponse, buffer);
        } catch (Exception e) {
            status = true;
        }
        Assert.assertFalse(status);
    }

    @Test
    public void testDecodeResponseEx() {
        boolean status = false;

        OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
        OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);
        WrapSchema schema = ProtobufSchemaUtils.getOrCreateSchema(int.class);
        Mockito.when(operationProtobuf.getRequestSchema()).thenReturn(schema);
        Mockito.when(operationMeta.getExtData("protobuf")).thenReturn(operationProtobuf);
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
        Mockito.when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
        Mockito.when(request.getHeader(Const.CSE_CONTEXT)).thenReturn("{\"name\":\"test\"}");
        Invocation invocation = Mockito.mock(Invocation.class);
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getResult()).thenReturn("test");
        Mockito.when(operationProtobuf.getResponseSchema()).thenReturn(Mockito.mock(WrapSchema.class));
        HttpClientResponse httpResponse = Mockito.mock(HttpClientResponse.class);
        Buffer buffer = Mockito.mock(Buffer.class);
        Mockito.when(httpResponse.statusCode()).thenReturn(200);
        try {
            GrpcCodec.decodeResponse(invocation, operationProtobuf, httpResponse, buffer);
        } catch (Exception e) {
            status = true;
        }
        Assert.assertFalse(status);
    }

}
