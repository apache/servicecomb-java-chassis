/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.common.rest;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.BootListener;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.definition.loader.SchemaListener;
import io.servicecomb.serviceregistry.RegistryUtils;

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
