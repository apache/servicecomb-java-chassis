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

package org.apache.servicecomb.common.rest;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.MicroserviceMetaManager;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.definition.loader.SchemaListener;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.springframework.stereotype.Component;

@Component
public class RestEngineSchemaListener implements SchemaListener, BootListener {
  private MicroserviceMetaManager microserviceMetaManager;

  @Inject
  public void setMicroserviceMetaManager(MicroserviceMetaManager microserviceMetaManager) {
    this.microserviceMetaManager = microserviceMetaManager;
  }

  @Override
  public void onBootEvent(BootEvent event) {
    if (!event.getEventType().equals(EventType.BEFORE_REGISTRY)) {
      return;
    }

    MicroserviceMeta microserviceMeta =
        microserviceMetaManager.getOrCreateMicroserviceMeta(RegistryUtils.getMicroservice());
    ServicePathManager servicePathManager = ServicePathManager.getServicePathManager(microserviceMeta);
    if (servicePathManager != null) {
      servicePathManager.buildProducerPaths();
    }
  }

  @Override
  public void onSchemaLoaded(SchemaMeta... schemaMetas) {
    // 此时相应的ServicePathManager可能正在被使用，为避免太高的复杂度，使用copy on write逻辑
    Map<String, ServicePathManager> mgrMap = new HashMap<>();
    for (SchemaMeta schemaMeta : schemaMetas) {
      MicroserviceMeta microserviceMeta = schemaMeta.getMicroserviceMeta();
      ServicePathManager mgr = findPathManager(mgrMap, microserviceMeta);
      mgr.addSchema(schemaMeta);
    }

    for (ServicePathManager mgr : mgrMap.values()) {
      // 对具有动态path operation进行排序
      mgr.sortPath();

      mgr.saveToMicroserviceMeta();
    }
  }

  protected ServicePathManager findPathManager(Map<String, ServicePathManager> mgrMap,
      MicroserviceMeta microserviceMeta) {
    ServicePathManager mgr = mgrMap.get(microserviceMeta.getName());
    if (mgr != null) {
      return mgr;
    }

    mgr = ServicePathManager.getServicePathManager(microserviceMeta);
    if (mgr == null) {
      mgr = new ServicePathManager(microserviceMeta);
    } else {
      mgr = mgr.cloneServicePathManager();
    }
    mgrMap.put(microserviceMeta.getName(), mgr);
    return mgr;
  }
}
