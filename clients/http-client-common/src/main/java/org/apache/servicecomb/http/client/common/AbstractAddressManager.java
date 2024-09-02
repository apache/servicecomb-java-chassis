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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class AbstractAddressManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAddressManager.class);

  public static final String DEFAULT_PROJECT = "default";

  public static final String V4_PREFIX = "/v4/";

  private static final String V3_PREFIX = "/v3/";

  private static final int DEFAULT_ADDRESS_CHECK_TIME = 30;

  private static final int ISOLATION_THRESHOLD = 3;

  private volatile List<String> addresses = new ArrayList<>();

  // when all addresses are isolation, it will use this for polling.
  private final List<String> defaultAddress = new ArrayList<>();

  private final List<String> defaultIsolationAddress = new ArrayList<>();

  private int index;

  private String projectName;

  // all address list.
  private final Set<String> addressCategory = new HashSet<>();

  // recording continuous times of failure of an address.
  private final Map<String, Integer> addressFailureStatus = new ConcurrentHashMap<>();

  private volatile List<String> availableZone = new ArrayList<>();

  private final List<String> isolationZoneAddress = new ArrayList<>();

  private volatile List<String> availableRegion = new ArrayList<>();

  private final List<String> isolationRegionAddress = new ArrayList<>();

  private boolean addressAutoRefreshed = false;

  private final Object lock = new Object();

  private final Random random = new Random();

  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1,
      new ThreadFactoryBuilder()
          .setNameFormat("check-available-address-%d")
          .build());

  public AbstractAddressManager(List<String> addresses) {
    this.projectName = DEFAULT_PROJECT;
    this.addresses.addAll(addresses);
    this.defaultAddress.addAll(addresses);
    this.addressCategory.addAll(addresses);
    this.index = !addresses.isEmpty() ? random.nextInt(addresses.size()) : 0;
    startCheck();
  }

  public AbstractAddressManager(String projectName, List<String> addresses) {
    this.projectName = StringUtils.isEmpty(projectName) ? DEFAULT_PROJECT : projectName;
    this.addresses = this.transformAddress(addresses);
    this.defaultAddress.addAll(addresses);
    this.addressCategory.addAll(this.addresses);
    this.index = !addresses.isEmpty() ? random.nextInt(addresses.size()) : 0;
    startCheck();
  }

  public void refreshEndpoint(RefreshEndpointEvent event, String key) {
    if (null == event || !event.getName().equals(key)) {
      return;
    }

    availableZone = event.getSameZone().stream().map(this::normalizeUri).collect(Collectors.toList());
    availableRegion = event.getSameRegion().stream().map(this::normalizeUri).collect(Collectors.toList());
    addressCategory.addAll(availableZone);
    addressCategory.addAll(availableRegion);
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

  private void startCheck() {
    executorService.scheduleAtFixedRate(this::checkHistory, 0, DEFAULT_ADDRESS_CHECK_TIME, TimeUnit.SECONDS);
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
    return getCurrentAddress(new ArrayList<>(defaultAddress));
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

  @VisibleForTesting
  protected void checkHistory() {
    addressCategory.forEach(address -> {
      if (telnetTest(address)) {
        // isolation addresses find address and restore it
        findAndRestoreAddress(address);
      } else {
        recordFailState(address);
      }
    });
  }

  protected boolean telnetTest(String address) {
    URI uri = parseIpPortFromURI(address);
    if (uri == null) {
      return false;
    }
    try (Socket s = new Socket()) {
      s.connect(new InetSocketAddress(uri.getHost(), uri.getPort()), 3000);
      return true;
    } catch (IOException e) {
      LOGGER.warn("ping endpoint {} failed, It will be quarantined again.", address);
      return false;
    }
  }

  private URI parseIpPortFromURI(String address) {
    try {
      return new URI(address);
    } catch (URISyntaxException e) {
      LOGGER.error("parse address [{}] failed.", address, e);
      return null;
    }
  }

  protected void findAndRestoreAddress(String address) {
    recordSuccessState(address);
    if (addressAutoRefreshed) {
      if (isolationZoneAddress.remove(address)) {
        LOGGER.warn("restore default address [{}]", address);
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
      LOGGER.warn("restore same region address [{}]", address);
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
      return;
    }
    if (availableRegion.remove(address)) {
      LOGGER.warn("isolation same region address [{}]", address);
      isolationRegionAddress.add(address);
    }
  }
}
