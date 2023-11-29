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
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrap;

public class MetricsBootListener implements BootListener {
  private final MetricsBootstrap metricsBootstrap;

  public MetricsBootListener(MetricsBootstrap metricsBootstrap) {
    this.metricsBootstrap = metricsBootstrap;
  }

  @Override
  public void onBeforeProducerProvider(BootEvent event) {

  }

  public MetricsBootstrap getMetricsBootstrap() {
    return metricsBootstrap;
  }

  @Override
  public void onAfterRegistry(BootEvent event) {
    metricsBootstrap.start(EventManager.getEventBus());
  }

  @Override
  public void onBeforeClose(BootEvent event) {
    metricsBootstrap.shutdown();
  }
}
