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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class AbstractAddressManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAddressManager.class);

  public static final String DEFAULT_PROJECT = "default";

  public static final String V4_PREFIX = "/v4/";

  private static final String V3_PREFIX = "/v3/";

  private List<String> addresses = new ArrayList<>();

  private int index = 0;

  private String projectName;

  private String currentAddress = "";

  private volatile List<String> availableZone = new ArrayList<>();

  private volatile List<String> availableRegion = new ArrayList<>();

  private Cache<String, Boolean> availableIpCache = CacheBuilder.newBuilder()
      .maximumSize(100)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build();

  public AbstractAddressManager(List<String> addresses) {
    this.addresses.addAll(addresses);
  }

  public AbstractAddressManager(String projectName, List<String> addresses) {
    this.projectName = StringUtils.isEmpty(projectName) ? DEFAULT_PROJECT : projectName;
    this.addresses = this.transformAddress(addresses);
  }

  public String formatUrl(String url, boolean absoluteUrl) {
    currentAddress = address();
    return absoluteUrl ? currentAddress + url : formatAddress(currentAddress) + url;
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

  private String formatAddress(String address) {
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
    if (endpoint.contains("sslEnabled=true")) {
      return StringUtils.replace(endpoint, "rest", "https");
    }
    return StringUtils.replace(endpoint, "rest", "http");
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

  public void recordFailState() {
    availableIpCache.put(currentAddress, false);
  }
}
