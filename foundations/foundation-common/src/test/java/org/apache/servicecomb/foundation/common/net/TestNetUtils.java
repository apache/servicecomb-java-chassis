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
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import mockit.Deencapsulation;

public class TestNetUtils {
  @Test
  public void testFindProperHostAddress() {
    NetUtils.resetHostName();
    String result = NetUtils.getHostName();
    System.out.println(result);
    Assertions.assertNotNull(result);

    result = NetUtils.getHostAddress();
    System.out.println(result);
    Assertions.assertNotNull(result);

    result = NetUtils.getIpv6HostAddress();
    System.out.println(result);
    if (result != null) {
      Assertions.assertFalse(result.contains("%"));
    }
  }

  @Test
  public void testIpPort() {
    IpPort oIPPort = new IpPort("10.145.154.45", 8080);
    Assertions.assertEquals("10.145.154.45", oIPPort.getHostOrIp());
    Assertions.assertEquals(8080, oIPPort.getPort());
    oIPPort.setPort(9090);
    Assertions.assertEquals(9090, oIPPort.getPort());
    Assertions.assertNotEquals(null, oIPPort.getSocketAddress());
  }

  @Test
  public void testNetUtils() {
    Assertions.assertEquals("127.0.0.1", NetUtils.parseIpPort("127.0.0.1:8080").getHostOrIp());
    Assertions.assertEquals(8080, NetUtils.parseIpPort("127.0.0.1:8080").getPort());
    Assertions.assertEquals("127.0.0.1", NetUtils.parseIpPort("127.0.0.1").getHostOrIp());
    Assertions.assertEquals(-1, NetUtils.parseIpPort("127.0.0.1").getPort());
    Assertions.assertNull(NetUtils.parseIpPort((String) null));
    Assertions.assertNull(NetUtils.parseIpPortFromURI(null));
    Assertions.assertEquals("127.0.0.1", NetUtils.parseIpPortFromURI("rest://127.0.0.1:8080").getHostOrIp());
    Assertions.assertEquals(8080, NetUtils.parseIpPortFromURI("http://127.0.0.1:8080").getPort());
    Assertions.assertEquals(80, NetUtils.parseIpPortFromURI("http://127.0.0.1").getPort());
    Assertions.assertEquals(8080, NetUtils.parseIpPortFromURI("https://127.0.0.1:8080").getPort());
    Assertions.assertEquals(443, NetUtils.parseIpPortFromURI("https://127.0.0.1").getPort());

    Assertions.assertEquals(30000, NetUtils.parseIpPort("http", "127.0.0.1:30000").getPort());
    Assertions.assertEquals("127.0.0.1", NetUtils.parseIpPort("http", "127.0.0.1:30000").getHostOrIp());
    Assertions.assertEquals(30000, NetUtils.parseIpPort("https", "127.0.0.1:30000").getPort());
    Assertions.assertEquals("127.0.0.1", NetUtils.parseIpPort("https", "127.0.0.1:30000").getHostOrIp());
    Assertions.assertEquals(80, NetUtils.parseIpPort("http", "127.0.0.1").getPort());
    Assertions.assertEquals("127.0.0.1", NetUtils.parseIpPort("http", "127.0.0.1").getHostOrIp());
    Assertions.assertEquals(443, NetUtils.parseIpPort("https", "127.0.0.1").getPort());
    Assertions.assertEquals("127.0.0.1", NetUtils.parseIpPort("https", "127.0.0.1").getHostOrIp());
    Assertions.assertNull(NetUtils.parseIpPort("http", null));

    checkException(v -> NetUtils.parseIpPort("127.0.0.18080"));
    checkException(v -> NetUtils.parseIpPortFromURI("ss"));
  }

