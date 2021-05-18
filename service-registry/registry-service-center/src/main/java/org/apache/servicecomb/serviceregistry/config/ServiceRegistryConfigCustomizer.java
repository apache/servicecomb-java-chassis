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

package org.apache.servicecomb.serviceregistry.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;

import com.netflix.config.DynamicPropertyFactory;

/**
 * easier way to have new customized copies of ServiceRegistryConfig
 */
public class ServiceRegistryConfigCustomizer {
  private ServiceRegistryConfig original;

  private ServiceRegistryConfigCustomizer(ServiceRegistryConfig original) {
    this.original = original;
  }

  public static ServiceRegistryConfigCustomizer from(ServiceRegistryConfig original) {
    return new ServiceRegistryConfigCustomizer(original);
  }

  public ServiceRegistryConfigCustomizer setClientName(String clientName) {
    this.original.setClientName(clientName);
    return this;
  }

  public ServiceRegistryConfigCustomizer setRegistryName(String registryName) {
    this.original.setRegistryName(registryName);
    return this;
  }

  public ServiceRegistryConfigCustomizer setWatchClientName(String watchClientName) {
    this.original.setWatchClientName(watchClientName);
    return this;
  }

  public ServiceRegistryConfigCustomizer addressListFromConfiguration(String configuration) {
    String address = DynamicPropertyFactory.getInstance()
        .getStringProperty(configuration, null)
        .get();
    if (address == null) {
      throw new IllegalStateException("service center address is required.");
    }
    String[] urls = address.split(",");
    List<String> uriList = Arrays.asList(urls);
    ArrayList<IpPort> ipPortList = new ArrayList<>();
    uriList.forEach(anUriList -> {
      try {
        URI uri = new URI(anUriList.trim());
        if ("https".equals(uri.getScheme())) {
          this.original.setSsl(true);
        }
        ipPortList.add(NetUtils.parseIpPort(uri));
      } catch (Exception e) {
        throw new IllegalStateException("service center address is required.", e);
      }
    });
    this.original.setIpPort(ipPortList);

    return this;
  }

  public ServiceRegistryConfig get() {
    return original;
  }
}
