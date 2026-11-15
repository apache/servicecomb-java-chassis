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

package org.apache.servicecomb.localservice;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache for local IP addresses.
 * Used to determine if an endpoint is on the local host.
 */
public final class LocalAddressCache {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalAddressCache.class);

  private static volatile Set<String> localAddresses = null;

  private static final Object LOCK = new Object();

  private LocalAddressCache() {
    // Utility class
  }

  /**
   * Get all local IP addresses.
   * Excludes loopback addresses (127.0.0.1, ::1) as they exist on all machines.
   *
   * @return set of local IP addresses (non-loopback interface addresses only)
   */
  public static Set<String> getLocalAddresses() {
    if (localAddresses == null) {
      synchronized (LOCK) {
        if (localAddresses == null) {
          localAddresses = discoverLocalAddresses();
        }
      }
    }
    return localAddresses;
  }

  /**
   * Check if the given IP address is a local address.
   * Loopback addresses (127.0.0.1, ::1, localhost) are NOT considered local
   * because they exist on all machines, not just this one.
   * Use {@link #isLoopbackAddress(String)} to check for loopback specifically.
   *
   * @param ip the IP address to check
   * @return true if the IP is a non-loopback local interface address
   */
  public static boolean isLocalAddress(String ip) {
    if (ip == null || ip.isEmpty()) {
      return false;
    }

    // Loopback addresses should NOT identify a provider as "local instance"
    // Because 127.0.0.1 exists on ALL machines, not just this one
    if (isLoopbackAddress(ip)) {
      return false;
    }

    // Check against cached local addresses (non-loopback interface IPs)
    return getLocalAddresses().contains(ip);
  }

  /**
   * Check if the given IP address is a loopback address.
   * Loopback addresses (127.0.0.1, ::1, localhost) exist on all machines
   * and should not be used to identify a specific local instance.
   *
   * @param ip the IP address to check
   * @return true if the IP is a loopback address
   */
  public static boolean isLoopbackAddress(String ip) {
    if (ip == null || ip.isEmpty()) {
      return false;
    }

    // Check well-known loopback addresses
    if ("127.0.0.1".equals(ip) || "::1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
      return true;
    }

    // Use InetAddress.isLoopbackAddress() for robust detection
    // This catches 127.x.x.x range and other loopback variations
    try {
      InetAddress inetAddress = InetAddress.getByName(ip);
      return inetAddress.isLoopbackAddress();
    } catch (UnknownHostException e) {
      LOGGER.debug("Failed to resolve address for loopback check: {}", ip);
      return false;
    }
  }

  /**
   * Reset the cache (for testing purposes).
   */
  public static void reset() {
    synchronized (LOCK) {
      localAddresses = null;
    }
  }

  private static Set<String> discoverLocalAddresses() {
    Set<String> addresses = new HashSet<>();

    // Note: Loopback addresses (127.0.0.1, ::1, localhost) are NOT included
    // because they exist on all machines and cannot identify a specific local instance.

    try {
      // Get local host address (only if not loopback)
      InetAddress localHost = InetAddress.getLocalHost();
      if (!localHost.isLoopbackAddress()) {
        addresses.add(localHost.getHostAddress());
        addresses.add(localHost.getHostName());
      }
    } catch (UnknownHostException e) {
      LOGGER.debug("Failed to get local host address", e);
    }

    // Get all network interface addresses (excluding loopback)
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      if (interfaces != null) {
        while (interfaces.hasMoreElements()) {
          NetworkInterface networkInterface = interfaces.nextElement();
          // Skip loopback interfaces
          if (networkInterface.isLoopback()) {
            continue;
          }
          Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
          while (inetAddresses.hasMoreElements()) {
            InetAddress inetAddress = inetAddresses.nextElement();
            // Skip loopback addresses
            if (inetAddress.isLoopbackAddress()) {
              continue;
            }
            String hostAddress = inetAddress.getHostAddress();
            if (hostAddress != null) {
              // Remove scope id from IPv6 addresses
              int percentIndex = hostAddress.indexOf('%');
              if (percentIndex > 0) {
                hostAddress = hostAddress.substring(0, percentIndex);
              }
              addresses.add(hostAddress);
            }
          }
        }
      }
    } catch (SocketException e) {
      LOGGER.warn("Failed to get network interface addresses", e);
    }

    LOGGER.info("Discovered local addresses: {}", addresses);
    return addresses;
  }
}
