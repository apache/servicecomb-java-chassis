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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.ServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TestRestAsyncListener {

    @Test
    public void testOnTimeout() throws IOException {

        boolean status = true;
        try {
            RestAsyncListener restasynclistener = new RestAsyncListener();
            AsyncEvent event = Mockito.mock(AsyncEvent.class);
            AsyncContext asynccontext = Mockito.mock(AsyncContext.class);
            Mockito.when(event.getAsyncContext()).thenReturn(asynccontext);
            ServletResponse response = Mockito.mock(ServletResponse.class);
            Mockito.when(asynccontext.getResponse()).thenReturn(response);

            PrintWriter out = Mockito.mock(PrintWriter.class);
            Mockito.when(response.getWriter()).thenReturn(out);
            restasynclistener.onTimeout(event);
            restasynclistener.onComplete(event);
            restasynclistener.onError(event);
            restasynclistener.onStartAsync(event);
            Assert.assertNotNull(restasynclistener);

        } catch (Exception e) {
            status = false;
            Assert.assertNotNull(e);
        }
        Assert.assertTrue(status);
    }
}
