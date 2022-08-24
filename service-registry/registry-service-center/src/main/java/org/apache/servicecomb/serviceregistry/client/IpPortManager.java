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

package org.apache.servicecomb.serviceregistry.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.registry.cache.InstanceCacheManager;
import org.apache.servicecomb.registry.cache.InstanceCacheManagerNew;
import org.apache.servicecomb.registry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.api.Type;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.refresh.ServiceRegistryAddressManager;
import org.apache.servicecomb.serviceregistry.refresh.ClassificationAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpPortManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(IpPortManager.class);

  private final ServiceRegistryConfig serviceRegistryConfig;

  InstanceCacheManager instanceCacheManager;

  private final ArrayList<IpPort> defaultIpPort;

  private boolean autoDiscoveryInited = false;

  private final ServiceRegistryAddressManager addressManger;

  ClassificationAddress classificationAddress;

  private final Object lock = new Object();

  public void setAutoDiscoveryInited(boolean autoDiscoveryInited) {
    this.autoDiscoveryInited = autoDiscoveryInited;
  }

  public int getMaxRetryTimes() {
    return classificationAddress.getMaxRetryTimes();
  }

  public IpPortManager(ServiceRegistryConfig serviceRegistryConfig) {
    this.serviceRegistryConfig = serviceRegistryConfig;
    this.instanceCacheManager = new InstanceCacheManagerNew(new AppManager());
    defaultIpPort = serviceRegistryConfig.getIpPort();
    if (defaultIpPort.isEmpty()) {
      throw new IllegalArgumentException("Service center address is required to start the application.");
    }
    List<String> addresses = defaultIpPort.stream().map(IpPort::toString).collect(Collectors.toList());
    addressManger = new ServiceRegistryAddressManager(addresses, EventManager.getEventBus());
    classificationAddress = new ClassificationAddress(serviceRegistryConfig, instanceCacheManager);
    LOGGER.info("Initial service center address is {}", getAvailableAddress());
  }

  // we have to do this operation after the first time setup has already done
  public void initAutoDiscovery() {
    if (!autoDiscoveryInited && this.serviceRegistryConfig.isRegistryAutoDiscovery()) {
      for (Type type : Type.values()) {
        classificationAddress.initEndPoint(type.name());
      }
      setAutoDiscoveryInited(true);
    }
  }

  public IpPort getAvailableAddress() {
    return addressManger.getAvailableIpPort();
  }

  public void recordState(String address) {
    addressManger.recordFailState(address);
  }
}
