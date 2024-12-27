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

package org.apache.servicecomb.registry.consul.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class HostInfo {

  private boolean override;

  private String ipAddress;

  private String hostname;

  public HostInfo(String hostname) {
    this.hostname = hostname;
  }

  public HostInfo() {
  }

  public int getIpAddressAsInt() {
    InetAddress inetAddress = null;
    String host = this.ipAddress;
    if (host == null) {
      host = this.hostname;
    }
    try {
      inetAddress = InetAddress.getByName(host);
    } catch (final UnknownHostException e) {
      throw new IllegalArgumentException(e);
    }
    return ByteBuffer.wrap(inetAddress.getAddress()).getInt();
  }

  public boolean isOverride() {
    return this.override;
  }

  public void setOverride(boolean override) {
    this.override = override;
  }

  public String getIpAddress() {
    return this.ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getHostname() {
    return this.hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }
}
