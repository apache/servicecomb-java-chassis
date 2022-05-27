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

package org.apache.servicecomb.demo.multiServiceCenterClient;

import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;

import com.netflix.config.DynamicPropertyFactory;

public class ServerBRegistryWatchHttpClientOptionsSPI extends ServerBRegistryHttpClientOptionsSPI {
  public static final String CLIENT_NAME = "registry-watch-serverB";

  private ServiceRegistryConfig serviceRegistryConfig = ServiceRegistryConfig.INSTANCE;

  @Override
  public String clientName() {
    return CLIENT_NAME;
  }

  @Override
  public int getOrder() {
    // low priority than registry
    return super.getOrder() + 1;
  }

  @Override
  public boolean enabled() {
    return serviceRegistryConfig.isWatch();
  }

  @Override
  public boolean isWorker() {
    return true;
  }


  @Override
  public String getWorkerPoolName() {
    return "pool-worker-service-center-watch-serverB";
  }

  @Override
  public int getWorkerPoolSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty(ServiceRegistryConfig.WORKER_POOL_SIZE, 2).get();
  }

  @Override
  public boolean isProxyEnable() {
    return false;
  }
}
