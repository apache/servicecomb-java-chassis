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

package org.apache.servicecomb.registry.nacos;

import java.util.Objects;
import java.util.Properties;

import org.apache.servicecomb.config.BootStrapProperties;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.NacosNamingService;

public class NamingServiceManager {
  private static volatile NamingService namingService;

  public static NamingService buildNamingService(Environment environment, NacosDiscoveryProperties properties) {
    if (Objects.isNull(namingService)) {
      synchronized (NamingServiceManager.class) {
        if (Objects.isNull(namingService)) {
          try {
            namingService = new NacosNamingService(getProperties(environment, properties));
          } catch (NacosException e) {
            throw new IllegalStateException("build namingService failed.");
          }
        }
      }
    }
    return namingService;
  }

  private static Properties getProperties(Environment environment,
      NacosDiscoveryProperties nacosDiscoveryProperties) {
    Properties properties = new Properties();
    properties.put(NacosConst.NAMESPACE, BootStrapProperties.readServiceEnvironment(environment));
    properties.put(NacosConst.SERVER_ADDR, nacosDiscoveryProperties.getServerAddr());
    if (nacosDiscoveryProperties.getUsername() != null) {
      properties.put(NacosConst.USERNAME, nacosDiscoveryProperties.getUsername());
    }
    if (nacosDiscoveryProperties.getPassword() != null) {
      properties.put(NacosConst.PASSWORD, nacosDiscoveryProperties.getPassword());
    }
    if (nacosDiscoveryProperties.getAccessKey() != null) {
      properties.put(NacosConst.ACCESS_KEY, nacosDiscoveryProperties.getAccessKey());
    }
    if (nacosDiscoveryProperties.getSecretKey() != null) {
      properties.put(NacosConst.SECRET_KEY, nacosDiscoveryProperties.getSecretKey());
    }
    if (nacosDiscoveryProperties.getLogName() != null) {
      properties.put(NacosConst.NACOS_NAMING_LOG_NAME, nacosDiscoveryProperties.getLogName());
    }

    properties.put(NacosConst.CLUSTER_NAME, nacosDiscoveryProperties.getClusterName());
    properties.put(NacosConst.NAMING_LOAD_CACHE_AT_START, nacosDiscoveryProperties.getNamingLoadCacheAtStart());
    return properties;
  }
}
