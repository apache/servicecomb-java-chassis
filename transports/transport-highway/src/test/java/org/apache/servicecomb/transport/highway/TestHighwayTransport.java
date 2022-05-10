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
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mockit.Mock;
import mockit.MockUp;

public class TestHighwayTransport {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestHighwayTransport.class);

  private final HighwayTransport transport = new HighwayTransport();

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

  @Test
  public void testGetInstance() {
    Assertions.assertNotNull(transport);
  }

  @Test
  public void testInit() {
    boolean status = true;
    try {
      transport.init();
    } catch (Exception e) {
      status = false;
    }

    Assertions.assertTrue(status);
  }

  @Test
  public void testSendException() throws Exception {
    Invocation invocation = Mockito.mock(Invocation.class);
    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
    commonHighwayMock(invocation);

    Holder<Boolean> sended = new Holder<>(false);
    new MockUp<HighwayClient>() {
      @Mock
      public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
        sended.value = true;
      }
    };
    transport.send(invocation, asyncResp);
    Assertions.assertTrue(sended.value);
  }

  @Test
  public void testHighway() {
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
