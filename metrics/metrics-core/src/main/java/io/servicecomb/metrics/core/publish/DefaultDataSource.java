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

package io.servicecomb.metrics.core.publish;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.monitor.Pollers;

import io.servicecomb.metrics.common.RegistryMetric;
import io.servicecomb.metrics.core.monitor.RegistryMonitor;

@Component
public class DefaultDataSource implements DataSource {
  private static final String METRICS_POLLING_TIME = "servicecomb.metrics.window_time";

  private final RegistryMonitor registryMonitor;

  @Autowired
  public DefaultDataSource(RegistryMonitor registryMonitor) {
    this(registryMonitor, DynamicPropertyFactory.getInstance().getStringProperty(METRICS_POLLING_TIME, "5000").get());
  }

  public DefaultDataSource(RegistryMonitor registryMonitor, String pollingSettings) {
    this.registryMonitor = registryMonitor;
    System.getProperties().setProperty("servo.pollers", pollingSettings);
  }

  @Override
  public RegistryMetric getRegistryMetric() {
    return getRegistryMetric(0);
  }

  @Override
  public RegistryMetric getRegistryMetric(int windowTimeIndex) {
    return registryMonitor.toRegistryMetric(windowTimeIndex);
  }

  @Override
  public List<Long> getAppliedWindowTime() {
    return Pollers.getPollingIntervals();
  }
}
