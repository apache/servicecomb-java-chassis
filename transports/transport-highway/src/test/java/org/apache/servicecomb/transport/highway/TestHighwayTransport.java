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

import javax.xml.ws.Holder;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.utils.WrapSchema;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Mock;
import mockit.MockUp;

public class TestHighwayTransport {

  private HighwayTransport transport = new HighwayTransport();

  @Test
  public void testGetInstance() {
    Assert.assertNotNull(transport);
  }

  @Test
  public void testInit() {
    boolean status = true;
    try {
      transport.init();
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);
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
    Assert.assertTrue(sended.value);
  }

  @Test
  public void testHighway() {
    Invocation invocation = Mockito.mock(Invocation.class);
    commonHighwayMock(invocation);
    Assert.assertEquals("highway", transport.getName());
  }

  private void commonHighwayMock(Invocation invocation) {
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    OperationProtobuf operationProtobuf = Mockito.mock(OperationProtobuf.class);
    Mockito.when(operationMeta.getExtData("protobuf")).thenReturn(operationProtobuf);
    Endpoint lEndpoint = Mockito.mock(Endpoint.class);
    Mockito.when(invocation.getEndpoint()).thenReturn(lEndpoint);
    WrapSchema lWrapSchema = Mockito.mock(WrapSchema.class);
    Mockito.when(operationProtobuf.getRequestSchema()).thenReturn(lWrapSchema);
    URIEndpointObject ep = Mockito.mock(URIEndpointObject.class);
    Mockito.when(lEndpoint.getAddress()).thenReturn(ep);
    Mockito.when(ep.getHostOrIp()).thenReturn("127.0.0.1");
    Mockito.when(ep.getPort()).thenReturn(80);
  }
}
