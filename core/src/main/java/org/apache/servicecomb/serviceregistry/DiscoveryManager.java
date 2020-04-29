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

package org.apache.servicecomb.serviceregistry;

import java.util.List;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;

public class DiscoveryManager {
  public static DiscoveryManager INSTANCE = new DiscoveryManager();

  private List<Discovery> discoveryList = SPIServiceUtils.getOrLoadSortedService(Discovery.class);

  private final AppManager appManager = new AppManager();

  private final MicroserviceDefinition microserviceDefinition;

  public DiscoveryManager() {
    MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader();
    microserviceDefinition = new MicroserviceDefinition(loader.getConfigModels());
  }

  public MicroserviceInstances findServiceInstances(String appId, String serviceName,
      String versionRule, String revision) {
    MicroserviceInstances result = new MicroserviceInstances();

    discoveryList
        .forEach(discovery -> {
          MicroserviceInstances microserviceInstances = discovery.findServiceInstances(appId, serviceName, versionRule);
          result.mergeMicroserviceInstances(microserviceInstances);
          discovery.setRevision(microserviceInstances.getRevision());
        });

    return result;
  }

  public AppManager getAppManager() {
    return this.appManager;
  }

  public MicroserviceVersions getOrCreateMicroserviceVersions(String appId, String microserviceName) {
    return appManager.getOrCreateMicroserviceVersions(appId, microserviceName);
  }

  public String getApplicationId() {
    return microserviceDefinition.getApplicationId();
  }

  public void destroy() {
    discoveryList.forEach(discovery -> discovery.destroy());
  }

  public void run() {
    discoveryList.forEach(discovery -> discovery.run());
  }
}
