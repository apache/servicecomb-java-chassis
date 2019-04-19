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

import javax.inject.Inject;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.definition.schema.ProducerSchemaFactory;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InspectorBootListener implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(InspectorBootListener.class);

  @Inject
  private ProducerSchemaFactory producerSchemaFactory;

  private InspectorConfig inspectorConfig;

  @Override
  public int getOrder() {
    return Short.MAX_VALUE;
  }

  @Override
  public void onBootEvent(BootEvent event) {
    if (event.getEventType() != EventType.AFTER_TRANSPORT) {
      return;
    }

    inspectorConfig = event.getScbEngine().getPriorityPropertyManager().createConfigObject(InspectorConfig.class);
    if (!inspectorConfig.isEnabled()) {
      LOGGER.info("inspector is not enabled.");
      return;
    }

    LOGGER.info("inspector is enabled.");
    // will not register this schemas to service registry
    InspectorImpl inspector = new InspectorImpl(event.getScbEngine(), inspectorConfig,
        RegistryUtils.getServiceRegistry().getMicroservice().getSchemaMap());
    inspector.setPriorityPropertyManager(event.getScbEngine().getPriorityPropertyManager());
    producerSchemaFactory.getOrCreateProducerSchema("inspector",
        InspectorImpl.class,
        inspector);
  }
}
