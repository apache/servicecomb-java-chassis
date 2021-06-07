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

import org.apache.servicecomb.deployment.Deployment;
import org.apache.servicecomb.deployment.SystemBootstrapInfo;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.MonitorConstant;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.RegistryUtils;


public class AddressManager {
  private static final String MONITOR_SERVICE_NAME = "CseMonitoring";

  private static final String MONITOR_APPLICATION = "default";

  private static final String MONITOR_VERSION = "latest";

  private final List<String> addresses = new ArrayList<>();

  private int index = 0;

  AddressManager() {
    updateAddresses();
    updateServersFromSC();
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
    synchronized (this) {
      this.index++;
      if (this.index >= addresses.size()) {
        this.index = 0;
      }
      return addresses.get(index);
    }
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
}
