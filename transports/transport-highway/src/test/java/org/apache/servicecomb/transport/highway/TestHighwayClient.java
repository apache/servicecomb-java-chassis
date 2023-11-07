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

package org.apache.servicecomb.transport.highway;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpClientConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestHighwayClient {
  private static final String REQUEST_TIMEOUT_KEY = "servicecomb.request.timeout";

  HighwayClient client = new HighwayClient();

  Environment environment = Mockito.mock(Environment.class);

  static long nanoTime = 123;

  @BeforeClass
  public static void setup() {
    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return nanoTime;
      }
    };
  }

  @AfterClass
  public static void teardown() {

  }

  @Before
  public void setUp() {
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty(REQUEST_TIMEOUT_KEY, long.class, (long) TcpClientConfig.DEFAULT_LOGIN_TIMEOUT))
        .thenReturn((long) 2000);
    Mockito.when(environment.getProperty("servicecomb.highway.client.verticle-count", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty("servicecomb.highway.client.thread-count", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty("servicecomb.highway.server.verticle-count", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty("servicecomb.highway.server.thread-count", int.class, -1))
        .thenReturn(-1);
  }

  @Test
  public void testLoginTimeout(@Mocked Vertx vertx) {
    TcpClientConfig tcpClientConfig = client.createTcpClientConfig();
    Assertions.assertEquals(2000, tcpClientConfig.getMsLoginTimeout());
  }

  @Test
  public void testHighwayClientSSL(@Mocked Vertx vertx) throws Exception {
    new MockUp<VertxUtils>() {
      @Mock
      <VERTICLE extends AbstractVerticle> boolean blockDeploy(Vertx vertx,
          Class<VERTICLE> cls,
          DeploymentOptions options) {
        return true;
      }
    };

    client.init(vertx);

    ClientPoolManager<HighwayClientConnectionPool> clientMgr = Deencapsulation.getField(client, "clientMgr");
    Assertions.assertSame(vertx, Deencapsulation.getField(clientMgr, "vertx"));
  }
}
