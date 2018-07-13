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
import java.util.Map.Entry;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.loadbalance.Configuration;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.discovery.AbstractDiscoveryFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;

import com.netflix.config.DynamicPropertyFactory;

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
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.loadbalance.filter.instanceProperty.enabled", true).get();
  }

  @Override
  public boolean isGroupingFilter() {
    return false;
  }

  @Override
  protected void init(DiscoveryContext context, DiscoveryTreeNode parent) {
    Map<String, MicroserviceInstance> matchedInstance = new HashMap<>();
    Invocation invocation = context.getInputParameters();
    Map<String, MicroserviceInstance> instances = parent.data();
    Map<String, String> filterOptions =
        Configuration.INSTANCE.getFlowsplitFilterOptions(invocation.getMicroserviceName());
    for (String id : instances.keySet()) {
      MicroserviceInstance target = instances.get(id);
      if (allowVisit(target, filterOptions)) {
        matchedInstance.put(id, target);
      }
    }
    parent.child(MATCHED, new DiscoveryTreeNode()
        .subName(parent, MATCHED)
        .data(matchedInstance));
  }

  @Override
  protected String findChildName(DiscoveryContext context, DiscoveryTreeNode parent) {
    return MATCHED;
  }

  protected boolean allowVisit(MicroserviceInstance instance, Map<String, String> filterOptions) {
    Map<String, String> propertiesMap = instance.getProperties();
    for (Entry<String, String> entry : filterOptions.entrySet()) {
      if (!entry.getValue().equals(propertiesMap.get(entry.getKey()))) {
        return false;
      }
    }
    return true;
  }
}
