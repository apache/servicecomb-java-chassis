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

package org.apache.servicecomb.core.provider.producer;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.ProducerProvider;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.definition.SchemaUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProducerProviderManager implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerProviderManager.class);

  @Autowired(required = false)
  private List<ProducerProvider> producerProviderList = Collections.emptyList();

  private MicroserviceMeta microserviceMeta;

  public void init() throws Exception {
    for (ProducerProvider provider : producerProviderList) {
      provider.init();
    }
    Microservice microservice = RegistryUtils.getMicroservice();
    microserviceMeta = SCBEngine.getInstance().getProducerMicroserviceMeta();
    for (SchemaMeta schemaMeta : microserviceMeta.getSchemaMetas()) {
      String content = SchemaUtils.swaggerToString(schemaMeta.getSwagger());
      microservice.addSchema(schemaMeta.getSchemaId(), content);
    }
  }

  @Override
  public void onBootEvent(BootEvent event) {
    if (!EventType.AFTER_CLOSE.equals(event.getEventType())) {
      return;
    }

    for (OperationMeta operationMeta : microserviceMeta.getOperations()) {
      if (ExecutorService.class.isInstance(operationMeta.getExecutor())) {
        ((ExecutorService) operationMeta.getExecutor()).shutdown();
        continue;
      }

      if (Closeable.class.isInstance(operationMeta.getExecutor())) {
        IOUtils.closeQuietly((Closeable) operationMeta.getExecutor());
        continue;
      }

      LOGGER.warn("Executor {} do not support close or shutdown, it may block service shutdown.",
          operationMeta.getExecutor().getClass().getName());
    }
  }
}
