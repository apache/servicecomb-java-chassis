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
import io.servicecomb.codec.protobuf.utils.WrapSchema;
import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.Endpoint;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.foundation.vertx.client.ClientPoolManager;
import io.servicecomb.foundation.vertx.client.http.HttpClientWithContext;

import mockit.Mock;
import mockit.MockUp;

public class TestGrpcTransport {

    private GrpcTransport transport = new GrpcTransport();

    @Test
    public void testInit() {
        boolean status = false;
        try {
            transport.init();
        } catch (Exception e) {
            status = true;
        }
        Assert.assertFalse(status);
    }

    @Test
    public void testSend() {
        boolean status = false;
        Invocation invocation = Mockito.mock(Invocation.class);
        AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
        OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
        Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
        OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);
        Mockito.when(operationMeta.getExtData("protobuf")).thenReturn(operationProtobuf);
        Endpoint lEndpoint = Mockito.mock(Endpoint.class);
        Mockito.when(invocation.getEndpoint()).thenReturn(lEndpoint);
        WrapSchema lWrapSchema = Mockito.mock(WrapSchema.class);
        Mockito.when(operationProtobuf.getRequestSchema()).thenReturn(lWrapSchema);
        IpPort ipPort = Mockito.mock(IpPort.class);
        Mockito.when(lEndpoint.getAddress()).thenReturn(ipPort);
        Mockito.when(ipPort.getHostOrIp()).thenReturn("127.0.0.1");
        Mockito.when(ipPort.getPort()).thenReturn(80);

        new MockUp<ClientPoolManager<HttpClientWithContext>>() {
            @Mock
            public HttpClientWithContext findThreadBindClientPool() {
                return Mockito.mock(HttpClientWithContext.class);
            }
        };

        try {
            transport.send(invocation, asyncResp);
        } catch (Exception e) {
            status = true;
        }
        Assert.assertFalse(status);
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("grpc", transport.getName());
    }

}
