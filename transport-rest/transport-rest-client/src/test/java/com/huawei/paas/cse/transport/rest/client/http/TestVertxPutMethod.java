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

package com.huawei.paas.cse.transport.rest.client.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.common.net.IpPort;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;

public class TestVertxPutMethod {

    @Test
    public void testVertxPutMethod() {

        HttpClient client = Mockito.mock(HttpClient.class);
        Invocation invocation = Mockito.mock(Invocation.class);
        IpPort ipPort = Mockito.mock(IpPort.class);
        RestOperationMeta operation = Mockito.mock(RestOperationMeta.class);
        AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
        Mockito.when(ipPort.getHostOrIp()).thenReturn("test");
        assertNotNull("test", ipPort.getHostOrIp());
        Mockito.when(ipPort.getPort()).thenReturn(13);
        assertEquals(13, ipPort.getPort());
        HttpClientRequest obj =
            VertxPutMethod.INSTANCE.createRequest(client, invocation, ipPort, "testCall", operation, asyncResp);
        Assert.assertNull(obj);

    }
}
