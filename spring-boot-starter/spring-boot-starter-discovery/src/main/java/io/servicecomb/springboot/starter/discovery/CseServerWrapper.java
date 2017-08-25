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
package io.servicecomb.springboot.starter.discovery;

import io.servicecomb.core.Transport;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.loadbalance.CseServer;
import io.servicecomb.serviceregistry.cache.CacheEndpoint;

public class CseServerWrapper extends CseServer {

  public CseServerWrapper(Transport transport, CacheEndpoint cacheEndpoint) {
    super(transport, cacheEndpoint);
  }

  // used in LoadBalancerContext
  public String getHost() {
    URIEndpointObject host = (URIEndpointObject) getEndpoint().getAddress();
    return host.getHostOrIp();
  }

  public int getPort() {
    URIEndpointObject host = (URIEndpointObject) getEndpoint().getAddress();
    return host.getPort();
  }
}
