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

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.RequestRootSerializer;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpClientConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import io.vertx.core.file.impl.FileResolverImpl;

public class TestHighwayTransport {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestHighwayTransport.class);

  Environment environment = Mockito.mock(Environment.class);

  @BeforeClass
  public static void setup() {
    VertxUtils.blockCloseVertxByName("transport");
    Thread.getAllStackTraces().keySet().forEach(t -> LOGGER.info("before: {}", t.getName()));
  }

  @AfterClass
  public static void teardown() {
    VertxUtils.blockCloseVertxByName("transport");
    Thread.getAllStackTraces().keySet().forEach(t -> LOGGER.info("after: {}", t.getName()));
  }

  @Before
  public void setUp() {
    Mockito.when(environment.getProperty(
            "servicecomb.request.timeout", long.class, (long) TcpClientConfig.DEFAULT_LOGIN_TIMEOUT))
        .thenReturn((long) TcpClientConfig.DEFAULT_LOGIN_TIMEOUT);
    Mockito.when(environment.getProperty("servicecomb.highway.client.verticle-count", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty("servicecomb.highway.client.thread-count", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty("servicecomb.highway.server.verticle-count", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty("servicecomb.highway.server.thread-count", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty("servicecomb.transport.eventloop.size", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty(FileResolverImpl.DISABLE_CP_RESOLVING_PROP_NAME, boolean.class, true))
        .thenReturn(true);
    LegacyPropertyFactory.setEnvironment(environment);
  }

  @Test
  public void testGetInstance() {
    HighwayTransport transport = new HighwayTransport();
    Assertions.assertNotNull(transport);
  }

  @Test
  public void testInit() {
    HighwayTransport transport = new HighwayTransport();
    transport.setEnvironment(environment);
    boolean status = true;
    try {
      transport.init();
    } catch (Exception e) {
      e.printStackTrace();
      status = false;
    }

    Assertions.assertTrue(status);
  }

  @Test
  public void testHighway() {
    HighwayTransport transport = new HighwayTransport();
    Invocation invocation = Mockito.mock(Invocation.class);
    commonHighwayMock(invocation);
    Assertions.assertEquals("highway", transport.getName());
  }

  private void commonHighwayMock(Invocation invocation) {
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);
    Mockito.when(operationMeta.getExtData("protobuf")).thenReturn(operationProtobuf);
    Endpoint lEndpoint = Mockito.mock(Endpoint.class);
    Mockito.when(invocation.getEndpoint()).thenReturn(lEndpoint);
    RequestRootSerializer lWrapSchema = Mockito.mock(RequestRootSerializer.class);
    Mockito.when(operationProtobuf.getRequestRootSerializer()).thenReturn(lWrapSchema);
    URIEndpointObject ep = Mockito.mock(URIEndpointObject.class);
    Mockito.when(lEndpoint.getAddress()).thenReturn(ep);
    Mockito.when(ep.getHostOrIp()).thenReturn("127.0.0.1");
    Mockito.when(ep.getPort()).thenReturn(80);
  }
}
