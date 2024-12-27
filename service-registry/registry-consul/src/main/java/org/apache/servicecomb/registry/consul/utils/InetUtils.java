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

package org.apache.servicecomb.registry.consul.utils;

import org.apache.servicecomb.registry.consul.config.HostInfo;

import java.io.Closeable;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class InetUtils implements Closeable {
  private final ExecutorService executorService;

  public InetUtils() {
    this.executorService = Executors.newCachedThreadPool(r -> {
      Thread thread = new Thread(r);
      thread.setName("servicecomb-inet-utils-thread");
      thread.setDaemon(true);
      return thread;
    });
  }

  @Override
  public void close() {
    this.executorService.shutdown();
  }

  public HostInfo findFirstNonLoopbackHostInfo() {
    InetAddress address = findFirstNonLoopbackAddress();
    if (address != null) {
      return convertAddress(address);
    }
    HostInfo hostInfo = new HostInfo();
    hostInfo.setHostname("localhost");
    hostInfo.setIpAddress("127.0.0.1");
    return hostInfo;
  }

  public InetAddress findFirstNonLoopbackAddress() {
    InetAddress result = null;
    try {
      int lowest = Integer.MAX_VALUE;
      for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics
          .hasMoreElements(); ) {
        NetworkInterface ifc = nics.nextElement();
        if (ifc.isUp()) {
          if (ifc.getIndex() < lowest || result == null) {
            lowest = ifc.getIndex();
          } else {
            continue;
          }
          if (!ignoreInterface()) {
            for (Enumeration<InetAddress> addrs = ifc
                .getInetAddresses(); addrs.hasMoreElements(); ) {
              InetAddress address = addrs.nextElement();
              if (address instanceof Inet4Address
                  && !address.isLoopbackAddress()
                  && isPreferredAddress()) {
                result = address;
              }
            }
          }
        }
      }
    } catch (IOException ex) {
    }
    if (result != null) {
      return result;
    }
    try {
      return InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
    }

    return null;
  }

  boolean isPreferredAddress() {
    return true;
  }

  boolean ignoreInterface() {
    return false;
  }

  public HostInfo convertAddress(final InetAddress address) {
    HostInfo hostInfo = new HostInfo();
    Future<String> result = this.executorService.submit(address::getHostName);

    String hostname;
    try {
      hostname = result.get(1, TimeUnit.SECONDS);
    } catch (Exception e) {
      hostname = "localhost";
    }
    hostInfo.setHostname(hostname);
    hostInfo.setIpAddress(address.getHostAddress());
    return hostInfo;
  }
}
