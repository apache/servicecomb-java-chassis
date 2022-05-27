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

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.metrics.core.publish.HealthCheckerRestPublisher;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

@Component
public class HealthBootListener implements BootListener {
  @Override
  public void onBeforeProducerProvider(BootEvent event) {
    if (!DynamicPropertyFactory.getInstance().getBooleanProperty("servicecomb.health.endpoint.enabled", true).get()) {
      return;
    }

    event.getScbEngine().getProducerProviderManager()
        .addProducerMeta("healthEndpoint", new HealthCheckerRestPublisher());
  }
}
