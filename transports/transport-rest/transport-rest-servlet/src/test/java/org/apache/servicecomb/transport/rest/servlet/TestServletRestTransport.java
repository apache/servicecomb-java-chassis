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
import java.net.ServerSocket;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.transport.rest.client.RestTransportClient;
import org.apache.servicecomb.transport.rest.client.RestTransportClientManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestServletRestTransport {
  ServletRestTransport transport = new ServletRestTransport();

  @After
  public void tearDown() {
    SCBEngine.clearClassLoaderScopeProperty();
  }

  @Test
  public void testInitNotPublish(@Mocked RestTransportClient restTransportClient) {
    new MockUp<RestTransportClientManager>() {
      @Mock
      public RestTransportClient getRestTransportClient(boolean sslEnabled) {
        return restTransportClient;
      }
    };

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = null;
      }
    };
    Assert.assertTrue(transport.init());
    Assert.assertNull(transport.getPublishEndpoint());
  }

  @Test
  public void testInitPublishNoUrlPrefix(@Mocked RestTransportClient restTransportClient) {
    new MockUp<RestTransportClientManager>() {
      @Mock
      public RestTransportClient getRestTransportClient(boolean sslEnabled) {
        return restTransportClient;
      }
    };

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = "1.1.1.1:1234";
      }
    };
    Assert.assertTrue(transport.init());
    Assert.assertEquals("rest://1.1.1.1:1234", transport.getPublishEndpoint().getEndpoint());
  }

  @Test
  public void testInitPublishWithUrlPrefix(@Mocked RestTransportClient restTransportClient) {

    new MockUp<RestTransportClientManager>() {
      @Mock
      public RestTransportClient getRestTransportClient(boolean sslEnabled) {
        return restTransportClient;
      }
    };

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = "1.1.1.1:1234";
      }
    };
    SCBEngine.setClassLoaderScopeProperty(DefinitionConst.URL_PREFIX, "/root");

    Assert.assertTrue(transport.init());
    Assert.assertEquals("rest://1.1.1.1:1234?urlPrefix=/root", transport.getPublishEndpoint().getEndpoint());
  }

  @Test
  public void testGetOrder() {
    ServletRestTransport transport = new ServletRestTransport();
    Assert.assertEquals(0, transport.getOrder());
  }

  @Test
  public void testCanInitNullAddress() throws IOException {
    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = null;
      }
    };

    ServletRestTransport transport = new ServletRestTransport();
    Assert.assertTrue(transport.canInit());
  }

  @Test
  public void testCanInitListened() throws IOException {
    ServerSocket ss = new ServerSocket(0);
    int port = ss.getLocalPort();

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = "0.0.0.0:" + port;
      }
    };

    ServletRestTransport transport = new ServletRestTransport();
    Assert.assertTrue(transport.canInit());

    ss.close();
  }

  @Test
  public void testCanInitNotListened() throws IOException {
    ServerSocket ss = new ServerSocket(0);
    int port = ss.getLocalPort();
    ss.close();

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = "0.0.0.0:" + port;
      }
    };

    ServletRestTransport transport = new ServletRestTransport();
    Assert.assertFalse(transport.canInit());
  }
}
