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

import static org.apache.servicecomb.core.transport.AbstractTransport.PUBLISH_ADDRESS;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.vertx.core.file.impl.FileResolverImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestRestServlet {
  private RestServlet restservlet = null;

  Environment environment = Mockito.mock(Environment.class);

  @Before
  public void setUp() {
    Mockito.when(environment.getProperty(PUBLISH_ADDRESS, String.class, ""))
        .thenReturn("");
    Mockito.when(environment.getProperty("servicecomb.rest.publishPort", int.class, 0))
        .thenReturn(0);
    Mockito.when(environment.getProperty("servicecomb.transport.eventloop.size", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty(FileResolverImpl.DISABLE_CP_RESOLVING_PROP_NAME, boolean.class, true))
        .thenReturn(true);
    LegacyPropertyFactory.setEnvironment(environment);
    restservlet = new RestServlet();

    SCBBootstrap.createSCBEngineForTest(environment);
  }

  @After
  public void tearDown() {
    restservlet = null;
    SCBEngine.getInstance().destroy();
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