  @Test
  public void testNetUtilsIPv6() {
    Assertions.assertEquals("[::1]", NetUtils.parseIpPort("[::1]:8080").getHostOrIp());
    Assertions.assertEquals("[::]", NetUtils.parseIpPort("[::]:8080").getHostOrIp());
    Assertions.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPort("[fe80::f816:3eff:feda:38cd%eth0]:8080").getHostOrIp());
    Assertions.assertEquals("[fe80::38f7:44b8:8ab1:468%16]",
        NetUtils.parseIpPort("[fe80::38f7:44b8:8ab1:468%16]:8080").getHostOrIp());
    Assertions.assertEquals(8080, NetUtils.parseIpPort("[::1]:8080").getPort());
    Assertions.assertEquals(8080, NetUtils.parseIpPort("[::]:8080").getPort());
    Assertions.assertEquals(8080, NetUtils.parseIpPort("[fe80::f816:3eff:feda:38cd%eth0]:8080").getPort());
    Assertions.assertEquals(8080, NetUtils.parseIpPort("[fe80::38f7:44b8:8ab1:468%16]:8080").getPort());

    Assertions.assertEquals("[::1]", NetUtils.parseIpPortFromURI("rest://[::1]:8080").getHostOrIp());
    Assertions.assertEquals("[::]", NetUtils.parseIpPortFromURI("rest://[::]:8080").getHostOrIp());
    Assertions.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPortFromURI("rest://[fe80::f816:3eff:feda:38cd%eth0]:8080").getHostOrIp());
    Assertions.assertEquals("[fe80::38f7:44b8:8ab1:468%16]",
        NetUtils.parseIpPortFromURI("rest://[fe80::38f7:44b8:8ab1:468%16]:8080").getHostOrIp());
    Assertions.assertEquals(8080, NetUtils.parseIpPortFromURI("rest://[::1]:8080").getPort());
    Assertions.assertEquals(80, NetUtils.parseIpPortFromURI("http://[::1]").getPort());
    Assertions.assertEquals(8080, NetUtils.parseIpPortFromURI("https://[::1]:8080").getPort());
    Assertions.assertEquals(443, NetUtils.parseIpPortFromURI("https://[::1]").getPort());

    Assertions.assertEquals(30000, NetUtils.parseIpPort("http", "[fe80::f816:3eff:feda:38cd%eth0]:30000").getPort());
    Assertions.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPort("http", "[fe80::f816:3eff:feda:38cd%eth0]:30000").getHostOrIp());
    Assertions.assertEquals(30000, NetUtils.parseIpPort("https", "[fe80::f816:3eff:feda:38cd%eth0]:30000").getPort());
    Assertions.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPort("https", "[fe80::f816:3eff:feda:38cd%eth0]:30000").getHostOrIp());
    Assertions.assertEquals(80, NetUtils.parseIpPort("http", "[fe80::f816:3eff:feda:38cd%eth0]").getPort());
    Assertions.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPort("http", "[fe80::f816:3eff:feda:38cd%eth0]").getHostOrIp());
    Assertions.assertEquals(443, NetUtils.parseIpPort("https", "[fe80::f816:3eff:feda:38cd%eth0]").getPort());
    Assertions.assertEquals("[fe80::f816:3eff:feda:38cd%eth0]",
        NetUtils.parseIpPort("https", "[fe80::f816:3eff:feda:38cd%eth0]").getHostOrIp());
  }

  @Test
  public void testFullOperation() {
    Assertions.assertNotNull(NetUtils.getHostAddress());
    Assertions.assertNotNull(NetUtils.getHostName());
  }

  @Test
  public void testGetRealListenAddress() {
    Assertions.assertNull(NetUtils.getRealListenAddress("http", null));
    Assertions.assertEquals("http://1.1.1.1:8080", NetUtils.getRealListenAddress("http", "1.1.1.1:8080"));

    checkException(v -> NetUtils.getRealListenAddress("http:1", "1.1.1.1:8080"));
  }

  @Test
  public void testNetworkInterface() {
    Map<String, InetAddress> org = Deencapsulation.getField(NetUtils.class, "allInterfaceAddresses");

    Map<String, InetAddress> newValue = new HashMap<>();
    InetAddress addr = Mockito.mock(InetAddress.class);
    newValue.put("eth100", addr);
    Deencapsulation.setField(NetUtils.class, "allInterfaceAddresses", newValue);

    Assertions.assertEquals(addr, NetUtils.getInterfaceAddress("eth100"));
    Assertions.assertEquals(addr, NetUtils.ensureGetInterfaceAddress("eth100"));

    try {
      NetUtils.ensureGetInterfaceAddress("xxx");
      Assertions.fail("must throw exception");
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("Can not find address for interface name: xxx", e.getMessage());
    }

    Deencapsulation.setField(NetUtils.class, "allInterfaceAddresses", org);
  }

  @Test
  public void testCanTcpListenNo() throws IOException {
    InetAddress address = InetAddress.getByName("127.0.0.1");
    try (ServerSocket ss = new ServerSocket(0, 0, address)) {
      Assertions.assertFalse(NetUtils.canTcpListen(address, ss.getLocalPort()));
    }
  }

  @Test
  public void testCanTcpListenYes() throws IOException {
    InetAddress address = InetAddress.getByName("127.0.0.1");
    ServerSocket ss = new ServerSocket(0, 0, address);
    int port = ss.getLocalPort();
    ss.close();

    Assertions.assertTrue(NetUtils.canTcpListen(address, port));
  }

  @Test
  public void humanReadableBytes() {
    Assertions.assertEquals("0", NetUtils.humanReadableBytes(0L));
    Assertions.assertEquals("1", NetUtils.humanReadableBytes(1L));
    Assertions.assertEquals("1023", NetUtils.humanReadableBytes(1023L));

    Assertions.assertEquals("1.000K", NetUtils.humanReadableBytes(1024L));
    Assertions.assertEquals("1.001K", NetUtils.humanReadableBytes(1025L));
    Assertions.assertEquals("1023.999K", NetUtils.humanReadableBytes(1024L * 1024 - 1));

    Assertions.assertEquals("1.000M", NetUtils.humanReadableBytes(1024L * 1024));
    Assertions.assertEquals("1.000M", NetUtils.humanReadableBytes(1024L * 1024 + 1));
    Assertions.assertEquals("1.001M", NetUtils.humanReadableBytes(1024L * 1024 + 1024));
    Assertions.assertEquals("1023.999M", NetUtils.humanReadableBytes(1024L * 1024 * 1024 - 1024));
    Assertions.assertEquals("1024.000M", NetUtils.humanReadableBytes(1024L * 1024 * 1024 - 1));
    Assertions.assertEquals("1.000G", NetUtils.humanReadableBytes(1024L * 1024 * 1024));
    Assertions.assertEquals("1.000G", NetUtils.humanReadableBytes(1024L * 1024 * 1024 + 1));
    Assertions.assertEquals("1.000G", NetUtils.humanReadableBytes(1024L * 1024 * 1024 + 1024));
    Assertions.assertEquals("1023.999G", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 - 1024 * 1024));
    Assertions.assertEquals("1024.000G", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 - 1024));
    Assertions.assertEquals("1.000T", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024));
    Assertions.assertEquals("1.001T", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 + 1024 * 1024 * 1024));
    Assertions.assertEquals("1023.999T",
        NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024 - 1024L * 1024 * 1024));

    Assertions.assertEquals("1.000P", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024));
    Assertions.assertEquals("1.001P",
        NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024 + 1024L * 1024 * 1024 * 1024));
    Assertions.assertEquals("1023.999P",
        NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024 * 1024 - 1024L * 1024 * 1024 * 1024));

    Assertions.assertEquals("1.000E", NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024 * 1024));
    Assertions.assertEquals("1.001E",
        NetUtils.humanReadableBytes(1024L * 1024 * 1024 * 1024 * 1024 * 1024 + 1024L * 1024 * 1024 * 1024 * 1024));
    Assertions.assertEquals("8.000E", NetUtils.humanReadableBytes(Long.MAX_VALUE));
  }

  @Test
  public void testGetHostName() {
    Assertions.assertNotEquals(null, NetUtils.getHostName());
    Deencapsulation.setField(NetUtils.class, "hostName", null);
    Assertions.assertNotEquals(null, NetUtils.getHostName());
  }

  @Test
  public void testGetHostAddress() {
    Assertions.assertNotEquals(null, NetUtils.getHostAddress());
    Deencapsulation.setField(NetUtils.class, "hostAddress", null);
    Assertions.assertNotEquals(null, NetUtils.getHostAddress());
  }

  public void checkException(Consumer<Void> testedBehavior) {
    try {
      testedBehavior.accept(null);
      Assertions.fail("IllegalArgumentException is expected!");
    } catch (Exception e) {
      Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
    }
  }
}
