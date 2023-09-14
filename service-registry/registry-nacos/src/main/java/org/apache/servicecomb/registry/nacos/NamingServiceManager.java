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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;

public class NamingServiceManager {
  private static volatile NamingService namingService;

  private static volatile NamingMaintainService namingMaintainService;

  public static NamingService buildNamingService(NacosDiscoveryProperties properties) throws NacosException {
    if (Objects.isNull(namingService)) {
      synchronized (NamingServiceManager.class) {
        if (Objects.isNull(namingService)) {
          namingService = new NacosNamingService(properties.getProperties());
        }
      }
    }
    return namingService;
  }

  public static NamingMaintainService buildNamingMaintainService(NacosDiscoveryProperties properties) throws NacosException {
    if (Objects.isNull(namingMaintainService)) {
      synchronized (NamingServiceManager.class) {
        if (Objects.isNull(namingMaintainService)) {
          namingMaintainService = new NacosNamingMaintainService(properties.getProperties());
        }
      }
    }
    return namingMaintainService;
  }
}
