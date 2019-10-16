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

package org.apache.servicecomb.it.edge;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PreLoadBootListener implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(PreLoadBootListener.class);

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public void onBootEvent(BootEvent bootEvent) {
    if (bootEvent.getEventType() == EventType.BEFORE_REGISTRY) {
      MicroserviceVersionRule rule = RegistryUtils.getServiceRegistry().getAppManager()
          .getOrCreateMicroserviceVersionRule(RegistryUtils.getAppId(), "it-producer", "0+");
      if (rule.getInstances().size() == 0) {
        LOGGER.warn("Prefetch not successful, maybe the provider not started.");
      }
    }
  }
}