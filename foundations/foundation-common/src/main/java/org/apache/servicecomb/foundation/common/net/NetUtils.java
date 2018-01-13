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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);

  // one interface can bind to multiple address
  // we only save one ip for each interface name.
  // eg:
  // 1. eth0 -> ip1 ip2
  //    last data is eth0 -> ip2
  // 2. eth0 -> ip1
  //    eth0:0 -> ip2
  //    eth0:1 -> ip3
  //    on interface name conflict, all data saved

  // key is network interface name
  private static Map<String, InetAddress> allInterfaceAddresses = new HashMap<>();

  private static String hostName;

  private static String hostAddress;

  static {
    try {
      doGetIpv4AddressFromNetworkInterface();
      // getLocalHost will throw exception in some docker image and sometimes will do a hostname lookup and time consuming
      InetAddress localHost = InetAddress.getLocalHost();
      hostName = localHost.getHostName();
      if ((localHost.isAnyLocalAddress() || localHost.isLoopbackAddress() || localHost.isMulticastAddress())
          && !allInterfaceAddresses.isEmpty()) {
        InetAddress availabelAddress = allInterfaceAddresses.values().iterator().next();
        hostAddress = availabelAddress.getHostAddress();
        LOGGER.warn("cannot find a proper host address, choose {}, may not be correct.", hostAddress);
      } else {
        hostAddress = localHost.getHostAddress();
      }

      LOGGER.info(
          "add host name from localhost:" + hostName + ",host address:" + hostAddress);
    } catch (Exception e) {
      LOGGER.error("got exception when trying to get addresses:", e);
      if (allInterfaceAddresses.size() >= 1) {
        InetAddress entry = allInterfaceAddresses.entrySet().iterator().next().getValue();
        // get host name will do a reverse name lookup and is time consuming
        hostName = entry.getHostName();
        hostAddress = entry.getHostAddress();
        LOGGER.info(
            "add host name from interfaces:" + hostName + ",host address:" + hostAddress);
      }
    }
  }

  private NetUtils() {
  }

  /**
   * docker环境中，有时无法通过InetAddress.getLocalHost()获取 ，会报unknown host Exception， system error
   * 此时，通过遍历网卡接口的方式规避，出来的数据不一定对
   */
  private static void doGetIpv4AddressFromNetworkInterface() throws SocketException {
    Enumeration<NetworkInterface> iterNetwork = NetworkInterface.getNetworkInterfaces();

    while (iterNetwork.hasMoreElements()) {
      NetworkInterface network = iterNetwork.nextElement();

      if (!network.isUp() || network.isLoopback() || network.isVirtual()) {
        continue;
      }

      Enumeration<InetAddress> iterAddress = network.getInetAddresses();
      while (iterAddress.hasMoreElements()) {
        InetAddress address = iterAddress.nextElement();

        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isMulticastAddress()
            || Inet6Address.class.isInstance(address)) {
          continue;
        }

        if (Inet4Address.class.isInstance(address)) {
          LOGGER.info(
              "add network interface name:" + network.getName() + ",host address:" + address.getHostAddress());
          allInterfaceAddresses.put(network.getName(), address);
        }
      }
    }
  }

  /**
   * address ip:port格式
   */
  public static IpPort parseIpPort(String address) {
    if (address == null) {
      return null;
    }

    int idx = address.indexOf(':');
    if (idx == -1) {
      return null;
    }
    String hostOrIp = address.substring(0, idx);
    int port = Integer.parseInt(address.substring(idx + 1));

    return new IpPort(hostOrIp, port);
  }

  public static IpPort parseIpPortFromURI(String uriAddress) {
    if (uriAddress == null) {
      return null;
    }

    try {
      URI uri = new URI(uriAddress);
      String authority = uri.getAuthority();
      return parseIpPort(authority);
    } catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * 对于配置为0.0.0.0的地址，let it go
   * schema, e.g. http
   * adddress, e.g 0.0.0.0:8080
   * return 实际监听的地址
   */
  public static String getRealListenAddress(String schema, String address) {
    if (address == null) {
      return null;
    }
    try {
      URI originalURI = new URI(schema + "://" + address);
      IpPort ipPort = NetUtils.parseIpPort(originalURI.getAuthority());
      if (ipPort == null) {
        LOGGER.error("address {} is not valid.", address);
        return null;
      }
      return originalURI.toString();
    } catch (URISyntaxException e) {
      LOGGER.error("address {} is not valid.", address);
      return null;
    }
  }

  public static String getHostName() {
    return hostName;
  }

  public static String getHostAddress() {
    return hostAddress;
  }

  public static InetAddress getInterfaceAddress(String interfaceName) {
    return allInterfaceAddresses.get(interfaceName);
  }

  public static InetAddress ensureGetInterfaceAddress(String interfaceName) {
    InetAddress address = allInterfaceAddresses.get(interfaceName);
    if (address == null) {
      throw new IllegalArgumentException("Can not find address for interface name: " + interfaceName);
    }
    return address;
  }

  public static boolean canTcpListen(InetAddress address, int port) {
    try (ServerSocket ss = new ServerSocket(port, 0, address)) {
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
