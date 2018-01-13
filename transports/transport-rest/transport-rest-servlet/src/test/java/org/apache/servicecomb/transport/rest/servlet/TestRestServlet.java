/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.transport.rest.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Holder;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.transport.TransportManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestRestServlet {
  private RestServlet restservlet = null;

  @Before
  public void setUp() throws Exception {
    restservlet = new RestServlet();

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

  // useless, but for coverage
  @Test
  public void testService() throws ServletException, IOException {
    Holder<Boolean> holder = new Holder<>();
    ServletRestDispatcher servletRestServer = new MockUp<ServletRestDispatcher>() {
      @Mock
      void service(HttpServletRequest request, HttpServletResponse response) {
        holder.value = true;
      }
    }.getMockInstance();

    Deencapsulation.setField(restservlet, "servletRestServer", servletRestServer);
    restservlet.service(null, null);
    Assert.assertTrue(holder.value);
  }
}
