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

package org.apache.servicecomb.registry.discovery;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance.HistoryStatus;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance.IsolationStatus;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance.PingStatus;

public class InstanceStatusDiscoveryFilter extends AbstractGroupDiscoveryFilter {
  public static final String PARAMETER = "status_level";

  public static final String GROUP_PREFIX = "status_group_";

  @Override
  public int getOrder() {
    return -10000;
  }

  @Override
  public boolean enabled() {
    return environment.getProperty("servicecomb.loadbalance.filter.status.enabled", Boolean.class, true);
  }

  @Override
  protected String contextParameter() {
    return PARAMETER;
  }

  @Override
  protected String groupPrefix() {
    return GROUP_PREFIX;
  }

  @Override
  public void init(DiscoveryContext context, DiscoveryTreeNode parent) {
    List<StatefulDiscoveryInstance> instances = parent.data();
    List<StatefulDiscoveryInstance> level0 = new ArrayList<>();
    List<StatefulDiscoveryInstance> level1 = new ArrayList<>();
    List<StatefulDiscoveryInstance> level2 = new ArrayList<>();
    List<StatefulDiscoveryInstance> level3 = new ArrayList<>();

    this.groups = 1;

    for (StatefulDiscoveryInstance instance : instances) {
      if (HistoryStatus.CURRENT == instance.getHistoryStatus() &&
          MicroserviceInstanceStatus.UP == instance.getMicroserviceInstanceStatus() &&
          PingStatus.OK == instance.getPingStatus() &&
          IsolationStatus.NORMAL == instance.getIsolationStatus()) {
        level0.add(instance);
        continue;
      }
      if (HistoryStatus.CURRENT == instance.getHistoryStatus() &&
          MicroserviceInstanceStatus.UP == instance.getMicroserviceInstanceStatus() &&
          PingStatus.UNKNOWN == instance.getPingStatus() &&
          IsolationStatus.NORMAL == instance.getIsolationStatus()) {
        level1.add(instance);
        continue;
      }
      if (HistoryStatus.HISTORY == instance.getHistoryStatus() &&
          MicroserviceInstanceStatus.UP == instance.getMicroserviceInstanceStatus() &&
          PingStatus.OK == instance.getPingStatus() &&
          IsolationStatus.NORMAL == instance.getIsolationStatus()) {
        level2.add(instance);
        continue;
      }
      level3.add(instance);
    }

    if (!level0.isEmpty()) {
      parent.child(GROUP_PREFIX + groups, new DiscoveryTreeNode()
          .subName(parent, GROUP_PREFIX + groups).data(level0));
      groups++;
    }
    if (!level1.isEmpty()) {
      parent.child(GROUP_PREFIX + groups, new DiscoveryTreeNode()
          .subName(parent, GROUP_PREFIX + groups).data(level1));
      groups++;
    }
    if (!level2.isEmpty()) {
      parent.child(GROUP_PREFIX + groups, new DiscoveryTreeNode()
          .subName(parent, GROUP_PREFIX + groups).data(level2));
      groups++;
    }
    parent.child(GROUP_PREFIX + groups, new DiscoveryTreeNode()
        .subName(parent, GROUP_PREFIX + groups).data(level3));
  }
}
