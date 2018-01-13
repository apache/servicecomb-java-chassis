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

package org.apache.servicecomb.metrics.core.monitor;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.metrics.common.ConsumerInvocationMetric;
import org.apache.servicecomb.metrics.common.ProducerInvocationMetric;
import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegistryMonitor {

  private final SystemMonitor systemMonitor;

  private final Map<String, ConsumerInvocationMonitor> consumerInvocationMonitors;

  private final Map<String, ProducerInvocationMonitor> producerInvocationMonitors;

  @Autowired
  public RegistryMonitor(SystemMonitor systemMonitor) {
    this.systemMonitor = systemMonitor;
    this.consumerInvocationMonitors = new ConcurrentHashMapEx<>();
    this.producerInvocationMonitors = new ConcurrentHashMapEx<>();
  }

  public ConsumerInvocationMonitor getConsumerInvocationMonitor(String operationName) {
    return consumerInvocationMonitors.computeIfAbsent(operationName, i -> new ConsumerInvocationMonitor(operationName));
  }

  public ProducerInvocationMonitor getProducerInvocationMonitor(String operationName) {
    return producerInvocationMonitors.computeIfAbsent(operationName, i -> new ProducerInvocationMonitor(operationName));
  }

  public RegistryMetric toRegistryMetric(int windowTimeIndex) {
    Map<String, ConsumerInvocationMetric> consumerInvocationMetrics = new HashMap<>();
    for (ConsumerInvocationMonitor monitor : this.consumerInvocationMonitors.values()) {
      consumerInvocationMetrics.put(monitor.getOperationName(), monitor.toMetric(windowTimeIndex));
    }
    Map<String, ProducerInvocationMetric> producerInvocationMetrics = new HashMap<>();
    for (ProducerInvocationMonitor monitor : this.producerInvocationMonitors.values()) {
      producerInvocationMetrics.put(monitor.getOperationName(), monitor.toMetric(windowTimeIndex));
    }
    return new RegistryMetric(systemMonitor.toMetric(), consumerInvocationMetrics, producerInvocationMetrics);
  }
}
