/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.vertx.client.tcp;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.foundation.common.net.NetUtils;
import io.vertx.core.Context;
import io.vertx.core.net.NetClient;
import mockit.Mock;
import mockit.MockUp;

public class TcpClientTest {

  private TcpClientConnection instance = null;

  @InjectMocks
  private NetUtils netUtils;

  private void mockTestCases() {

    new MockUp<NetUtils>() {
      @Mock
      public IpPort parseIpPort(String address) {
        return Mockito.mock(IpPort.class);
      }
    };
  }

  @Before
  public void setUp() throws Exception {
    Context context = Mockito.mock(Context.class);
    NetClient netClient = Mockito.mock(NetClient.class);
    InetSocketAddress socketAddress = Mockito.mock(InetSocketAddress.class);
    mockTestCases();
    Mockito.when(NetUtils.parseIpPort("sss").getSocketAddress()).thenReturn(socketAddress);
    instance = new TcpClientConnection(context, netClient, "highway://127.0.0.1:80", new TcpClientConfig());
  }

  @After
  public void tearDown() throws Exception {
    instance = null;
  }

  @Test
  public void testCallBack() {
    instance.getClass();
    TcpResonseCallback callback = Mockito.mock(TcpResonseCallback.class);
    try {
      instance.send(new TcpClientPackage(null), 1, callback);
      Assert.assertNotNull(callback);
    } catch (Exception e) {
      Assert.assertEquals("java.lang.NullPointerException", e.getClass().getName());
    }
  }
}
