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

package org.apache.servicecomb.loadbalance.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.loadbalance.LoadBalancer;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.loadbalancer.Server;

public class ZoneAwareServerListFilterExt implements ServerListFilterExt {
  private LoadBalancer loadBalancer;

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.loadbalance.filter.zoneaware.enabled", true).get();
  }

  @Override
  public void setLoadBalancer(LoadBalancer loadBalancer) {
    this.loadBalancer = loadBalancer;
  }

  @Override
  public int getOrder() {
    return 300;
  }

  @Override
  public List<Server> getFilteredListOfServers(List<Server> list, Invocation invocation) {
    List<Server> result = new ArrayList<>();
    MicroserviceInstance myself = RegistryUtils.getMicroserviceInstance();
    boolean find = false;
    for (Server server : list) {
      ServiceCombServer serviceCombServer = (ServiceCombServer) server;
      if (regionAndAZMatch(myself, serviceCombServer.getInstance())) {
        result.add(serviceCombServer);
        find = true;
      }
    }

    if (!find) {
      for (Server server : list) {
        ServiceCombServer serviceCombServer = (ServiceCombServer) server;
        if (regionMatch(myself, serviceCombServer.getInstance())) {
          result.add(serviceCombServer);
          find = true;
        }
      }
    }

    if (!find) {
      result = list;
    }
    return result;
  }

  private boolean regionAndAZMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (myself.getDataCenterInfo() != null && target.getDataCenterInfo() != null) {
      return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion()) &&
          myself.getDataCenterInfo().getAvailableZone().equals(target.getDataCenterInfo().getAvailableZone());
    }
    return false;
  }

  private boolean regionMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (myself.getDataCenterInfo() != null && target.getDataCenterInfo() != null) {
      return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion());
    }
    return false;
  }
}
