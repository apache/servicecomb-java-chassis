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
package org.apache.servicecomb.serviceregistry.client;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpPortEndpoint {
  private static final Logger LOGGER = LoggerFactory.getLogger(IpPortEndpoint.class);

  private IpPortManager ipPortManager;

  private List<IpPort> ipPorts;

  private AtomicInteger currentAvailableIndex;

  private int maxRetryTimes;

  private EndpointTag tag;

  public IpPortEndpoint(List<IpPort> ipPorts, IpPortManager ipPortManager, EndpointTag tag) {
    if (ipPorts.isEmpty()) {
      throw new IllegalArgumentException("Service center address is required to start the application.");
    }
    this.tag = tag;
    this.ipPortManager = ipPortManager;
    this.ipPorts = ipPorts;
    int initialIndex = new Random().nextInt(ipPorts.size());
    currentAvailableIndex = new AtomicInteger(initialIndex);
    maxRetryTimes = ipPorts.size();
  }

  public List<IpPort> getIpPorts() {
    return ipPorts;
  }

  public IpPortEndpoint setIpPorts(List<IpPort> ipPorts) {
    this.ipPorts = ipPorts;
    return this;
  }

  public AtomicInteger getCurrentAvailableIndex() {
    return currentAvailableIndex;
  }

  public IpPortEndpoint setCurrentAvailableIndex(AtomicInteger currentAvailableIndex) {
    this.currentAvailableIndex = currentAvailableIndex;
    return this;
  }

  public int getMaxRetryTimes() {
    return maxRetryTimes;
  }

  public IpPortEndpoint setMaxRetryTimes(int maxRetryTimes) {
    this.maxRetryTimes = maxRetryTimes;
    return this;
  }

  public IpPort getNextAvailableAddress(IpPort failedIpPort) {
    int currentIndex = currentAvailableIndex.get();
    IpPort current = getAvailableAddress(currentIndex);
    if (current.equals(failedIpPort)) {
      currentAvailableIndex.compareAndSet(currentIndex, currentIndex + 1);
      current = getAvailableAddress();
    }

    LOGGER.info("Change service center [{}] address from {} to {}", tag, failedIpPort.toString(), current.toString());
    return current;
  }

  public IpPort getAvailableAddress() {
    return getAvailableAddress(currentAvailableIndex.get());
  }

  private IpPort getAvailableAddress(int index) {
    if (index < ipPorts.size()) {
      return ipPorts.get(index);
    }
    List<CacheEndpoint> endpoints = ipPortManager.getDiscoveredIpPort();
    if (endpoints == null || (index >= ipPorts.size() + endpoints.size())) {
      currentAvailableIndex.set(0);
      return ipPorts.get(0);
    }
    maxRetryTimes = ipPorts.size() + endpoints.size();
    CacheEndpoint nextEndpoint = endpoints.get(index - ipPorts.size());
    return new URIEndpointObject(nextEndpoint.getEndpoint());
  }

  public EndpointTag getTag() {
    return tag;
  }

  public IpPortEndpoint setTag(EndpointTag tag) {
    this.tag = tag;
    return this;
  }

  public enum EndpointTag {
    REGISTRY,
    DISCOVERY,
    REGISTRY_DISCOVERY
  }
}
