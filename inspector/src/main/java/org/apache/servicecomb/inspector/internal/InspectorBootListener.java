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
package org.apache.servicecomb.inspector.internal;

import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.core.BootListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class InspectorBootListener implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(InspectorBootListener.class);

  private final InspectorConfig inspectorConfig;

  private MicroserviceProperties microserviceProperties;

  public InspectorBootListener(InspectorConfig inspectorConfig) {
    this.inspectorConfig = inspectorConfig;
  }

  @Autowired
  public void setMicroserviceProperties(MicroserviceProperties microserviceProperties) {
    this.microserviceProperties = microserviceProperties;
  }

  @Override
  public int getOrder() {
    return Short.MAX_VALUE;
  }

  @Override
  public void onAfterTransport(BootEvent event) {
    if (!inspectorConfig.isEnabled()) {
      LOGGER.info("inspector is not enabled.");
      return;
    }
    LOGGER.info("inspector is enabled.");

    // will not register this schemas to service registry
    InspectorImpl inspector = new InspectorImpl()
        .setServiceName(microserviceProperties.getName());
    // TODO: inspector get schemas
//        .setSchemas(RegistrationManager.INSTANCE.getMicroservice().getSchemaMap())
//        .correctBasePathForOnlineTest(event.getScbEngine());
    event.getScbEngine().getProducerProviderManager().registerSchema("inspector", inspector);
  }
}
