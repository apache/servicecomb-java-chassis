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

package org.apache.servicecomb.loadbalance.filterext;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;

import com.netflix.config.DynamicPropertyFactory;

public class ZoneAwareDiscoveryFilter implements ServerListFilterExt {
  public static final String CONFIG_RATIO = "servicecomb.loadbalance.filter.zoneaware.ratio";

  public static final String CONFIG_RATIO_CEILING = "servicecomb.loadbalance.filter.zoneaware.ratioCeiling";

  @Override
  public int getOrder() {
    return ORDER_ZONE_AWARE;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty(ZONE_AWARE_FILTER_ENABLED, true)
        .get();
  }

  private int getRatio() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty(CONFIG_RATIO, 30).get();
  }

  private int getRatioCeiling(int defaultValue) {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty(CONFIG_RATIO_CEILING, defaultValue).get();
  }

  @Override
  public List<ServiceCombServer> getFilteredListOfServers(List<ServiceCombServer> servers,
      Invocation invocation) {
    MicroserviceInstance myself = RegistrationManager.INSTANCE.getMicroserviceInstance();

    List<ServiceCombServer> instancesRegionAndAZMatch = new ArrayList<>();
    List<ServiceCombServer> instancesAZMatch = new ArrayList<>();
    List<ServiceCombServer> instancesNoMatch = new ArrayList<>();
    servers.forEach((server) -> {
      if (regionAndAZMatch(myself, server.getInstance())) {
        instancesRegionAndAZMatch.add(server);
      } else if (regionMatch(myself, server.getInstance())) {
        instancesAZMatch.add(server);
      } else {
        instancesNoMatch.add(server);
      }
    });

    int ratio = getRatio();
    int ratioCeiling = getRatioCeiling(100 - ratio);

    if (hasEnoughMembers(servers.size(), instancesRegionAndAZMatch.size(), ratio, ratioCeiling)) {
      return instancesRegionAndAZMatch;
    } else {
      instancesAZMatch.addAll(instancesRegionAndAZMatch);
    }

    if (hasEnoughMembers(servers.size(), instancesAZMatch.size(), ratio, ratioCeiling)) {
      return instancesAZMatch;
    } else {
      instancesNoMatch.addAll(instancesAZMatch);
    }
    return instancesNoMatch;
  }

  private boolean hasEnoughMembers(int totalSize, int groupSize, int ratio, int ratioCeiling) {
    if (totalSize == 0 || groupSize == 0) {
      return false;
    }
    int actual = Math.floorDiv(groupSize * 100, totalSize);
    return actual >= ratio && actual <= ratioCeiling;
  }

  private boolean regionAndAZMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (myself == null || myself.getDataCenterInfo() == null) {
      // when instance have no datacenter info, it will match all other datacenters
      return true;
    }
    if (target.getDataCenterInfo() != null) {
      return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion()) &&
          myself.getDataCenterInfo().getAvailableZone().equals(target.getDataCenterInfo().getAvailableZone());
    }
    return false;
  }

  private boolean regionMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (target.getDataCenterInfo() != null) {
      return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion());
    }
    return false;
  }
}
