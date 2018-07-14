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

import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;

import com.google.common.eventbus.EventBus;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.servo.ServoRegistry;

public class DefaultRegistryInitializer implements MetricsInitializer {
  public static final String SERVO_POLLERS = "servo.pollers";

  private CompositeRegistry globalRegistry;

  private ServoRegistry registry;

  // create registry before init meters
  @Override
  public int getOrder() {
    return -10;
  }

  @Override
  public void init(CompositeRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    this.globalRegistry = globalRegistry;

    System.getProperties().setProperty(SERVO_POLLERS, String.valueOf(config.getMsPollInterval()));
    registry = new ServoRegistry();

    globalRegistry.add(registry);
  }

  @Override
  public void destroy() {
    if (registry != null) {
      DefaultMonitorRegistry.getInstance().unregister(registry);
      globalRegistry.remove(registry);
    }
  }

  public Registry getRegistry() {
    return registry;
  }
}
