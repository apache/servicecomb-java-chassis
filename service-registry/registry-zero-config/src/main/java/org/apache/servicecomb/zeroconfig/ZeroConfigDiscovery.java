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
package org.apache.servicecomb.zeroconfig;

import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.CFG_ENABLED;
import static org.apache.servicecomb.zeroconfig.ZeroConfigConst.ORDER;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.consumer.AppManager;
import org.apache.servicecomb.registry.lightweight.AbstractLightDiscovery;
import org.apache.servicecomb.registry.lightweight.DiscoveryClient;
import org.apache.servicecomb.registry.lightweight.SchemaChangedEvent;
import org.apache.servicecomb.registry.lightweight.store.Store;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.netflix.config.DynamicPropertyFactory;

@Component
public class ZeroConfigDiscovery extends AbstractLightDiscovery {
  private static final String NAME = "zero-config discovery";

  private final AppManager appManager;

  public ZeroConfigDiscovery(Store store, DiscoveryClient discoveryClient, EventBus eventBus, AppManager appManager) {
    super(store);
    this.appManager = appManager;

    Executors
        .newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "zero-config-discovery"))
        .scheduleAtFixedRate(appManager::safePullInstances, 0, 3, TimeUnit.SECONDS);
    eventBus.register(this);
  }

  @Subscribe
  public void onSchemaChanged(SchemaChangedEvent event) {
    Microservice microservice = event.getMicroservice();
    appManager.markWaitingDelete(microservice.getAppId(), microservice.getServiceName());
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(CFG_ENABLED, true).get();
  }
}
