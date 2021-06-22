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
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

public final class NetUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);

  private static final String IPV4_KEY = "_v4";

  private static final String IPV6_KEY = "_v6";

  private static final String PREFERRED_INTERFACE = "eth";

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
      LOGGER.info("localhost hostName={}, hostAddress={}.", hostName, localHost.getHostAddress());

      if (!isLocalAddress(localHost)) {
        if (Inet6Address.class.isInstance(localHost)) {
          hostAddressIpv6 = trimIpv6(localHost.getHostAddress());
          hostAddress = tryGetHostAddressFromNetworkInterface(false, localHost);
          LOGGER.info("Host address info ipV4={}, ipV6={}.", hostAddress, hostAddressIpv6);
          return;
        }
        hostAddress = localHost.getHostAddress();
        hostAddressIpv6 = trimIpv6(tryGetHostAddressFromNetworkInterface(true, localHost));
        LOGGER.info("Host address info ipV4={}, ipV6={}.", hostAddress, hostAddressIpv6);
        return;
      }
      hostAddressIpv6 = trimIpv6(tryGetHostAddressFromNetworkInterface(true, localHost));
      hostAddress = tryGetHostAddressFromNetworkInterface(false, localHost);
      LOGGER.info("Host address info ipV4={}, ipV6={}.", hostAddress, hostAddressIpv6);
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

  private static String tryGetHostAddressFromNetworkInterface(boolean isIpv6, InetAddress localhost) {
    InetAddress result = null;
    for (Entry<String, InetAddress> entry : allInterfaceAddresses.entrySet()) {
      if (isIpv6 && entry.getKey().endsWith(IPV6_KEY)) {
        result = entry.getValue();
        if (entry.getKey().startsWith(PREFERRED_INTERFACE)) {
          return result.getHostAddress();
        }
      } else if (!isIpv6 && entry.getKey().endsWith(IPV4_KEY)) {
        result = entry.getValue();
        if (entry.getKey().startsWith(PREFERRED_INTERFACE)) {
          return result.getHostAddress();
        }
      }
    }

    if (result == null) {
      return localhost.getHostAddress();
    }

    return result.getHostAddress();
  }

  private NetUtils() {
  }

  /**
   * docker环境中，有时无法通过InetAddress.getLocalHost()获取 ，会报unknown host Exception， system error
   * 此时，通过遍历网卡接口的方式规避，出来的数据不一定对
   */
  private static void doGetAddressFromNetworkInterface() throws SocketException {
    Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

    while (networkInterfaces.hasMoreElements()) {
      NetworkInterface network = networkInterfaces.nextElement();

      if (!network.isUp() || network.isLoopback() || network.isVirtual()) {
        continue;
      }

      Enumeration<InetAddress> addresses = network.getInetAddresses();
      while (addresses.hasMoreElements()) {
        InetAddress address = addresses.nextElement();

        if (isLocalAddress(address)) {
          continue;
        }

        if (address instanceof Inet4Address) {
          LOGGER.info("add ipv4 network interface:" + network.getName() + ",host address:" + address.getHostAddress());
          allInterfaceAddresses.put(network.getName() + IPV4_KEY, address);
        } else if (address instanceof Inet6Address) {
          LOGGER.info("add ipv6 network interface:" + network.getName() + ",host address:" + address.getHostAddress());
          allInterfaceAddresses.put(network.getName() + IPV6_KEY, address);
        }
      }
    }
  }

  private static String trimIpv6(String hostAddress) {
    int index = hostAddress.indexOf("%");
    if (index >= 0) {
      return hostAddress.substring(0, index);
    }
    return hostAddress;
  }

  private static boolean isLocalAddress(InetAddress address) {
    return address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isMulticastAddress();
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
      // validate original url
      NetUtils.parseIpPort(originalURI);

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

  @VisibleForTesting
  static void resetHostName() {
    hostName = null;
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
    char pre = "KMGTPE" .charAt(exp - 1);
    return String.format("%.3f%c", bytes / Math.pow(unit, exp), pre);
  }
}
