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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class AbstractAddressManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAddressManager.class);

  public static final String DEFAULT_PROJECT = "default";

  public static final String V4_PREFIX = "/v4/";

  private static final String V3_PREFIX = "/v3/";

  private static final int DEFAULT_METRICS_WINDOW_TIME = 1;

  private static final int ISOLATION_THRESHOLD = 3;

  private List<String> addresses = new ArrayList<>();

  private int index = 0;

  private String projectName;

  // if address in same zone will be true; others will be false.
  private final Map<String, Boolean> addressCategory = new HashMap<>();

  // recording continuous times of failure of an address.
  private final Map<String, Integer> addressFailureStatus = new ConcurrentHashMap<>();

  // recording address isolation status, if isolated will be false
  private final Map<String, Boolean> addressIsolated = new ConcurrentHashMap<>();

  // recording address isolation status, if isolated will be false
  private Cache<String, Boolean> addressIsolationStatus = CacheBuilder.newBuilder()
      .maximumSize(100)
      .expireAfterWrite(1, TimeUnit.MINUTES)
      .build();

  private volatile List<String> availableZone = new ArrayList<>();

  private volatile List<String> availableRegion = new ArrayList<>();

  private final List<String> defaultAddress = new ArrayList<>();

  private boolean addressAutoRefreshed = false;

  private final Object lock = new Object();

  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1,
      new ThreadFactoryBuilder()
          .setNameFormat("check-available-address-%d")
          .build());

  public AbstractAddressManager(List<String> addresses) {
    this.projectName = DEFAULT_PROJECT;
    this.addresses.addAll(addresses);
    this.defaultAddress.addAll(addresses);
  }

  public AbstractAddressManager(String projectName, List<String> addresses) {
    this.projectName = StringUtils.isEmpty(projectName) ? DEFAULT_PROJECT : projectName;
    this.addresses = this.transformAddress(addresses);
    this.defaultAddress.addAll(this.addresses);
  }

  @VisibleForTesting
  Cache<String, Boolean> getAddressIsolationStatus() {
    return addressIsolationStatus;
  }

  @VisibleForTesting
  void setAddressIsolationStatus(Cache<String, Boolean> addressIsolationStatus) {
    this.addressIsolationStatus = addressIsolationStatus;
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
    executorService.scheduleAtFixedRate(this::checkHistory,
        0,
        DEFAULT_METRICS_WINDOW_TIME,
        TimeUnit.MINUTES);
  }

  public String formatUrl(String url, boolean absoluteUrl, String address) {
    return absoluteUrl ? address + url : formatAddress(address) + url;
  }

  public String address() {
    if (!addressAutoRefreshed) {
      return getDefaultAddress();
    } else {
      return getAvailableZoneAddress();
    }
  }

  public boolean sslEnabled() {
    return address().startsWith("https://");
  }

  protected List<String> transformAddress(List<String> addresses) {
    return addresses.stream().map(this::formatAddress).collect(Collectors.toList());
  }

  protected String getUrlPrefix(String address) {
    return address + V3_PREFIX;
  }

  protected String formatAddress(String address) {
    try {
      return getUrlPrefix(address) + HttpUtils.encodeURLParam(this.projectName);
    } catch (Exception e) {
      throw new IllegalStateException("not possible");
    }
  }

  private String getDefaultAddress() {
    List<String> addresses = getAvailableAddress(defaultAddress);
    if (!addresses.isEmpty()) {
      return getCurrentAddress(addresses);
    }
    return getInitAddress();
  }

  private String getAvailableZoneAddress() {
    List<String> addresses = getAvailableZoneIpPorts();
    if (!addresses.isEmpty()) {
      return getCurrentAddress(addresses);
    }
    return getInitAddress();
  }

  // when all available address is fail, it will use all the initial addresses for polling.
  private String getInitAddress() {
    if (addresses.isEmpty()) {
      return null;
    }
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

  private List<String> getAvailableZoneIpPorts() {
    List<String> results = new ArrayList<>();
    if (!availableZone.isEmpty()) {
      results.addAll(getAvailableAddress(availableZone));
    } else {
      results.addAll(getAvailableAddress(availableRegion));
    }
    return results;
  }

  private List<String> getAvailableAddress(List<String> endpoints) {
    return endpoints.stream().filter(uri -> !addressIsolated.containsKey(uri) || addressIsolated.get(uri))
        .collect(Collectors.toList());
  }

  protected String normalizeUri(String endpoint) {
    return new URLEndPoint(endpoint).toString();
  }

  public void refreshEndpoint(RefreshEndpointEvent event, String key) {
    if (null == event || !event.getName().equals(key)) {
      return;
    }

    availableZone = event.getSameZone().stream().map(this::normalizeUri).collect(Collectors.toList());
    availableRegion = event.getSameRegion().stream().map(this::normalizeUri).collect(Collectors.toList());
    availableZone.forEach(address -> addressCategory.put(address, true));
    availableRegion.forEach(address -> addressCategory.put(address, false));
    startCheck();
    addressAutoRefreshed = true;
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

  public void recordSuccessState(String address) {
    addressFailureStatus.put(address, 0);
  }

  @VisibleForTesting
  protected void checkHistory() {
    addressIsolated.keySet().stream().filter(this::judgeIsolation).forEach(s -> {
      if (telnetTest(s)) {
        rejoinAddress(s);
      } else {
        addressIsolationStatus.put(s, false);
      }
    });
  }

  private Boolean judgeIsolation(String address) {
    try {
      return addressIsolationStatus.get(address, () -> true);
    } catch (ExecutionException e) {
      return true;
    }
  }

  protected boolean telnetTest(String address) {
    URI ipPort = parseIpPortFromURI(address);
    try (Socket s = new Socket()) {
      s.connect(new InetSocketAddress(ipPort.getHost(), ipPort.getPort()), 3000);
      return true;
    } catch (IOException e) {
      LOGGER.warn("ping endpoint {} failed, It will be quarantined again.", address);
    }
    return false;
  }

  private URI parseIpPortFromURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      return null;
    }
  }

  //Query whether the current address belongs to the same AZ or region through azmap,
  // add it to the sequence of, and delete the record in history
  @VisibleForTesting
  void rejoinAddress(String address) {
    if (!addressAutoRefreshed) {
      defaultAddress.add(address);
      addressFailureStatus.put(address, 0);
      addressIsolated.remove(address);
      return;
    }

    if (addressCategory.get(address) == null) {
      LOGGER.warn("may not happen {}-{}", addressCategory.size(), address);
      return;
    }

    if (addressCategory.get(address)) {
      availableZone.add(address);
    } else {
      availableRegion.add(address);
    }
    addressFailureStatus.put(address, 0);
    addressIsolated.remove(address);
  }

  //Query whether the current address belongs to the same AZ or the same region through AZMap,
  // and delete it from the record. At the same time, add records in history and cache
  @VisibleForTesting
  void removeAddress(String address) {
    if (!addressAutoRefreshed) {
      defaultAddress.remove(address);
      addressIsolated.put(address, false);
      addressIsolationStatus.put(address, false);
      return;
    }

    if (addressCategory.get(address) == null) {
      LOGGER.warn("may not happen {}-{}", addressCategory.size(), address);
      return;
    }

    if (addressCategory.get(address)) {
      availableZone.remove(address);
    } else {
      availableRegion.remove(address);
    }

    addressIsolated.put(address, false);
    addressIsolationStatus.put(address, false);
  }
}
