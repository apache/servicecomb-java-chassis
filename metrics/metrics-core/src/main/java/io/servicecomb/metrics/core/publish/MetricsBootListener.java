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

package io.servicecomb.metrics.core.publish;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.core.BootListener;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.definition.loader.SchemaLoader;
import io.servicecomb.core.definition.schema.ProducerSchemaFactory;
import io.servicecomb.foundation.common.config.PaaSResourceUtils;
import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;

@Component
public class MetricsBootListener implements BootListener {

  private static final String PUBLISH_CLASS_NAME = "servicecomb.metrics.publish.class";

  private static final String METRICS_CONTRACT_DEFINITION_FILE = "servicecomb_internal_metrics_contract_definition.yaml";

  private final ProducerSchemaFactory schemaFactory;

  private final SchemaLoader schemaLoader;

  private final MetricsPublisher metricsPublisher;

  @Autowired
  public MetricsBootListener(ProducerSchemaFactory schemaFactory, SchemaLoader schemaLoader,
      MetricsPublisher metricsPublisher) {
    this.schemaFactory = schemaFactory;
    this.schemaLoader = schemaLoader;
    this.metricsPublisher = metricsPublisher;
  }

  @Override
  public void onBootEvent(BootEvent event) {
    //inject metrics provider before ProducerProviderManager init
    if (EventType.BEFORE_PRODUCER_PROVIDER.equals(event.getEventType())) {

      String publisherClassName = DynamicPropertyFactory.getInstance()
          .getStringProperty(PUBLISH_CLASS_NAME, JsonMetricsPublisher.class.getName()).get();

      Resource[] resources = PaaSResourceUtils.getResources(METRICS_CONTRACT_DEFINITION_FILE);
      if (resources.length != 0) {
        Microservice microservice = RegistryUtils.getMicroservice();
        try {
          String swaggerContent = IOUtils.toString(resources[0].getURL());
          SchemaMeta meta = schemaLoader
              .registerSchema(microservice.getServiceName(), "metricsEndpoint", swaggerContent);
          schemaFactory
              .getOrCreateProducerSchema(microservice.getServiceName(), meta.getSchemaId(),
                  Class.forName(publisherClassName),
                  metricsPublisher);
        } catch (ClassNotFoundException e) {
          throw new ServiceCombException("unable found publish class", e);
        } catch (IOException e) {
          throw new ServiceCombException("unable load metrics contract definition file", e);
        }
      }
    }
  }
}
