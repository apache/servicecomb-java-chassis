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

package org.apache.servicecomb.registry.lightweight;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.lightweight.model.MicroserviceInstance;
import org.apache.servicecomb.registry.lightweight.model.MicroserviceInstances;
import org.apache.servicecomb.registry.lightweight.store.Store;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractLightweightDiscovery implements Discovery<ZeroConfigDiscoveryInstance> {
  protected EventBus eventBus;

  protected Store store;

  protected String revision;

  @Autowired
  public AbstractLightweightDiscovery setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
    return this;
  }

  @Autowired
  public AbstractLightweightDiscovery setStore(Store store) {
    this.store = store;
    return this;
  }

  @Override
  public void init() {
    eventBus.register(this);
  }

  @Override
  public void destroy() {
  }

  @Override
  public List<ZeroConfigDiscoveryInstance> findServiceInstances(String application, String serviceName) {
    MicroserviceInstances microserviceInstances =
        store.findServiceInstances(application, serviceName, "0");
    List<ZeroConfigDiscoveryInstance> result = new ArrayList<>();
    for (MicroserviceInstance instance : microserviceInstances.getInstancesResponse().getInstances()) {
      result.add(new ZeroConfigDiscoveryInstance(store.getMicroservice(instance.getServiceId()).get(), instance));
    }
    return result;
  }
}
