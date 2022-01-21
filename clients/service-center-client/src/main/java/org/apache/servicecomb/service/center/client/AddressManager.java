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

package org.apache.servicecomb.service.center.client;

import java.util.List;

import org.apache.servicecomb.http.client.common.HttpUtils;
import org.apache.servicecomb.http.client.event.EventManager;
import org.apache.servicecomb.http.client.event.ServiceCenterEndpointChangeEvent;
import org.apache.servicecomb.http.client.common.EndpointAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class AddressManager {

  private final String projectName;

  private EndpointAddress endpointAddress;

  private boolean isSSLEnable = false;

  private String currentAddress = "";

  public AddressManager(String projectName, List<String> addresses) {
    this.projectName = projectName;
    this.endpointAddress = new EndpointAddress(addresses);
    EventManager.register(this);
  }

  private String formatAddress(String address) {
    try {
      return address + "/v4/" + HttpUtils.encodeURLParam(this.projectName);
    } catch (Exception e) {
      throw new IllegalStateException("not possible");
    }
  }

  public String address() {
    return endpointAddress.getAvailableZoneAddress();
  }

  public boolean sslEnabled() {
    isSSLEnable = address().startsWith("https://");
    return isSSLEnable;
  }

  public String formatUrl(String url, boolean absoluteUrl) {
    currentAddress = address();
    return absoluteUrl ? currentAddress + url : formatAddress(currentAddress) + url;
  }

  public String getCurrentAddress() {
    return currentAddress;
  }

  public EndpointAddress getEndpointAddress() {
    return endpointAddress;
  }

  public void setEndpointAddress(EndpointAddress endpointAddress) {
    this.endpointAddress = endpointAddress;
  }

  @Subscribe
  public void onServiceCenterEndpointChangeEvent(ServiceCenterEndpointChangeEvent event) {
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
