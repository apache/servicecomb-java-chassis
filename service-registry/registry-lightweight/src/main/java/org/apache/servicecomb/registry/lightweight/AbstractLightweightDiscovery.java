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

import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.lightweight.store.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractLightweightDiscovery<D extends DiscoveryInstance> implements Discovery<D> {
  public static final String ZERO_CONFIG_NAME = "zero-config-discovery";

  public static final String ZERO_DISCOVERY_ENABLED = "servicecomb.registry.zero-config.%s.%s.enabled";

  protected EventBus eventBus;

  protected Store store;

  protected String revision;

  private Environment environment;

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

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
  public boolean enabled(String application, String serviceName) {
    return environment.getProperty(String.format(ZERO_DISCOVERY_ENABLED, application, serviceName),
        boolean.class, true);
  }
}
