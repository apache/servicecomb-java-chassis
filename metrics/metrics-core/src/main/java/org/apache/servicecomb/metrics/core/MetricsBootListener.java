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
package org.apache.servicecomb.metrics.core;

import javax.inject.Inject;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.definition.schema.ProducerSchemaFactory;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrap;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.metrics.core.publish.HealthCheckerRestPublisher;
import org.apache.servicecomb.metrics.core.publish.MetricsRestPublisher;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.springframework.stereotype.Component;

import com.netflix.spectator.api.Spectator;

@Component
public class MetricsBootListener implements BootListener {
  private MetricsBootstrap metricsBootstrap = new MetricsBootstrap();

  @Inject
  private ProducerSchemaFactory producerSchemaFactory;

  @Override
  public void onBootEvent(BootEvent event) {
    switch (event.getEventType()) {
      case BEFORE_PRODUCER_PROVIDER:
        registerSchemas();
        break;
      case AFTER_REGISTRY:
        metricsBootstrap.start(Spectator.globalRegistry(), EventManager.getEventBus());
        break;
      case BEFORE_CLOSE:
        metricsBootstrap.shutdown();
      default:
        break;
    }
  }

  private void registerSchemas() {
    Microservice microservice = RegistryUtils.getMicroservice();

    producerSchemaFactory.getOrCreateProducerSchema(microservice.getServiceName(),
        "healthEndpoint",
        HealthCheckerRestPublisher.class,
        new HealthCheckerRestPublisher());

    MetricsRestPublisher metricsRestPublisher =
        SPIServiceUtils.getTargetService(MetricsInitializer.class, MetricsRestPublisher.class);
    producerSchemaFactory.getOrCreateProducerSchema(microservice.getServiceName(),
        "metricsEndpoint",
        metricsRestPublisher.getClass(),
        metricsRestPublisher);
  }
}
