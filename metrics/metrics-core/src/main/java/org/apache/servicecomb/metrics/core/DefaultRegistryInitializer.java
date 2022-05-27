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

import java.time.Duration;

import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;

import com.google.common.eventbus.EventBus;
import com.netflix.servo.DefaultMonitorRegistry;

public class DefaultRegistryInitializer implements MetricsInitializer {
  public static final String SERVO_POLLERS = "servo.pollers";

  private GlobalRegistry globalRegistry;

  @SuppressWarnings("deprecation")
  private com.netflix.spectator.servo.ServoRegistry registry;

  // create registry before init meters
  @Override
  public int getOrder() {
    return -10;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void init(GlobalRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    this.globalRegistry = globalRegistry;

    // spectator move poll gauges from inline to background executor
    // we need to set the interval to unify value
    System.setProperty("spectator.api.gaugePollingFrequency", Duration.ofMillis(config.getMsPollInterval()).toString());

    System.setProperty(SERVO_POLLERS, String.valueOf(config.getMsPollInterval()));
    registry = new com.netflix.spectator.servo.ServoRegistry();
    globalRegistry.add(registry);
  }

  @Override
  public void destroy() {
    if (registry != null) {
      DefaultMonitorRegistry.getInstance().unregister(registry);
      globalRegistry.remove(registry);
    }
  }
}
