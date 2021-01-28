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


import com.google.common.eventbus.Subscribe;
import org.apache.servicecomb.deployment.SystemBootstrapInfo;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.deployment.Deployment;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.MonitorConstant;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.event.MonitorFailEvent;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.RegistryUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AddressManager {
  private static final String MONITR_SERVICE_NAME = "CseMonitoring";

  private static final String MONITR_APPLICATION = "default";

  private static final String MONITR_VERSION = "latest";

  static class State {
    static final long MAX_TIME = 300000;

    static final int MIN_FAILED = 3;

    private long failedCount = 0;

    private long time;

    public long getTime() {
      return time;
    }

    public void setTime(long time) {
      this.time = time;
    }

    boolean isIsolated() {
      if (failedCount < MIN_FAILED) {
        return false;
      }
      if ((System.currentTimeMillis() - time) > MAX_TIME) {
        time = System.currentTimeMillis();
        return false;
      }
      return true;
    }

    void setIsolateStatus(boolean isFailed) {
      if (isFailed) {
        if (failedCount == 0) {
          time = System.currentTimeMillis();
        }
        failedCount++;
      } else {
        failedCount = 0;
      }
    }
  }

  private Map<String, State> addresses = new LinkedHashMap<>();

  private String currentServer;

  private State discoveryState = new State();

  AddressManager() {
    updateAddresses();
    EventManager.register(this);
  }

  public Map<String, State> getAddresses() {
    return addresses;
  }

  public void updateAddresses() {
    SystemBootstrapInfo info = Deployment.getSystemBootStrapInfo(
        MonitorConstant.SYSTEM_KEY_DASHBOARD_SERVICE);
    if (addresses.size() > 0) {
      addresses.clear();
    }
    if (info != null && info.getAccessURL() != null) {
      info.getAccessURL().forEach(url -> {
        addresses.put(url, new State());
      });
    }
  }

  @Subscribe
  public void MonitorFailEvent(MonitorFailEvent event) {
    updateAddresses();
  }

  String nextServer() {
    if (currentServer == null && addresses.size() > 0) {
      currentServer = addresses.keySet().iterator().next();
    }

    if (currentServer == null || addresses.get(currentServer).isIsolated()) {
      currentServer = null;
      if (!discoveryState.isIsolated()) {
        updateServersFromSC();
        discoveryState.setIsolateStatus(true);
      }
      for (Map.Entry<String, State> entry : addresses.entrySet()) {
        if (!entry.getValue().isIsolated()) {
          currentServer = entry.getKey();
          break;
        }
      }
    }
    return currentServer;
  }

  private void updateServersFromSC() {
    List<MicroserviceInstance> servers = RegistryUtils.findServiceInstance(MONITR_APPLICATION,
        MONITR_SERVICE_NAME,
        MONITR_VERSION);
    if (servers != null) {
      for (MicroserviceInstance server : servers) {
        for (String endpoint : server.getEndpoints()) {
          addresses.computeIfAbsent(endpoint, key -> new State());
        }
      }
    }
  }

  void updateStates(String server, boolean failed) {
    addresses.get(server).setIsolateStatus(failed);
  }
}
