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

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.utils.ClassLoaderScopeContext;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.vertx.core.file.impl.FileResolverImpl;
import mockit.Expectations;

public class TestServletRestTransport {
  ServletRestTransport transport;

  Environment environment = Mockito.mock(Environment.class);

  @Before
  public void setUp() {
    Mockito.when(environment.getProperty(PUBLISH_ADDRESS, ""))
        .thenReturn("");
    Mockito.when(environment.getProperty("servicecomb.rest.publishPort", int.class, 0))
        .thenReturn(0);
    Mockito.when(environment.getProperty("servicecomb.transport.eventloop.size", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty(FileResolverImpl.DISABLE_CP_RESOLVING_PROP_NAME, boolean.class, true))
        .thenReturn(true);
    LegacyPropertyFactory.setEnvironment(environment);
    transport = new ServletRestTransport();
    transport.setEnvironment(environment);
  }

  @After
  public void tearDown() {
    ClassLoaderScopeContext.clearClassLoaderScopeProperty();
  }

  @Test
  public void testInitNotPublish() {
    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress(environment);
        result = null;
      }
    };
    Assertions.assertTrue(transport.init());
    Assertions.assertNull(transport.getPublishEndpoint());
  }

  @Test
  public void testInitPublishNoUrlPrefix() {
    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress(environment);
        result = "1.1.1.1:1234";
      }
    };
    Assertions.assertTrue(transport.init());
    Assertions.assertEquals("rest://1.1.1.1:1234", transport.getPublishEndpoint().getEndpoint());
  }

  @Test
  public void testInitPublishWithUrlPrefix() {
    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress(environment);
        result = "1.1.1.1:1234";
      }
    };
    ClassLoaderScopeContext.setClassLoaderScopeProperty(DefinitionConst.URL_PREFIX, "/root");

    Assertions.assertTrue(transport.init());
    Assertions.assertEquals("rest://1.1.1.1:1234?urlPrefix=%2Froot", transport.getPublishEndpoint().getEndpoint());
  }

  @Test
  public void testGetOrder() {
    ServletRestTransport transport = new ServletRestTransport();
    Assertions.assertEquals(0, transport.getOrder());
  }

  @Test
  public void testCanInitNullAddress() throws IOException {
    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress(environment);
        result = null;
      }
    };

    ServletRestTransport transport = new ServletRestTransport();
    transport.setEnvironment(environment);
    Assertions.assertTrue(transport.canInit());
  }

  @Test
  public void testCanInitListened() throws IOException {
    ServerSocket ss = new ServerSocket(0);
    int port = ss.getLocalPort();

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress(environment);
        result = "0.0.0.0:" + port;
      }
    };

    ServletRestTransport transport = new ServletRestTransport();
    transport.setEnvironment(environment);
    Assertions.assertTrue(transport.canInit());

    ss.close();
  }

  @Test
  public void testCanInitNotListened() throws IOException {
    ServerSocket ss = new ServerSocket(0);
    int port = ss.getLocalPort();
    ss.close();

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress(environment);
        result = "0.0.0.0:" + port;
      }
    };

    ServletRestTransport transport = new ServletRestTransport();
    transport.setEnvironment(environment);
    Assertions.assertFalse(transport.canInit());
  }
}
