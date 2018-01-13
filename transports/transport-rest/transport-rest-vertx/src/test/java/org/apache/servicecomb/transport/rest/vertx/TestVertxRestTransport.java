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

package org.apache.servicecomb.transport.rest.vertx;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

public class TestVertxRestTransport {

  private VertxRestTransport instance = new VertxRestTransport();

  @Test
  public void testGetInstance() {
    Assert.assertNotNull(instance);
  }

  @Test
  public void testGetName() {
    Assert.assertEquals("rest", instance.getName());
  }

  @Test
  public void testInit() {
    boolean status = false;
    try {
      new MockUp<VertxUtils>() {
        @Mock
        public Vertx init(VertxOptions vertxOptions) {
          return null;
        }

        @Mock
        public <VERTICLE extends AbstractVerticle> boolean blockDeploy(Vertx vertx, Class<VERTICLE> cls,
            DeploymentOptions options) throws InterruptedException {
          return true;
        }
      };
      instance.init();
    } catch (Exception e) {
      status = true;
    }
    Assert.assertFalse(status);
  }

  @Test
  public void testSendException() {
    boolean validAssert;
    Invocation invocation = Mockito.mock(Invocation.class);
    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
    URIEndpointObject endpoint = Mockito.mock(URIEndpointObject.class);
    Endpoint end = Mockito.mock(Endpoint.class);
    Mockito.when(invocation.getEndpoint()).thenReturn(end);
    Mockito.when(invocation.getEndpoint().getAddress()).thenReturn(endpoint);
    try {
      validAssert = true;
      instance.send(invocation, asyncResp);
    } catch (Exception e) {

      validAssert = false;
    }
    Assert.assertFalse(validAssert);
  }

  @Test
  public void testGetOrder() {
    VertxRestTransport transport = new VertxRestTransport();
    Assert.assertEquals(-1000, transport.getOrder());
  }

  @Test
  public void testCanInitNullAddress() throws IOException {
    new Expectations(TransportConfig.class) {
      {
        TransportConfig.getAddress();
        result = null;
      }
    };

    VertxRestTransport transport = new VertxRestTransport();
    Assert.assertTrue(transport.canInit());
  }

  @Test
  public void testCanInitListened() throws IOException {
    ServerSocket ss = new ServerSocket(0);
    int port = ss.getLocalPort();

    new Expectations(TransportConfig.class) {
      {
        TransportConfig.getAddress();
        result = "0.0.0.0:" + port;
      }
    };

    VertxRestTransport transport = new VertxRestTransport();
    Assert.assertFalse(transport.canInit());

    ss.close();
  }

  @Test
  public void testCanInitNotListened() throws IOException {
    ServerSocket ss = new ServerSocket(0);
    int port = ss.getLocalPort();
    ss.close();

    new Expectations(TransportConfig.class) {
      {
        TransportConfig.getAddress();
        result = "0.0.0.0:" + port;
      }
    };

    VertxRestTransport transport = new VertxRestTransport();
    Assert.assertTrue(transport.canInit());
  }
}
