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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Deencapsulation;

public class TestNetUtils {
  @Test
  public void testFindProperHostAddress() {
    NetUtils.resetHostName();
    String result = NetUtils.getHostName();
    System.out.println(result);
    Assert.assertNotNull(result);

    result = NetUtils.getHostAddress();
    System.out.println(result);
    Assert.assertNotNull(result);

    result = NetUtils.getIpv6HostAddress();
    System.out.println(result);
    if (result != null) {
      Assert.assertFalse(result.contains("%"));
    }
  }

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
  public void testNetUtils() {
    Assert.assertEquals("127.0.0.1", NetUtils.parseIpPort("127.0.0.1:8080").getHostOrIp());
    Assert.assertEquals(8080, NetUtils.parseIpPort("127.0.0.1:8080").getPort());
    Assert.assertEquals("127.0.0.1", NetUtils.parseIpPort("127.0.0.1").getHostOrIp());
    Assert.assertEquals(-1, NetUtils.parseIpPort("127.0.0.1").getPort());
    Assert.assertEquals(null, NetUtils.parseIpPort((String) null));
    Assert.assertEquals(null, NetUtils.parseIpPortFromURI(null));
    Assert.assertEquals("127.0.0.1", NetUtils.parseIpPortFromURI("rest://127.0.0.1:8080").getHostOrIp());
    Assert.assertEquals(8080, NetUtils.parseIpPortFromURI("http://127.0.0.1:8080").getPort());
    Assert.assertEquals(80, NetUtils.parseIpPortFromURI("http://127.0.0.1").getPort());
    Assert.assertEquals(8080, NetUtils.parseIpPortFromURI("https://127.0.0.1:8080").getPort());
    Assert.assertEquals(443, NetUtils.parseIpPortFromURI("https://127.0.0.1").getPort());

    Assert.assertEquals(30000, NetUtils.parseIpPort("http", "127.0.0.1:30000").getPort());
    Assert.assertEquals("127.0.0.1", NetUtils.parseIpPort("http", "127.0.0.1:30000").getHostOrIp());
    Assert.assertEquals(30000, NetUtils.parseIpPort("https", "127.0.0.1:30000").getPort());
    Assert.assertEquals("127.0.0.1", NetUtils.parseIpPort("https", "127.0.0.1:30000").getHostOrIp());
    Assert.assertEquals(80, NetUtils.parseIpPort("http", "127.0.0.1").getPort());
    Assert.assertEquals("127.0.0.1", NetUtils.parseIpPort("http", "127.0.0.1").getHostOrIp());
    Assert.assertEquals(443, NetUtils.parseIpPort("https", "127.0.0.1").getPort());
    Assert.assertEquals("127.0.0.1", NetUtils.parseIpPort("https", "127.0.0.1").getHostOrIp());
    Assert.assertNull(NetUtils.parseIpPort("http", null));

    checkException(v -> NetUtils.parseIpPort("127.0.0.18080"));
    checkException(v -> NetUtils.parseIpPortFromURI("ss"));
  }

  @Test
  public void testNetUtilsIPv6() {
    Assert.assertEquals("[::1]", NetUtils.parseIpPort("[::1]:8080").getHostOrIp());
    Assert.assertEquals("[::]", NetUtils.parseIpPort("[::]:8080").getHostOrIp());
    Assert.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPort("[fe80::f816:3eff:feda:38cd%eth0]:8080").getHostOrIp());
    Assert.assertEquals("[fe80::38f7:44b8:8ab1:468%16]",
        NetUtils.parseIpPort("[fe80::38f7:44b8:8ab1:468%16]:8080").getHostOrIp());
    Assert.assertEquals(8080, NetUtils.parseIpPort("[::1]:8080").getPort());
    Assert.assertEquals(8080, NetUtils.parseIpPort("[::]:8080").getPort());
    Assert.assertEquals(8080, NetUtils.parseIpPort("[fe80::f816:3eff:feda:38cd%eth0]:8080").getPort());
    Assert.assertEquals(8080, NetUtils.parseIpPort("[fe80::38f7:44b8:8ab1:468%16]:8080").getPort());

