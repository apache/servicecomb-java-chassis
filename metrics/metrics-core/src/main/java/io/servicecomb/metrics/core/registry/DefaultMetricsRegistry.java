/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.core.registry;

import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.metrics.core.metric.RegistryMetric;
import io.servicecomb.metrics.core.monitor.RegistryMonitor;

@Component
public class DefaultMetricsRegistry implements MetricsRegistry {

  private static final String METRICS_POLLING = "servicecomb.metrics.polling";

  private final RegistryMonitor registryMonitor;

  public DefaultMetricsRegistry() {
    this(DynamicPropertyFactory.getInstance().getStringProperty(METRICS_POLLING, "10000").get());
  }

  public DefaultMetricsRegistry(String pollingSetting) {
    System.getProperties().setProperty("servo.pollers", pollingSetting);
    this.registryMonitor = new RegistryMonitor();
  }

  @Override
  public RegistryMonitor getRegistryMonitor() {
    return registryMonitor;
  }

  @Override
  public RegistryMetric getRegistryMetric(int pollerIndex) {
    return new RegistryMetric(registryMonitor, pollerIndex);
  }
}

