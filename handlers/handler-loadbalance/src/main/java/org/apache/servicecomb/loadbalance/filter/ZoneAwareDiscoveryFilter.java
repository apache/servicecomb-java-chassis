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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.discovery.AbstractDiscoveryFilter;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;

import com.netflix.config.DynamicPropertyFactory;


public class ZoneAwareDiscoveryFilter extends AbstractDiscoveryFilter {
  public static final String KEY_ZONE_AWARE_STEP = "_KEY_ZONE_AWARE_STEP";

  private static final String GROUP_RegionAndAZMatch = "instancesRegionAndAZMatch";

  private static final String GROUP_InstancesAZMatch = "instancesAZMatch";

  private static final String GROUP_InstancesNoMatch = "instancesNoMatch";

  public static final String GROUP_Instances_All = "instancesAll";

  @Override
  public int getOrder() {
    return 300;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.loadbalance.filter.zoneaware.enabled", true).get();
  }

  @Override
  public boolean isGroupingFilter() {
    return true;
  }

  @Override
  protected void init(DiscoveryContext context, DiscoveryTreeNode parent) {
    MicroserviceInstance myself = RegistrationManager.INSTANCE.getMicroserviceInstance();

    Map<String, MicroserviceInstance> instancesRegionAndAZMatch = new HashMap<>();
    Map<String, MicroserviceInstance> instancesAZMatch = new HashMap<>();
    Map<String, MicroserviceInstance> instancesNoMatch = new HashMap<>();
    Map<String, MicroserviceInstance> instances = parent.data();
    instances.forEach((id, target) -> {
      if (regionAndAZMatch(myself, target)) {
        instancesRegionAndAZMatch.put(id, target);
      } else if (regionMatch(myself, target)) {
        instancesAZMatch.put(id, target);
      } else {
        instancesNoMatch.put(id, target);
      }
    });
    Map<String, DiscoveryTreeNode> children = new HashMap<>();
    children.put(GROUP_RegionAndAZMatch, new DiscoveryTreeNode()
        .subName(parent, GROUP_RegionAndAZMatch)
        .data(instancesRegionAndAZMatch));
    children.put(GROUP_InstancesAZMatch, new DiscoveryTreeNode()
        .subName(parent, GROUP_InstancesAZMatch)
        .data(instancesAZMatch));
    children.put(GROUP_InstancesNoMatch, new DiscoveryTreeNode()
        .subName(parent, GROUP_InstancesNoMatch)
        .data(instancesNoMatch));
    children.put(GROUP_Instances_All, new DiscoveryTreeNode()
        .subName(parent, GROUP_Instances_All)
        .data(instances));
    parent.children(children);
  }

  @Override
  protected String findChildName(DiscoveryContext context, DiscoveryTreeNode parent) {
    String key = context.getContextParameter(KEY_ZONE_AWARE_STEP);
    if (key == null) {
      key = GROUP_RegionAndAZMatch;
      context.pushRerunFilter();
    } else if (GROUP_RegionAndAZMatch.equals(key)) {
      key = GROUP_InstancesAZMatch;
      context.pushRerunFilter();
    } else if (GROUP_InstancesAZMatch.equals(key)) {
      key = GROUP_InstancesNoMatch;
      context.pushRerunFilter();
    } else if (GROUP_InstancesNoMatch.equals(key)) {
      key = GROUP_Instances_All;
    } else {
      throw new ServiceCombException("not possible happen, maybe a bug.");
    }
    context.putContextParameter(KEY_ZONE_AWARE_STEP, key);
    return key;
  }

  private boolean regionAndAZMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (myself.getDataCenterInfo() == null) {
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
