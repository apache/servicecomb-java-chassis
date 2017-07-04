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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestRestServletHttpRequest {

    private RestServletHttpRequest lrequest = null;

    @Before
    public void setUp() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        AsyncContext asyncCtx = Mockito.mock(AsyncContext.class);
        lrequest = new RestServletHttpRequest(request, asyncCtx);
    }

    @After
    public void tearDown() throws Exception {
        lrequest = null;
    }

    @Test
    public void testGetPath() {
        Assert.assertNull(lrequest.getPath());
    }

    @Test
    public void testGetMethod() {
        Assert.assertNull(lrequest.getMethod());
    }

    @Test
    public void testComplete() {
        boolean status = true;
        try {
            lrequest.complete();
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testSetPathParamMap() {
        Map<String, String> pathParamMap = new HashMap<String, String>();
        boolean status = true;
        try {
            lrequest.setPathParamMap(pathParamMap);
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);

    }

    @Test
    public void testGetQueryParam() {
        Assert.assertNull(lrequest.getQueryParam("key"));
    }

    @Test
    public void testGetPathParam() {
        boolean status = false;
        try {
            Assert.assertNull(lrequest.getPathParam("key"));
        } catch (Exception ex) {
            status = true;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testGetHeaderParam() {
        Assert.assertNull(lrequest.getHeaderParam("key"));
    }

    @Test
    public void testGetFormParam() {
        Assert.assertNull(lrequest.getFormParam("key"));
    }

    @Test
    public void testGetBody() {
        boolean status = true;
        try {
            Assert.assertNull(lrequest.getBody());
        } catch (IOException e) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testGetQueryParams() {
        Assert.assertNotNull(lrequest.getQueryParams());
    }

    @Test
    public void testGetHttpRequest() {
        Assert.assertNotNull(lrequest.getHttpRequest());
    }

}
