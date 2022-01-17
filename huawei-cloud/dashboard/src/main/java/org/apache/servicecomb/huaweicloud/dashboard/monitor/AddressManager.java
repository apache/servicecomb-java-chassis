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

package org.apache.servicecomb.huaweicloud.dashboard.monitor;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.deployment.Deployment;
import org.apache.servicecomb.deployment.SystemBootstrapInfo;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.http.client.event.MonitorEndpointChangeEvent;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.MonitorConstant;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;


public class AddressManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(AddressManager.class);

  private static final String MONITOR_SERVICE_NAME = "CseMonitoring";

  private static final String MONITOR_APPLICATION = "default";

  private static final String MONITOR_VERSION = "latest";

  private final List<String> addresses = new ArrayList<>();

  private int index = 0;

  private boolean isSSLEnable = false;

  private volatile List<String> availableZone = new ArrayList<>();

  private volatile List<String> availableRegion = new ArrayList<>();

  public static final Cache<String, Boolean> availableIpCache = CacheBuilder.newBuilder()
      .maximumSize(10)
      .expireAfterAccess(10, TimeUnit.MINUTES)
      .build();

  AddressManager() {
    updateAddresses();
    EventManager.register(this);
  }

  private void updateAddresses() {
    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(
        MonitorConstant.SYSTEM_KEY_DASHBOARD_SERVICE);
    if (info != null && info.getAccessURL() != null) {
      addresses.addAll(info.getAccessURL());
    }
  }

  String nextServer() {
    return getAvailableZoneAddress();
  }

  private void updateServersFromSC() {
    List<MicroserviceInstance> servers = RegistryUtils.findServiceInstance(MONITOR_APPLICATION,
        MONITOR_SERVICE_NAME,
        MONITOR_VERSION);
    if (servers != null) {
      for (MicroserviceInstance server : servers) {
        for (String endpoint : server.getEndpoints()) {
          if (!addresses.contains(endpoint)) {
            addresses.add(endpoint);
          }
        }
      }
    }
  }

  @Subscribe
  public void onMonitorEndpointChangeEvent(MonitorEndpointChangeEvent event) {
    if (null == event) {
      return;
    }
    availableZone = event.getSameAZ();
    availableRegion = event.getSameRegion();
    refreshCache();
  }

  private void refreshCache() {
    availableZone.forEach(address -> availableIpCache.put(address, true));
    availableRegion.forEach(address -> availableIpCache.put(address, true));
  }

  public String getDefaultAddress() {
    if (addresses.size() == 0) {
      return null;
    }
    synchronized (this) {
      this.index++;
      if (this.index >= addresses.size()) {
        this.index = 0;
      }
      return addresses.get(index);
    }
  }

  private String getAvailableZoneAddress() {
    List<String> addresses = getAvailableZoneIpPorts();

    if (!addresses.isEmpty()) {
      synchronized (this) {
        this.index++;
        if (this.index >= addresses.size()) {
          this.index = 0;
        }
        return addresses.get(index);
      }
    }
    return getDefaultAddress();
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

  private List<String> getAvailableAddress(List<String> endpoints) {
    List<String> result = new ArrayList<>();
    for (String endpoint : endpoints) {
      try {
        String uri = getUri(endpoint);
        if (availableIpCache.get(uri, () -> true)) {
          result.add(uri);
        }
      } catch (ExecutionException e) {
        LOGGER.error("Not expected to happen, maybe a bug.", e);
      }
    }
    return result;
  }

  private String getUri(String endpoint) {
    if (isSSLEnable) {
      return StringUtils.replace(endpoint, "rest", "https");
    }
    return StringUtils.replace(endpoint, "rest", "http");
  }
}
