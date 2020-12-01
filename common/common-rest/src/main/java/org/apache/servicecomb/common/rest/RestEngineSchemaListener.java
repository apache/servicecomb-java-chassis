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

import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.definition.CoreMetaUtils;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.foundation.common.event.EnableExceptionPropagation;
import org.apache.servicecomb.registry.consumer.MicroserviceVersion;
import org.apache.servicecomb.registry.api.event.CreateMicroserviceVersionEvent;

import com.google.common.eventbus.Subscribe;

public class RestEngineSchemaListener implements BootListener {
  @Override
  public int getOrder() {
    return -10000;
  }

  @Override
  public void onBeforeRegistry(BootEvent event) {
    createServicePathManager(event.getScbEngine().getProducerMicroserviceMeta())
        .buildProducerPaths();
    event.getScbEngine().getEventBus().register(this);
  }

  @EnableExceptionPropagation
  @Subscribe
  public void onCreateMicroserviceVersion(CreateMicroserviceVersionEvent event) {
    MicroserviceVersion microserviceVersion = event.getMicroserviceVersion();
    MicroserviceMeta microserviceMeta = CoreMetaUtils.getMicroserviceMeta(microserviceVersion);
    createServicePathManager(microserviceMeta);
  }

  private ServicePathManager createServicePathManager(MicroserviceMeta microserviceMeta) {
    // already connect ServicePathManager and MicroserviceMeta instance
    // no need to save ServicePathManager instance again
    return new ServicePathManager(microserviceMeta);
  }
}
