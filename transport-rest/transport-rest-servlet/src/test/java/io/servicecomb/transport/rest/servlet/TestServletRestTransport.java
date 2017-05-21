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

package io.servicecomb.transport.rest.servlet;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.Endpoint;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.common.net.URIEndpointObject;

public class TestServletRestTransport {
    ServletRestTransport transport = new ServletRestTransport();

    @Test
    public void testInit() {
        boolean status = true;
        try {
            transport.init();
        } catch (Exception exce) {
            Assert.assertNotNull(exce);
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testSendException() {
        boolean status = true;
        Invocation invocation = Mockito.mock(Invocation.class);
        AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
        URIEndpointObject endpoint = Mockito.mock(URIEndpointObject.class);
        Endpoint endpoint1 = Mockito.mock(Endpoint.class);
        Mockito.when(invocation.getEndpoint()).thenReturn(endpoint1);
        Mockito.when(invocation.getEndpoint().getAddress()).thenReturn(endpoint);
        try {
            transport.send(invocation, asyncResp);
        } catch (Exception exce) {
            Assert.assertNotNull(exce);
            status = false;
        }
        Assert.assertFalse(status);
    }
}
