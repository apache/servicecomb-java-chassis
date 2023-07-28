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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class InstanceStatusDiscoveryFilter extends AbstractDiscoveryFilter {
  public static final String PARAMETER = "status_level";

  public static final String INSTANCE_GROUP_KEY_PREFIX = "status_group_";

  public static final int MAX_GROUP = 3;

  private Environment environment;

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public int getOrder() {
    return -10000;
  }

  @Override
  public boolean enabled() {
    return environment.getProperty("servicecomb.loadbalance.filter.status.enabled", Boolean.class, true);
  }

  @Override
  public boolean isGroupingFilter() {
    return true;
  }

  @Override
  protected String findChildName(DiscoveryContext context, DiscoveryTreeNode parent) {
    Integer level = context.getContextParameter(PARAMETER);
    String group;
    if (level == null) {
      group = INSTANCE_GROUP_KEY_PREFIX + 0;
      context.pushRerunFilter();
      context.putContextParameter(PARAMETER, 0);
      return group;
    }

    level = level + 1;
    group = INSTANCE_GROUP_KEY_PREFIX + level;
    context.putContextParameter(PARAMETER, level);

    if (level < MAX_GROUP) {
      context.pushRerunFilter();
    }
    return group;
  }

  @Override
  public void init(DiscoveryContext context, DiscoveryTreeNode parent) {
    List<StatefulDiscoveryInstance> instances = parent.data();
    List<StatefulDiscoveryInstance> level0 = new ArrayList<>();
    List<StatefulDiscoveryInstance> level1 = new ArrayList<>();
    List<StatefulDiscoveryInstance> level2 = new ArrayList<>();
    List<StatefulDiscoveryInstance> level3 = new ArrayList<>();
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

    parent.child(INSTANCE_GROUP_KEY_PREFIX + 0, new DiscoveryTreeNode()
        .subName(parent, INSTANCE_GROUP_KEY_PREFIX + 0).data(level0));
    parent.child(INSTANCE_GROUP_KEY_PREFIX + 1, new DiscoveryTreeNode()
        .subName(parent, INSTANCE_GROUP_KEY_PREFIX + 1).data(level1));
    parent.child(INSTANCE_GROUP_KEY_PREFIX + 2, new DiscoveryTreeNode()
        .subName(parent, INSTANCE_GROUP_KEY_PREFIX + 2).data(level2));
    parent.child(INSTANCE_GROUP_KEY_PREFIX + 3, new DiscoveryTreeNode()
        .subName(parent, INSTANCE_GROUP_KEY_PREFIX + 3).data(level3));
  }
}
