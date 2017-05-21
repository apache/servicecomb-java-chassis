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

package io.servicecomb.common.rest;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.common.rest.codec.RestServerRequestInternal;
import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Response;
import io.servicecomb.core.Transport;
import io.servicecomb.foundation.common.utils.JsonUtils;

/**
 * <一句话功能简述> <功能详细描述>
 * 
 * @author  
 * @version [版本号, 2017年3月1日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class TestAbstractRestServer {
    private Invocation invocation;

    private RestServerRequestInternal restRequest;

    private AbstractRestServer<Response> restServer;

    @Before
    public void before() throws Exception {
        invocation = Mockito.mock(Invocation.class);
        restRequest = Mockito.mock(RestServerRequestInternal.class);
        restServer = new AbstractRestServer<Response>() {
            @Override
            protected void doSendResponse(Response httpServerResponse, ProduceProcessor produceProcessor,
                    Response response) throws Exception {
            }

            @Override
            protected void setHttpRequestContext(Invocation invocation, RestServerRequestInternal restRequest) {
            }
        };
        restServer.setTransport(Mockito.mock(Transport.class));
    }

    @After
    public void after() {

        invocation = null;
        restRequest = null;
        restServer = null;
    }

    @Test
    public void testSetContext() throws Exception {
        boolean status = true;
        try {
            Map<String, String> contextMap = new HashMap<>();
            contextMap.put("abc", "abc");
            String strContext = JsonUtils.writeValueAsString(contextMap);
            when(restRequest.getHeaderParam(Const.CSE_CONTEXT)).thenReturn(strContext);
            restServer.setContext(invocation, restRequest);
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testFindRestOperationException() {
        boolean status = true;
        try {
            when(restRequest.getPath()).thenReturn("/a/b/c");
            when(restRequest.getMethod()).thenReturn("POST");
            restServer.findRestOperation(restRequest);
        } catch (Throwable e) {
            status = false;
        }
        Assert.assertFalse(status);
    }
}
