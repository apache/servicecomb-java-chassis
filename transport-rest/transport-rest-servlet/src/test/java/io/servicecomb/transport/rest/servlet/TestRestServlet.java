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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.transport.TransportManager;
import io.servicecomb.transport.rest.servlet.RestServlet;
import io.servicecomb.transport.rest.servlet.common.MockUtil;

public class TestRestServlet {
    private RestServlet restservlet = null;

    private HttpServletRequest request = null;

    private HttpServletResponse response = null;

    @Before
    public void setUp() throws Exception {
        restservlet = new RestServlet();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);

        CseContext.getInstance().setTransportManager(Mockito.mock(TransportManager.class));
    }

    @After
    public void tearDown() throws Exception {
        restservlet = null;
    }

    @Test
    public void testInit() throws ServletException {
        restservlet.init();
        Assert.assertTrue(true);
    }

    @Test
    public void testService() {
        boolean status = true;
        try {
            MockUtil.getInstance().mockServletRestServer();
            restservlet.service(request, response);
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);
    }
}
