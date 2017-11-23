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

package io.servicecomb.foundation.metrics;

import javax.inject.Inject;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.core.BootListener;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.definition.loader.SchemaLoader;
import io.servicecomb.core.definition.schema.ProducerSchemaFactory;
import io.servicecomb.foundation.common.config.PaaSResourceUtils;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;

@Component
public class MetricsBootListener implements BootListener {

  private static final String MEM_ENABLED = "cse.metrics.mem.enabled";

  @Inject
  private ProducerSchemaFactory schemaFactory;

  @Inject
  private SchemaLoader schemaLoader;

  @Inject
  private MetricsObserverManager observerManager;

  @Override
  public void onBootEvent(BootEvent event) {
    //inject metrics provider before ProducerProviderManager init
    if (EventType.BEFORE_PRODUCER_PROVIDER.equals(event.getEventType())) {

      boolean memoryObserverEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty(MEM_ENABLED, false).get();
      if (memoryObserverEnabled) {
        Resource[] resources = PaaSResourceUtils.getResources("metrics.yaml");
        if (resources.length != 0) {
          Microservice microservice = RegistryUtils.getMicroservice();
          SchemaMeta meta = schemaLoader.registerSchema(microservice.getServiceName(), resources[0]);
          schemaFactory
              .getOrCreateProducerSchema(microservice.getServiceName(), meta.getSchemaId(), MetricsProviderImpl.class,
                  new MetricsProviderImpl(observerManager));
        }
      }
    }
  }
}
