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

package io.servicecomb.core.provider.consumer;

import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.exception.InvocationException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.Response;
import io.servicecomb.core.definition.OperationMeta;

import mockit.Mock;
import mockit.MockUp;

public class TestInvokerUtils {

    @Test
    public void testSyncInvokeInvocationWithException() throws InterruptedException {
        Invocation invocation = Mockito.mock(Invocation.class);

        Response response = Mockito.mock(Response.class);
        new MockUp<SyncResponseExecutor>() {
            @Mock
            public Response waitResponse() throws InterruptedException {
                return Mockito.mock(Response.class);
            }

        };
        Mockito.when(response.isSuccessed()).thenReturn(true);
        OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
        Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
        Mockito.when(operationMeta.getMicroserviceQualifiedName()).thenReturn("test");

        try {
            InvokerUtils.syncInvoke(invocation);

        } catch (InvocationException e) {
            Assert.assertEquals(490, e.getStatusCode());

        }

    }

    @Test
    public void testReactiveInvoke() {
        Invocation invocation = Mockito.mock(Invocation.class);
        AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
        boolean validAssert;
        try {
            InvokerUtils.reactiveInvoke(invocation, asyncResp);
            validAssert = true;
        } catch (Exception e) {
            validAssert = false;
        }
        Assert.assertTrue(validAssert);

    }

    @Test
    public void testInvokeWithException() {
        new MockUp<SyncResponseExecutor>() {
            @Mock
            public Response waitResponse() throws InterruptedException {
                return Mockito.mock(Response.class);
            }

        };
        Invocation invocation = Mockito.mock(Invocation.class);
        OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
        Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
        Mockito.when(operationMeta.isSync()).thenReturn(true);
        try {
            InvokerUtils.invoke(invocation);

        } catch (InvocationException e) {
            Assert.assertEquals(490, e.getStatusCode());
        }

    }

    @Test
    public void testInvoke() {
        Object[] objectArray = new Object[2];
        Invocation invocation = Mockito.mock(Invocation.class);
        OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
        Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
        Mockito.when(operationMeta.isSync()).thenReturn(false);
        Mockito.when(invocation.getArgs()).thenReturn(objectArray);
        Object obj = InvokerUtils.invoke(invocation);
        Assert.assertNull(obj);
    }
}
