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

package org.apache.servicecomb.http.client.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.http.client.event.EngineConnectChangedEvent;
import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;

public class AbstractAddressManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAddressManager.class);

  public static final String DEFAULT_PROJECT = "default";

  public static final String V4_PREFIX = "/v4/";

  private static final String V3_PREFIX = "/v3/";

  private static final int ISOLATION_THRESHOLD = 3;

  private volatile List<String> addresses = new ArrayList<>();

  // when all addresses are isolation, it will use this for polling.
  private final List<String> defaultAddress = new ArrayList<>();

  private final List<String> defaultIsolationAddress = new ArrayList<>();

  private int index;

  private String projectName;

  // recording continuous times of failure of an address.
  private final Map<String, Integer> addressFailureStatus = new ConcurrentHashMap<>();

  private volatile List<String> availableZone = new ArrayList<>();

  private final List<String> isolationZoneAddress = new ArrayList<>();

  private volatile List<String> availableRegion = new ArrayList<>();

  private final List<String> isolationRegionAddress = new ArrayList<>();

  private boolean addressAutoRefreshed = false;

  private final Object lock = new Object();

  private final Random random = new Random();

  private EventBus eventBus;

  public AbstractAddressManager(List<String> addresses) {
    this.projectName = DEFAULT_PROJECT;
    this.addresses.addAll(addresses);
    this.defaultAddress.addAll(addresses);
    this.index = !addresses.isEmpty() ? getRandomIndex() : 0;
  }

  public AbstractAddressManager(String projectName, List<String> addresses) {
    this.projectName = StringUtils.isEmpty(projectName) ? DEFAULT_PROJECT : projectName;
    this.addresses = this.transformAddress(addresses);
    this.defaultAddress.addAll(addresses);
    this.index = !addresses.isEmpty() ? getRandomIndex() : 0;
  }

  private int getRandomIndex() {
    return random.nextInt(addresses.size());
  }

  public void refreshEndpoint(RefreshEndpointEvent event, String key) {
    if (null == event || !event.getName().equals(key)) {
      return;
    }

    availableZone = event.getSameZone().stream().map(this::normalizeUri).collect(Collectors.toList());
    availableRegion = event.getSameRegion().stream().map(this::normalizeUri).collect(Collectors.toList());
    addressAutoRefreshed = true;
  }

  protected String normalizeUri(String endpoint) {
    return new URLEndPoint(endpoint).toString();
  }

  @VisibleForTesting
  Map<String, Integer> getAddressFailureStatus() {
    return addressFailureStatus;
  }

  public List<String> getAddresses() {
    return addresses;
  }

  public List<String> getAvailableZone() {
    return availableZone;
  }

  public List<String> getAvailableRegion() {
    return availableRegion;
  }

  public String formatUrl(String url, boolean absoluteUrl, String address) {
    return absoluteUrl ? address + url : formatAddress(address) + url;
  }

  public boolean sslEnabled() {
    return address().startsWith("https://");
  }

  protected List<String> transformAddress(List<String> addresses) {
    return addresses.stream().map(this::formatAddress).collect(Collectors.toList());
  }

  protected String formatAddress(String address) {
    try {
      return getUrlPrefix(address) + HttpUtils.encodeURLParam(this.projectName);
    } catch (Exception e) {
      throw new IllegalStateException("not possible");
    }
  }

  protected String getUrlPrefix(String address) {
    return address + V3_PREFIX;
  }

  public String address() {
    if (!addressAutoRefreshed) {
      return getDefaultAddress();
    } else {
      return getAvailableZoneAddress();
    }
  }

  private String getDefaultAddress() {
    if (!addresses.isEmpty()) {
      return getCurrentAddress(addresses);
    }
    LOGGER.warn("all addresses are isolation, please check server status.");
    // when all addresses are isolation, it will use all default address for polling.
    return getCurrentAddress(defaultAddress);
  }

  private String getAvailableZoneAddress() {
    List<String> zoneOrRegionAddress = getZoneOrRegionAddress();
    if (!zoneOrRegionAddress.isEmpty()) {
      return getCurrentAddress(zoneOrRegionAddress);
    }
    LOGGER.warn("all auto discovery addresses are isolation, please check server status.");
    // when all available address are isolation, it will use config addresses for polling.
    return getCurrentAddress(addresses);
  }

  private String getCurrentAddress(List<String> addresses) {
    synchronized (this) {
      this.index++;
      if (this.index >= addresses.size()) {
        this.index = 0;
      }
      return addresses.get(index);
    }
  }

  private List<String> getZoneOrRegionAddress() {
    List<String> results = new ArrayList<>();
    if (!availableZone.isEmpty()) {
      results.addAll(availableZone);
    } else {
      results.addAll(availableRegion);
    }
    return results;
  }

  public void recoverIsolatedAddress(String address) {
    recordSuccessState(address);
    if (addressAutoRefreshed) {
      if (isolationZoneAddress.remove(address)) {
        LOGGER.warn("restore same region address [{}]", address);
        if (eventBus != null && availableZone.isEmpty()) {
          eventBus.post(new EngineConnectChangedEvent());
        }
        availableZone.add(address);
        return;
      }
      if (isolationRegionAddress.remove(address)) {
        LOGGER.warn("restore same zone address [{}]", address);
        availableRegion.add(address);
      }
      return;
    }
    if (defaultIsolationAddress.remove(address)) {
      LOGGER.warn("restore default address [{}]", address);
      addresses.add(address);
    }
  }

  public void recordSuccessState(String address) {
    addressFailureStatus.put(address, 0);
  }

  public void recordFailState(String address) {
    synchronized (lock) {
      if (!addressFailureStatus.containsKey(address)) {
        addressFailureStatus.put(address, 1);
        return;
      }
      int number = addressFailureStatus.get(address) + 1;
      if (number < ISOLATION_THRESHOLD) {
        addressFailureStatus.put(address, number);
      } else {
        removeAddress(address);
      }
    }
  }

  //Query whether the current address belongs to the same AZ or the same region through AZMap,
  // and delete it from the record. At the same time, add records in history and cache
  @VisibleForTesting
  void removeAddress(String address) {
    if (!addressAutoRefreshed) {
      if (addresses.remove(address)) {
        LOGGER.warn("isolation default address [{}]", address);
        defaultIsolationAddress.add(address);
      }
      return;
    }
    if (availableZone.remove(address)) {
      LOGGER.warn("isolation same zone address [{}]", address);
      isolationZoneAddress.add(address);
      if (eventBus != null && availableZone.isEmpty() && !availableRegion.isEmpty()) {
        eventBus.post(new EngineConnectChangedEvent());
      }
      return;
    }
    if (availableRegion.remove(address)) {
      LOGGER.warn("isolation same region address [{}]", address);
      isolationRegionAddress.add(address);
    }
  }

  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public List<String> getIsolationAddresses() {
    List<String> isolationAddresses = new ArrayList<>(defaultIsolationAddress);
    isolationAddresses.addAll(isolationZoneAddress);
    isolationAddresses.addAll(isolationRegionAddress);
    return isolationAddresses;
  }
}
