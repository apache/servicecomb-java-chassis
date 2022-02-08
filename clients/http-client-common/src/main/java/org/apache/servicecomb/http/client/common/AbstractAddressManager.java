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

  private List<String> addresses = new ArrayList<>();

  private int index = 0;

  private String projectName;

  private Map<String, Boolean> categoryMap = new HashMap<>();

  private Map<String, Integer> recodeStatus = new ConcurrentHashMap<>();

  private Map<String, Boolean> history = new ConcurrentHashMap<>();

  private volatile List<String> availableZone = new ArrayList<>();

  private volatile List<String> availableRegion = new ArrayList<>();

  private volatile List<String> defaultAddress = new ArrayList<>();

  private boolean isAddressRefresh = false;

  private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1,
      new ThreadFactoryBuilder()
          .setNameFormat("check-available-address-%d")
          .build());

  private Cache<String, Boolean> cacheAddress = CacheBuilder.newBuilder()
      .maximumSize(100)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build();

  public AbstractAddressManager(List<String> addresses) {
    this.addresses.addAll(addresses);
  }

  public AbstractAddressManager(String projectName, List<String> addresses) {
    this.projectName = StringUtils.isEmpty(projectName) ? DEFAULT_PROJECT : projectName;
    this.addresses = this.transformAddress(addresses);
    this.defaultAddress = this.addresses;
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
    return getAvailableZoneAddress();
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
    synchronized (this) {
      if (addresses.size() == 0) {
        return null;
      }
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
        return joinProject(addresses.get(index));
      }
    }
    return getDefaultAddress();
  }

  protected String joinProject(String address) {
    return address;
  }

  private List<String> getAvailableZoneIpPorts() {
    List<String> results = new ArrayList<>();
    if (!isAddressRefresh) {
      return this.defaultAddress;
    }
    if (!availableZone.isEmpty()) {
      results.addAll(getAvailableAddress(availableZone));
    } else {
      results.addAll(getAvailableAddress(availableRegion));
    }
    return results;
  }

  private List<String> getAvailableAddress(List<String> endpoints) {
    List<String> list = endpoints.stream().filter(uri -> !history.containsKey(uri))
        .collect(Collectors.toList());
    return list;
  }

  protected String normalizeUri(String endpoint) {
    if (endpoint.contains("sslEnabled=true")) {
      return StringUtils.replace(endpoint, "rest", "https");
    }
    return StringUtils.replace(endpoint, "rest", "http");
  }

  public void refreshEndpoint(RefreshEndpointEvent event, String key) {
    this.isAddressRefresh = true;
    if (null == event || !event.getName().equals(key)) {
      return;
    }
    availableZone = event.getSameZone().stream().map(this::normalizeUri).collect(Collectors.toList());
    availableRegion = event.getSameRegion().stream().map(this::normalizeUri).collect(Collectors.toList());
    availableZone.forEach(address -> categoryMap.put(address, true));
    availableRegion.forEach(address -> categoryMap.put(address, false));
    startCheck();
  }

  public void recordFailState(String address) {
    if (recodeStatus.containsKey(address)) {
      int number = recodeStatus.get(address) + 1;
      if (number < 3) {
        recodeStatus.put(address, number);
      } else {
        removeAddress(address);
      }
      return;
    }
    recodeStatus.put(address, 1);
  }

  public void recordSuccessState(String address) {
    if (recodeStatus.containsKey(address) && recodeStatus.get(address) >= 1) {
      recodeStatus.put(address, recodeStatus.get(address) - 1);
    } else {
      recodeStatus.put(address, 0);
    }
  }

  @VisibleForTesting
  protected void checkHistory() {
    history.keySet().stream().filter(this::judgeIsolation).forEach(s -> {
      if (telnetTest(s)) {
        rejoinAddress(s);
      } else {
        cacheAddress.put(s, false);
      }
    });
  }

  private Boolean judgeIsolation(String address) {
    try {
      return cacheAddress.get(address, () -> true);
    } catch (ExecutionException e) {
      return true;
    }
  }

  private boolean telnetTest(String address) {
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
    if (!isAddressRefresh) {
      defaultAddress.add(address);
    } else {
      if (categoryMap.get(address)) {
        availableZone.add(address);
      } else {
        availableRegion.add(address);
      }
    }
    recodeStatus.put(address, 0);
    history.remove(address);
  }

  //Query whether the current address belongs to the same AZ or the same region through AZMap,
  // and delete it from the record. At the same time, add records in history and cache
  @VisibleForTesting
  void removeAddress(String address) {
    if (!isAddressRefresh) {
      defaultAddress.remove(address);
      history.put(address, null);
    } else {
      if (categoryMap.get(address)) {
        availableZone.remove(address);
      } else {
        availableRegion.remove(address);
      }
      history.put(address, categoryMap.get(address));
    }
    recodeStatus.put(address, 0);
    cacheAddress.put(address, false);
  }
}
