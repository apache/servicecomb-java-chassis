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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.loadbalance.Configuration;
import org.apache.servicecomb.registry.discovery.AbstractDiscoveryFilter;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;

/**
 *  Instance property based filter
 */
public class InstancePropertyDiscoveryFilter extends AbstractDiscoveryFilter {
  private static final String MATCHED = "matched";

  @Override
  public int getOrder() {
    return 400;
  }

  @Override
  public boolean enabled() {
    return environment.getProperty("servicecomb.loadbalance.filter.instanceProperty.enabled",
        boolean.class, true);
  }

  @Override
  public boolean isGroupingFilter() {
    return false;
  }

  @Override
  protected void init(DiscoveryContext context, DiscoveryTreeNode parent) {
    List<StatefulDiscoveryInstance> matchedInstance = new ArrayList<>();
    Invocation invocation = context.getInputParameters();
    List<StatefulDiscoveryInstance> instances = parent.data();
    Map<String, String> filterOptions =
        Configuration.INSTANCE.getFlowsplitFilterOptions(invocation.getMicroserviceName());
    instances.forEach((target) -> {
      if (allowVisit(target, filterOptions)) {
        matchedInstance.add(target);
      }
    });
    parent.child(MATCHED, new DiscoveryTreeNode()
        .subName(parent, MATCHED)
        .data(matchedInstance));
  }

  @Override
  protected String findChildName(DiscoveryContext context, DiscoveryTreeNode parent) {
    return MATCHED;
  }

  protected boolean allowVisit(StatefulDiscoveryInstance instance, Map<String, String> filterOptions) {
    Map<String, String> propertiesMap = instance.getProperties();
    for (Entry<String, String> entry : filterOptions.entrySet()) {
      if (!entry.getValue().equals(propertiesMap.get(entry.getKey()))) {
        return false;
      }
    }
    return true;
  }
}
