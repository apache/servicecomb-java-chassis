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
import org.apache.servicecomb.http.client.event.MonitorEndpointChangeEvent;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.MonitorConstant;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.model.EndpointAddress;

import com.google.common.eventbus.Subscribe;


public class AddressManager {

  private final List<String> addresses = new ArrayList<>();

  private EndpointAddress endpointAddress;

  AddressManager() {
    updateAddresses();
    this.endpointAddress = new EndpointAddress(addresses);
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
    return endpointAddress.getAvailableZoneAddress();
  }

  public EndpointAddress getEndpointAddress() {
    return endpointAddress;
  }

  public void setEndpointAddress(EndpointAddress endpointAddress) {
    this.endpointAddress = endpointAddress;
  }

  @Subscribe
  public void onMonitorEndpointChangeEvent(MonitorEndpointChangeEvent event) {
    if (null == event) {
      return;
    }
    endpointAddress.setAvailableZone(event.getSameAZ());
    endpointAddress.setAvailableRegion(event.getSameRegion());
    refreshCache();
  }

  private void refreshCache() {
    endpointAddress.getAvailableZone()
        .forEach(address -> endpointAddress.getAvailableIpCache().put(address, true));
    endpointAddress.getAvailableRegion()
        .forEach(address -> endpointAddress.getAvailableIpCache().put(address, true));
  }
}
