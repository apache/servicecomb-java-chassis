/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.loadbalance;

import com.netflix.loadbalancer.Server;

import io.servicecomb.core.Transport;
import io.servicecomb.core.endpoint.AbstractEndpointsCache;
import io.servicecomb.serviceregistry.cache.CacheEndpoint;

public class ServerListCache extends AbstractEndpointsCache<Server> {

  public ServerListCache(String appId, String microserviceName, String microserviceVersionRule,
      String transportName) {
    super(appId, microserviceName, microserviceVersionRule, transportName);
  }

  @Override
  protected Server createEndpoint(Transport transport, CacheEndpoint cacheEndpoint) {
    return new CseServer(transport, cacheEndpoint);
  }
}
