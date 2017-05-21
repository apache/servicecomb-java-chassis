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

package com.huawei.paas.cse.transport.rest.servlet;

import static org.junit.Assert.assertEquals;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.common.rest.codec.RestServerRequestInternal;
import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import com.huawei.paas.cse.core.Invocation;
import com.huawei.paas.cse.core.Response;
import com.huawei.paas.cse.core.definition.OperationMeta;
import com.huawei.paas.cse.transport.rest.servlet.common.MockUtil;

public class TestServletRestServer {

    private ServletRestServer servletRestServer = null;

    private HttpServletRequest request = null;

    private HttpServletResponse response = null;

    private AsyncContext asyncCtx = null;

    private RestOperationMeta restOperation = null;

    private OperationMeta operationMeta = null;

    @Before
    public void setUp() throws Exception {
        servletRestServer = new ServletRestServer();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);

        asyncCtx = Mockito.mock(AsyncContext.class);
        restOperation = Mockito.mock(RestOperationMeta.class);
        operationMeta = Mockito.mock(OperationMeta.class);
        Mockito.when(request.startAsync()).thenReturn(asyncCtx);
        Mockito.when(restOperation.getOperationMeta()).thenReturn(operationMeta);

    }

    @After
    public void tearDown() throws Exception {
        servletRestServer = null;
    }

    @Test
    public void testServiceException() {
        boolean status = true;
        try {
            MockUtil.getInstance().mockAbstactRestServer();
            servletRestServer.service(request, response);
        } catch (Exception exce) {
            Assert.assertNotNull(exce);
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testSetHttpRequestContext() {
        boolean status = true;
        try {
            Invocation invocation = Mockito.mock(Invocation.class);
            RestServerRequestInternal restRequest = Mockito.mock(RestServerRequestInternal.class);
            servletRestServer.setHttpRequestContext(invocation, restRequest);
        } catch (Exception exce) {
            Assert.assertNotNull(exce);
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testDoSendResponse() {
        boolean status = true;
        HttpServletResponse httpServerResponse = Mockito.mock(HttpServletResponse.class);
        ProduceProcessor produceProcessor = Mockito.mock(ProduceProcessor.class);
        Object result = Mockito.mock(Object.class);
        Mockito.when(produceProcessor.getName()).thenReturn("testCall");
        assertEquals("testCall", produceProcessor.getName());
        try {
            Response response = Response.create(12, "gjhghjgk", result);
            servletRestServer.doSendResponse(httpServerResponse, produceProcessor, response);
        } catch (Exception exce) {
            Assert.assertNotNull(exce);
            status = false;
        }
        Assert.assertTrue(status);
    }
}
