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

package org.apache.servicecomb.serviceregistry.refresh;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class AddressManger {
  private static final Logger LOGGER = LoggerFactory.getLogger(IpPortManager.class);

  private boolean autoDiscoveryInited = false;

  private ArrayList<IpPort> defaultIpPort;

  private final AtomicInteger index = new AtomicInteger();

  private volatile List<String> availableZone = new ArrayList<>();

  private volatile List<String> availableRegion = new ArrayList<>();

  public static final Cache<String, Boolean> availableIpCache = CacheBuilder.newBuilder()
      .maximumSize(100)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build();

  public AddressManger(ArrayList<IpPort> defaultIpPort, EventBus eventBus) {
    this.defaultIpPort = defaultIpPort;
    eventBus.register(this);
  }

  public IpPort getAvailableIpPort() {
    IpPort ipPort = null;
    if (!isAutoDiscoveryInited()) {
      ipPort = getDefaultIpPort();
    } else {
      List<String> addresses = getAvailableZoneIpPorts();
      if (addresses.isEmpty()) {
        ipPort = getDefaultIpPort();
      } else {
        if (index.get() >= addresses.size()) {
          index.set(0);
        }
        ipPort = new URIEndpointObject(addresses.get(index.get()));
      }
    }
    index.getAndIncrement();
    return ipPort;
  }

  private List<String> getAvailableZoneIpPorts() {
    List<String> results = new ArrayList<>();
    if (!getAvailableAddress(availableZone).isEmpty()) {
      results.addAll(getAvailableAddress(availableZone));
    } else {
      results.addAll(getAvailableAddress(availableRegion));
    }
    return results;
  }

  private IpPort getDefaultIpPort() {
    if (index.get() >= defaultIpPort.size()) {
      index.set(0);
    }
    return defaultIpPort.get(index.get());
  }

  private List<String> getAvailableAddress(List<String> endpoints) {
    List<String> result = new ArrayList<>();
    for (String endpoint : endpoints) {
      try {
        if (availableIpCache.get(getUri(endpoint), () -> true)) {
          result.add(endpoint);
        }
      } catch (ExecutionException e) {
        LOGGER.error("Not expected to happen, maybe a bug.", e);
      }
    }
    return result;
  }

  private String getUri(String endpoint) {
    return StringUtils.split(endpoint, "//")[1];
  }

  public void recordFailState(String currentAddress) {
    availableIpCache.put(currentAddress, false);
  }

  @Subscribe
  public void onRefreshEndpointEvent(RefreshEndpointEvent event) {
    refreshEndpoint(event, "SERVICECENTER");
  }

  public void refreshEndpoint(RefreshEndpointEvent event, String key) {
    if (null == event || !event.getName().equals(key)) {
      return;
    }
    availableZone = event.getSameZone();
    availableRegion = event.getSameRegion();
    availableZone.forEach(address -> availableIpCache.put(address, true));
    availableRegion.forEach(address -> availableIpCache.put(address, true));
  }

  public boolean isAutoDiscoveryInited() {
    return autoDiscoveryInited;
  }

  public void setAutoDiscoveryInited(boolean autoDiscoveryInited) {
    this.autoDiscoveryInited = autoDiscoveryInited;
  }
}
