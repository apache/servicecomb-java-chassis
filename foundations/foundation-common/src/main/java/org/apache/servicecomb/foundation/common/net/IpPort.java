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

import java.net.InetSocketAddress;

public class IpPort {
  private String hostOrIp;

  private int port;

  private volatile InetSocketAddress socketAddress;

  private final Object lock = new Object();

  public IpPort() {

  }

  public IpPort(String hostOrIp, int port) {
    this.hostOrIp = hostOrIp;
    this.port = port;
  }

  public String getHostOrIp() {
    return hostOrIp;
  }

  public void setHostOrIp(String hostOrIp) {
    this.hostOrIp = hostOrIp;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    IpPort ipPort = (IpPort) o;

    if (port != ipPort.port) {
      return false;
    }
    return hostOrIp.equals(ipPort.hostOrIp);
  }

  @Override
  public String toString() {
    return hostOrIp + ":" + port;
  }

  public InetSocketAddress getSocketAddress() {
    if (socketAddress == null) {
      synchronized (lock) {
        if (socketAddress == null) {
          InetSocketAddress tmpSocketAddress = new InetSocketAddress(hostOrIp, port);
          socketAddress = tmpSocketAddress;
        }
      }
    }

    return socketAddress;
  }
}
