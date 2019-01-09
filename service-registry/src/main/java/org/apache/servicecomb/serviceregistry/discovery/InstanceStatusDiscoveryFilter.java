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

package org.apache.servicecomb.serviceregistry.discovery;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;

import com.netflix.config.DynamicPropertyFactory;

public class InstanceStatusDiscoveryFilter extends AbstractDiscoveryFilter {
  private static final String UP_INSTANCES = "upInstances";

  @Override
  public int getOrder() {
    return -20000;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.loadbalance.filter.status.enabled", true).get();
  }

  @Override
  public boolean isGroupingFilter() {
    return true;
  }

  @Override
  protected String findChildName(DiscoveryContext context, DiscoveryTreeNode parent) {
    return UP_INSTANCES;
  }

  @Override
  public void init(DiscoveryContext context, DiscoveryTreeNode parent) {
    Map<String, MicroserviceInstance> instances = parent.data();
    Map<String, MicroserviceInstance> filteredServers = new HashMap<>();
    for (String key : instances.keySet()) {
      MicroserviceInstance instance = instances.get(key);
      if (MicroserviceInstanceStatus.UP == instance.getStatus()) {
        filteredServers.put(key, instance);
      }
    }

    if (filteredServers.isEmpty()) {
      return;
    }
    DiscoveryTreeNode child = new DiscoveryTreeNode().subName(parent, UP_INSTANCES).data(filteredServers);
    parent.child(UP_INSTANCES, child);
  }
}
