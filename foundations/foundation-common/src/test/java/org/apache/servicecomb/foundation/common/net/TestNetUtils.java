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

package org.apache.servicecomb.foundation.common.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Deencapsulation;

public class TestNetUtils {
  @Test
  public void testIpPort() {
    IpPort oIPPort = new IpPort("10.145.154.45", 8080);
    Assert.assertEquals("10.145.154.45", oIPPort.getHostOrIp());
    Assert.assertEquals(8080, oIPPort.getPort());
    oIPPort.setPort(9090);
    Assert.assertEquals(9090, oIPPort.getPort());
    Assert.assertNotEquals(null, oIPPort.getSocketAddress());
  }

  @Test
  public void testNetutils() {
    Assert.assertEquals("127.0.0.1", NetUtils.parseIpPort("127.0.0.1:8080").getHostOrIp());
    Assert.assertEquals(8080, NetUtils.parseIpPort("127.0.0.1:8080").getPort());
    Assert.assertEquals(null, NetUtils.parseIpPort(null));
    Assert.assertEquals(null, NetUtils.parseIpPort("127.0.0.18080"));
    Assert.assertEquals(null, NetUtils.parseIpPortFromURI(null));
    Assert.assertEquals(null, NetUtils.parseIpPortFromURI("ss"));
    Assert.assertEquals("127.0.0.1", NetUtils.parseIpPortFromURI("rest://127.0.0.1:8080").getHostOrIp());
    Assert.assertEquals(8080, NetUtils.parseIpPortFromURI("http://127.0.0.1:8080").getPort());
    Assert.assertEquals(80, NetUtils.parseIpPortFromURI("http://127.0.0.1").getPort());
    Assert.assertEquals(8080, NetUtils.parseIpPortFromURI("https://127.0.0.1:8080").getPort());
    Assert.assertEquals(443, NetUtils.parseIpPortFromURI("https://127.0.0.1").getPort());
  }

  @Test
  public void testFullOperation() {
    Assert.assertNotNull(NetUtils.getHostAddress());
    Assert.assertNotNull(NetUtils.getHostName());
  }

  @Test
  public void testGetRealListenAddress() {
    Assert.assertNull(NetUtils.getRealListenAddress("http", null));
    Assert.assertNull(NetUtils.getRealListenAddress("http:1", "1.1.1.1:8080"));
    Assert.assertEquals("http://1.1.1.1:8080", NetUtils.getRealListenAddress("http", "1.1.1.1:8080"));
  }

  @Test
  public void testNetworkInterface() {
    Map<String, InetAddress> org = Deencapsulation.getField(NetUtils.class, "allInterfaceAddresses");

    Map<String, InetAddress> newValue = new HashMap<>();
    InetAddress addr = Mockito.mock(InetAddress.class);
    newValue.put("eth100", addr);
    Deencapsulation.setField(NetUtils.class, "allInterfaceAddresses", newValue);

    Assert.assertEquals(addr, NetUtils.getInterfaceAddress("eth100"));
    Assert.assertEquals(addr, NetUtils.ensureGetInterfaceAddress("eth100"));

    try {
      NetUtils.ensureGetInterfaceAddress("xxx");
      Assert.fail("must throw exception");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Can not find address for interface name: xxx", e.getMessage());
    }

    Deencapsulation.setField(NetUtils.class, "allInterfaceAddresses", org);
  }

  @Test
  public void testCanTcpListenNo() throws IOException {
    InetAddress address = InetAddress.getByName("127.0.0.1");
    try (ServerSocket ss = new ServerSocket(0, 0, address)) {
      Assert.assertFalse(NetUtils.canTcpListen(address, ss.getLocalPort()));
    }
  }

  @Test
  public void testCanTcpListenYes() throws IOException {
    InetAddress address = InetAddress.getByName("127.0.0.1");
    ServerSocket ss = new ServerSocket(0, 0, address);
    int port = ss.getLocalPort();
    ss.close();

    Assert.assertTrue(NetUtils.canTcpListen(address, port));
  }

  @Test
  public void testGetHostName() {
    Assert.assertNotEquals(null, NetUtils.getHostName());
    Deencapsulation.setField(NetUtils.class, "hostName", null);
    Assert.assertNotEquals(null, NetUtils.getHostName());
  }

  @Test
  public void testGetHostAddress() {
    Assert.assertNotEquals(null, NetUtils.getHostAddress());
    Deencapsulation.setField(NetUtils.class, "hostAddress", null);
    Assert.assertNotEquals(null, NetUtils.getHostAddress());
  }
}
