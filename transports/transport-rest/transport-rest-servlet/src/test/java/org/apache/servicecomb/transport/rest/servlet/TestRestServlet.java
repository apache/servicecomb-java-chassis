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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Assertions;

public class TestRestServlet {
  private RestServlet restservlet = null;

  @Before
  public void setUp() {
    ConfigUtil.installDynamicConfig();

    restservlet = new RestServlet();

    SCBBootstrap.createSCBEngineForTest();
  }

  @After
  public void tearDown() {
    restservlet = null;
    SCBEngine.getInstance().destroy();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testInit() throws ServletException {
    restservlet.init();
    Assertions.assertTrue(true);
  }

  // useless, but for coverage
  @Test
  public void testService() {
    Holder<Boolean> holder = new Holder<>();
    ServletRestDispatcher servletRestServer = new MockUp<ServletRestDispatcher>() {
      @Mock
      void service(HttpServletRequest request, HttpServletResponse response) {
        holder.value = true;
      }
    }.getMockInstance();

    Deencapsulation.setField(restservlet, "servletRestServer", servletRestServer);
    restservlet.service(null, null);
    Assertions.assertTrue(holder.value);
  }
}
