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

package org.apache.servicecomb.localregistry;


import java.util.List;

import org.apache.servicecomb.registry.api.Discovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class LocalDiscovery implements Discovery<LocalDiscoveryInstance> {
  public static final String LOCAL_DISCOVERY_NAME = "local-discovery";

  public static final String LOCAL_DISCOVERY_ENABLED = "servicecomb.registry.local.enabled.%s.%s";

  private LocalRegistryStore localRegistryStore;

  private Environment environment;

  @Autowired
  public void setLocalRegistryStore(LocalRegistryStore localRegistryStore) {
    this.localRegistryStore = localRegistryStore;
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public int getOrder() {
    return -10000;
  }

  @Override
  public String name() {
    return LOCAL_DISCOVERY_NAME;
  }

  @Override
  public boolean enabled(String application, String serviceName) {
    return environment.getProperty(String.format(LOCAL_DISCOVERY_ENABLED, application, serviceName),
        boolean.class, true);
  }

  @Override
  public List<LocalDiscoveryInstance> findServiceInstances(String application, String serviceName) {
    return this.localRegistryStore.findServiceInstances(application, serviceName);
  }

  @Override
  public void setInstanceChangedListener(InstanceChangedListener<LocalDiscoveryInstance> instanceChangedListener) {

  }

  @Override
  public void init() {
    this.localRegistryStore.init();
  }

  @Override
  public void run() {
    this.localRegistryStore.run();
  }

  @Override
  public void destroy() {
  }

  @Override
  public boolean enabled() {
    return this.environment.getProperty(Const.LOCAL_REGISTRY_ENABLED, boolean.class, true);
  }
}
