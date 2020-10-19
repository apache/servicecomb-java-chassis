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

  private static final String IPV4_KEY = "_v4";

  private static final String IPV6_KEY = "_v6";

  // one interface can bind to multiple address
  // we only save one ip for each interface name.
  // eg:
  // 1. eth0 -> ip1 ip2
  //    last data is eth0 -> ip2
  // 2. eth0 -> ip1
  //    eth0:0 -> ip2
  //    eth0:1 -> ip3
  //    on interface name conflict, all data saved

  // key is network interface name and type
  private static Map<String, InetAddress> allInterfaceAddresses = new HashMap<>();

  private static String hostName;

  private static String hostAddress;

  private static String hostAddressIpv6;

  static {
    doGetHostNameAndHostAddress();
  }

  private static void doGetHostNameAndHostAddress() {
    try {
      doGetAddressFromNetworkInterface();
      // getLocalHost will throw exception in some docker image and sometimes will do a hostname lookup and time consuming
      InetAddress localHost = InetAddress.getLocalHost();
      hostName = localHost.getHostName();
      if ((localHost.isAnyLocalAddress() || localHost.isLoopbackAddress() || localHost.isMulticastAddress())
          && !allInterfaceAddresses.isEmpty()) {
        allInterfaceAddresses.forEach((key, val) -> {
          if (key.endsWith(IPV4_KEY)) {
            hostAddress = val.getHostAddress();
            LOGGER.warn("cannot find a proper ipv4 host address, choose {} , may not be correct.", hostAddress);
          } else {
            hostAddressIpv6 = val.getHostAddress();
            int index = hostAddressIpv6.indexOf("%");
            if (index > 0) {
              hostAddressIpv6 = hostAddressIpv6.substring(0, index);
            }
            LOGGER.warn("cannot find a proper ipv6 host address, choose {} , may not be correct.", hostAddressIpv6);
          }
        });
      } else {
        LOGGER.info("get localhost address: {}", localHost.getHostAddress());
        hostAddress = localHost.getHostAddress();
      }

      LOGGER.info("add host name from localhost:" + hostName + ",host address:" + hostAddress);
    } catch (Exception e) {
      LOGGER.error("got exception when trying to get addresses:", e);
      if (allInterfaceAddresses.size() >= 1) {
        InetAddress entry = allInterfaceAddresses.entrySet().iterator().next().getValue();
        // get host name will do a reverse name lookup and is time consuming
        hostName = entry.getHostName();
        hostAddress = entry.getHostAddress();
        LOGGER.info("add host name from interfaces:" + hostName + ",host address:" + hostAddress);
      }
    }
  }

  private NetUtils() {
  }

  /**
   * docker环境中，有时无法通过InetAddress.getLocalHost()获取 ，会报unknown host Exception， system error
   * 此时，通过遍历网卡接口的方式规避，出来的数据不一定对
   */
  private static void doGetAddressFromNetworkInterface() throws SocketException {
    Enumeration<NetworkInterface> iterNetwork = NetworkInterface.getNetworkInterfaces();

    while (iterNetwork.hasMoreElements()) {
      NetworkInterface network = iterNetwork.nextElement();

      if (!network.isUp() || network.isLoopback() || network.isVirtual()) {
        continue;
      }

      Enumeration<InetAddress> iterAddress = network.getInetAddresses();
      while (iterAddress.hasMoreElements()) {
        InetAddress address = iterAddress.nextElement();

        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isMulticastAddress()) {
          continue;
        }

        if (Inet4Address.class.isInstance(address)) {
          LOGGER.info(
              "add network interface name:" + network.getName() + ",ipv4 host address:" + address.getHostAddress());
          allInterfaceAddresses.put(network.getName() + IPV4_KEY, address);
        } else if (Inet6Address.class.isInstance(address)) {
          LOGGER.info(
              "add network interface name:" + network.getName() + ",ipv6 host address:" + address.getHostAddress());
          allInterfaceAddresses.put(network.getName() + IPV6_KEY, address);
        }
      }
    }
  }

  /**
   * The format of address should be {@code IPv4:port} or {@code [IPv6]:port}, or {@code host:port},
   * or you will not get expected result.
   *
   * Note that the IPv6 address should be wrapped by square brackets.
   * @return IpPort parsed from input param, or {@code null} if the param is null.
   */
  public static IpPort parseIpPort(String address) {
    if (address == null) {
      return null;
    }

    URI uri = URI.create("http://" + address);
    return parseIpPort(uri, true);
  }

  /**
   * Parse a {@link URI} into an {@link IpPort}.
   *
   * <p>
   *   A uri without port is allowed, in which case the port will be inferred from the scheme. {@code http} is 80, and
   *   {@code https} is 443.
   * </p>
   * <p>
   *   The host of the {@code uri} should not be null, or it will be treated as an illegal param,
   *   and an {@link IllegalArgumentException} will be thrown.
   * </p>
   */
  public static IpPort parseIpPort(URI uri) {
    return parseIpPort(uri, false);
  }

  /**
   * Parse a {@link URI} into an {@link IpPort}
   * @param uri a uri representing {@link IpPort}
   * @param ignorePortUndefined whether the port should be inferred from scheme, when the port part of {@code uri} is {@code -1}.
   * If {@code true} the undefined port is ignored;
   * otherwise a port will be inferred from scheme: {@code http} is 80, and {@code https} is 443.
   */
  public static IpPort parseIpPort(URI uri, boolean ignorePortUndefined) {
    if (null == uri.getHost()) {
      // if the format of address is legal but the value is out of range, URI#create(String) will not throw exception
      // but return a URI with null host.
      throw new IllegalArgumentException("Illegal uri: [" + uri + "]");
    }

    IpPort ipPort = new IpPort(uri.getHost(), uri.getPort());
    if (-1 != ipPort.getPort() || ignorePortUndefined) {
      return ipPort;
    }

    if (uri.getScheme().equals("http")) {
      ipPort.setPort(80);
    }
    if (uri.getScheme().equals("https")) {
      ipPort.setPort(443);
    }

    return ipPort;
  }

  /**
   * @param uriAddress the address containing IP and port info.
   * @return IpPort parsed from input param, or {@code null} if the param is null.
   */
  public static IpPort parseIpPortFromURI(String uriAddress) {
    if (uriAddress == null) {
      return null;
    }

    try {
      return parseIpPort(new URI(uriAddress));
    } catch (URISyntaxException e) {
      return null;
    }
  }

  public static IpPort parseIpPort(String scheme, String authority) {
    if (authority == null) {
      return null;
    }
    return parseIpPort(URI.create(scheme + "://" + authority));
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
      IpPort ipPort = NetUtils.parseIpPort(originalURI);
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
    //If failed to get host name ,micro-service will registry failed
    //So I add retry mechanism
    if (hostName == null) {
      doGetHostNameAndHostAddress();
    }
    return hostName;
  }

  public static String getHostAddress() {
    //If failed to get host address ,micro-service will registry failed
    //So I add retry mechanism
    if (hostAddress == null) {
      doGetHostNameAndHostAddress();
    }
    return hostAddress;
  }

  public static String getIpv6HostAddress() {
    //If failed to get host address ,micro-service will registry failed
    //So I add retry mechanism
    if (hostAddressIpv6 == null) {
      doGetHostNameAndHostAddress();
    }
    return hostAddressIpv6;
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

  @SuppressWarnings({"unused", "try"})
  public static boolean canTcpListen(InetAddress address, int port) {
    try (ServerSocket ss = new ServerSocket(port, 0, address)) {
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public static String humanReadableBytes(long bytes) {
    int unit = 1024;
    if (bytes < unit) {
      return String.valueOf(bytes);
    }
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    char pre = "KMGTPE".charAt(exp - 1);
    return String.format("%.3f%c", bytes / Math.pow(unit, exp), pre);
  }
}
