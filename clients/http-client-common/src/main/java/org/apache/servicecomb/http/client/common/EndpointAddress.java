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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class EndpointAddress {

  private static final Logger LOGGER = LoggerFactory.getLogger(EndpointAddress.class);

  private List<String> addresses = new ArrayList<>();

  private int index = 0;

  private volatile List<String> availableZone = new ArrayList<>();

  private volatile List<String> availableRegion = new ArrayList<>();

  private Cache<String, Boolean> availableIpCache = CacheBuilder.newBuilder()
      .maximumSize(100)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build();

  public EndpointAddress(List<String> addresses) {
    this.addresses.addAll(addresses);
  }

  public String getDefaultAddress() {
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

  public String getAvailableZoneAddress() {
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

  public void refreshCache() {
    availableZone.forEach(address -> availableIpCache.put(address, true));
    availableRegion.forEach(address -> availableIpCache.put(address, true));
  }

  public List<String> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<String> addresses) {
    this.addresses = addresses;
  }

  public List<String> getAvailableZone() {
    return availableZone;
  }

  public void setAvailableZone(List<String> availableZone) {
    this.availableZone = availableZone;
  }

  public List<String> getAvailableRegion() {
    return availableRegion;
  }

  public void setAvailableRegion(List<String> availableRegion) {
    this.availableRegion = availableRegion;
  }

  public Cache<String, Boolean> getAvailableIpCache() {
    return availableIpCache;
  }
}
