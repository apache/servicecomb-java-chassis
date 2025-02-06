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

import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.config.DynamicProperties;
import org.apache.servicecomb.registry.discovery.AbstractGroupDiscoveryFilter;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class ZoneAwareDiscoveryFilter extends AbstractGroupDiscoveryFilter {
  public static final String PARAMETER = "zone_aware_level";

  public static final String GROUP_PREFIX = "zone_aware_group_";

  public static final String GROUP_SIZE = "zone_aware_group_size";

  public static final String CONFIG_ENABLED = "servicecomb.loadbalance.filter.zoneaware.enabled";

  public static final String CONFIG_RATIO = "servicecomb.loadbalance.filter.zoneaware.ratio";

  public static final String CONFIG_RATIO_CEILING = "servicecomb.loadbalance.filter.zoneaware.ratioCeiling";

  private DataCenterProperties dataCenterProperties;

  private Boolean configEnabled;

  private DynamicProperties dynamicProperties;

  @Autowired
  public void setDynamicProperties(DynamicProperties dynamicProperties) {
    this.dynamicProperties = dynamicProperties;
  }

  @Autowired
  @SuppressWarnings("unused")
  public void setDataCenterProperties(DataCenterProperties dataCenterProperties) {
    this.dataCenterProperties = dataCenterProperties;
  }

  @Override
  public int getOrder() {
    return -9000;
  }

  @Override
  public boolean enabled() {

    if (configEnabled == null) {
      configEnabled = dynamicProperties.getBooleanProperty(CONFIG_ENABLED,
          value -> configEnabled = value,
          true);
    }
    return configEnabled;
  }

  private int getRatio() {
    return environment.getProperty(CONFIG_RATIO,
        int.class, 30);
  }

  private int getRatioCeiling(int defaultValue) {
    return environment.getProperty(CONFIG_RATIO_CEILING,
        int.class, defaultValue);
  }

  @Override
  protected String contextParameter() {
    return PARAMETER;
  }

  @Override
  protected String groupsSizeParameter() {
    return GROUP_SIZE;
  }

  @Override
  protected String groupPrefix() {
    return GROUP_PREFIX;
  }

  @Override
  public void init(DiscoveryContext context, DiscoveryTreeNode parent) {
    List<StatefulDiscoveryInstance> instances = parent.data();
    List<StatefulDiscoveryInstance> instancesRegionAndAZMatch = new ArrayList<>();
    List<StatefulDiscoveryInstance> instancesAZMatch = new ArrayList<>();
    List<StatefulDiscoveryInstance> instancesNoMatch = new ArrayList<>();

    int groups = 1;

    for (StatefulDiscoveryInstance server : instances) {
      if (regionAndAZMatch(server)) {
        instancesRegionAndAZMatch.add(server);
      } else if (regionMatch(server)) {
        instancesAZMatch.add(server);
      } else {
        instancesNoMatch.add(server);
      }
    }

    int ratio = getRatio();
    int ratioCeiling = getRatioCeiling(100 - ratio);

    if (hasEnoughMembers(instances.size(), instancesRegionAndAZMatch.size(), ratio, ratioCeiling)) {
      parent.child(GROUP_PREFIX + groups, new DiscoveryTreeNode()
          .subName(parent, GROUP_PREFIX + groups).data(instancesRegionAndAZMatch));
      groups++;
    } else {
      instancesAZMatch.addAll(instancesRegionAndAZMatch);
    }

    if (hasEnoughMembers(instances.size(), instancesAZMatch.size(), ratio, ratioCeiling)) {
      parent.child(GROUP_PREFIX + groups, new DiscoveryTreeNode()
          .subName(parent, GROUP_PREFIX + groups).data(instancesAZMatch));
      groups++;
    } else {
      instancesNoMatch.addAll(instancesAZMatch);
    }

    parent.child(GROUP_PREFIX + groups, new DiscoveryTreeNode()
        .subName(parent, GROUP_PREFIX + groups).data(instancesNoMatch));

    parent.attribute(GROUP_SIZE, groups);
  }

  private boolean hasEnoughMembers(int totalSize, int groupSize, int ratio, int ratioCeiling) {
    if (totalSize == 0 || groupSize == 0) {
      return false;
    }
    int actual = Math.floorDiv(groupSize * 100, totalSize);
    return actual >= ratio && actual <= ratioCeiling;
  }

  private boolean regionAndAZMatch(StatefulDiscoveryInstance target) {
    if (dataCenterProperties.getRegion() != null
        && dataCenterProperties.getAvailableZone() != null && target.getDataCenterInfo() != null) {
      return dataCenterProperties.getRegion()
          .equals(target.getDataCenterInfo().getRegion()) &&
          dataCenterProperties.getAvailableZone()
              .equals(target.getDataCenterInfo().getAvailableZone());
    }
    return false;
  }

  private boolean regionMatch(StatefulDiscoveryInstance target) {
    if (dataCenterProperties.getRegion() != null && target.getDataCenterInfo() != null) {
      return dataCenterProperties.getRegion()
          .equals(target.getDataCenterInfo().getRegion());
    }
    return false;
  }
}
