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

package org.apache.servicecomb.handler.governance;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryFilter;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.netflix.config.DynamicPropertyFactory;

public class InstanceIsolationDiscoveryFilter implements DiscoveryFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceIsolationDiscoveryFilter.class);

  private static final String KEY_ISOLATED = "isolated";

  private final Object lock = new Object();

  private final Map<String, Long> isolatedInstances = new ConcurrentHashMapEx<>();

  public InstanceIsolationDiscoveryFilter() {
    EventManager.register(this);
  }

  @Subscribe
  public void onInstanceIsolatedEvent(InstanceIsolatedEvent event) {
    synchronized (lock) {
      for (Iterator<String> iterator = isolatedInstances.keySet().iterator(); iterator.hasNext(); ) {
        Long duration = isolatedInstances.get(iterator.next());
        if (System.currentTimeMillis() - duration > 0) {
          iterator.remove();
        }
      }

      isolatedInstances.put(event.getInstanceId(),
          System.currentTimeMillis() + event.getWaitDurationInHalfOpenState().toMillis());
      LOGGER.info("isolate instance {} for {}ms", event.getInstanceId(),
          event.getWaitDurationInHalfOpenState().toMillis());
    }
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(
        "servicecomb.loadbalance.filter.instance.isolation.enabled", true).get();
  }

  @Override
  public int getOrder() {
    return Short.MAX_VALUE - 1;
  }

  @Override
  public boolean isGroupingFilter() {
    return true;
  }

  @Override
  public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
    Map<String, MicroserviceInstance> instances = parent.data();
    if (isolatedInstances.isEmpty() || instances.isEmpty()) {
      return parent;
    }

    boolean changed = false;
    Map<String, MicroserviceInstance> result = new HashMap<>(instances.size());
    for (Entry<String, MicroserviceInstance> item : instances.entrySet()) {
      Long duration = isolatedInstances.get(item.getKey());
      if (duration == null) {
        result.put(item.getKey(), item.getValue());
        continue;
      }

      if (System.currentTimeMillis() - duration < 0) {
        changed = true;
        continue;
      }

      synchronized (lock) {
        isolatedInstances.remove(item.getKey());
        LOGGER.info("try to recover instance {}", item.getKey());
      }
      result.put(item.getKey(), item.getValue());
    }

    if (!changed || result.size() == 0) {
      return parent;
    }

    // Create new child. And all later DiscoveryFilter will re-calculate based on this result.
    DiscoveryTreeNode child = new DiscoveryTreeNode().subName(parent, KEY_ISOLATED).data(result);
    parent.child(KEY_ISOLATED, child);
    return child;
  }
}