    Assert.assertEquals("[::1]", NetUtils.parseIpPortFromURI("rest://[::1]:8080").getHostOrIp());
    Assert.assertEquals("[::]", NetUtils.parseIpPortFromURI("rest://[::]:8080").getHostOrIp());
    Assert.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPortFromURI("rest://[fe80::f816:3eff:feda:38cd%eth0]:8080").getHostOrIp());
    Assert.assertEquals("[fe80::38f7:44b8:8ab1:468%16]",
        NetUtils.parseIpPortFromURI("rest://[fe80::38f7:44b8:8ab1:468%16]:8080").getHostOrIp());
    Assert.assertEquals(8080, NetUtils.parseIpPortFromURI("rest://[::1]:8080").getPort());
    Assert.assertEquals(80, NetUtils.parseIpPortFromURI("http://[::1]").getPort());
    Assert.assertEquals(8080, NetUtils.parseIpPortFromURI("https://[::1]:8080").getPort());
    Assert.assertEquals(443, NetUtils.parseIpPortFromURI("https://[::1]").getPort());

    Assert.assertEquals(30000, NetUtils.parseIpPort("http", "[fe80::f816:3eff:feda:38cd%eth0]:30000").getPort());
    Assert.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPort("http", "[fe80::f816:3eff:feda:38cd%eth0]:30000").getHostOrIp());
    Assert.assertEquals(30000, NetUtils.parseIpPort("https", "[fe80::f816:3eff:feda:38cd%eth0]:30000").getPort());
    Assert.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPort("https", "[fe80::f816:3eff:feda:38cd%eth0]:30000").getHostOrIp());
    Assert.assertEquals(80, NetUtils.parseIpPort("http", "[fe80::f816:3eff:feda:38cd%eth0]").getPort());
    Assert.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPort("http", "[fe80::f816:3eff:feda:38cd%eth0]").getHostOrIp());
    Assert.assertEquals(443, NetUtils.parseIpPort("https", "[fe80::f816:3eff:feda:38cd%eth0]").getPort());
    Assert.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPort("https", "[fe80::f816:3eff:feda:38cd%eth0]").getHostOrIp());
  }

  @Test
  public void testFullOperation() {
    Assert.assertNotNull(NetUtils.getHostAddress());
    Assert.assertNotNull(NetUtils.getHostName());
  }

  @Test
  public void testGetRealListenAddress() {
    Assert.assertNull(NetUtils.getRealListenAddress("http", null));
    Assert.assertEquals("http://1.1.1.1:8080", NetUtils.getRealListenAddress("http", "1.1.1.1:8080"));

    checkException(v -> NetUtils.getRealListenAddress("http:1", "1.1.1.1:8080"));
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
      fail("must throw exception");
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
  public void humanReadableBytes() {
    Assert.assertEquals("0", NetUtils.humanReadableBytes(0L));
    Assert.assertEquals("1", NetUtils.humanReadableBytes(1L));
    Assert.assertEquals("1023", NetUtils.humanReadableBytes(1023L));

    Assert.assertEquals("1.000K", NetUtils.humanReadableBytes(1024L));
    Assert.assertEquals("1.001K", NetUtils.humanReadableBytes(1025L));
    Assert.assertEquals("1023.999K", NetUtils.humanReadableBytes(1024L * 1024 - 1));

    Assert.assertEquals("1.000M", NetUtils.humanReadableBytes(1024L * 1024));
    Assert.assertEquals("1.000M", NetUtils.humanReadableBytes(1024L * 1024 + 1));
    Assert.assertEquals("1.001M", NetUtils.humanReadableBytes(1024L * 1024 + 1024));
    Assert.assertEquals("1023.999M", NetUtils.humanReadableBytes(1024L * 1024 * 1024 - 1024));
    Assert.assertEquals("1024.000M", NetUtils.humanReadableBytes(1024L * 1024 * 1024 - 1));
    Assert.assertEquals("1.000G", NetUtils.humanReadableBytes(1024L * 1024 * 1024));
    Assert.assertEquals("1.000G", NetUtils.humanReadableBytes(1024L * 1024 * 1024 + 1));
    Assert.assertEquals("1.000G", NetUtils.humanReadableBytes(1024L * 1024 * 1024 + 1024));
    Assert.assertEquals("1023.999G", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 - 1024 * 1024));
    Assert.assertEquals("1024.000G", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 - 1024));
    Assert.assertEquals("1.000T", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024));
    Assert.assertEquals("1.001T", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 + 1024 * 1024 * 1024));
    Assert.assertEquals("1023.999T",
        NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024 - 1024L * 1024 * 1024));

    Assert.assertEquals("1.000P", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024));
    Assert.assertEquals("1.001P",
        NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024 + 1024L * 1024 * 1024 * 1024));
    Assert.assertEquals("1023.999P",
        NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024 * 1024 - 1024L * 1024 * 1024 * 1024));

    Assert.assertEquals("1.000E", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024 * 1024));
    Assert.assertEquals("1.001E",
        NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024 * 1024 + 1024L * 1024 * 1024 * 1024 * 1024));
    Assert.assertEquals("8.000E", NetUtils.humanReadableBytes(Long.MAX_VALUE));
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

  public void checkException(Consumer<Void> testedBehavior) {
    try {
      testedBehavior.accept(null);
      fail("IllegalArgumentException is expected!");
    } catch (Exception e) {
      Assert.assertEquals(IllegalArgumentException.class, e.getClass());
    }
  }
}
