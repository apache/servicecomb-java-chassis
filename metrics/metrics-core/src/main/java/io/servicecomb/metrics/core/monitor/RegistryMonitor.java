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

package io.servicecomb.metrics.core.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.servicecomb.metrics.core.metric.InvocationMetric;
import io.servicecomb.metrics.core.metric.RegistryMetric;

@Component
public class RegistryMonitor extends BasicMonitor {

  private final SystemMonitor systemMonitor;

  private final Map<String, InvocationMonitor> invocationMonitors;

  @Autowired
  public RegistryMonitor(SystemMonitor systemMonitor) {
    this.systemMonitor = systemMonitor;
    this.invocationMonitors = new ConcurrentHashMap<>();
  }

  public InvocationMonitor getInvocationMonitor(String operationName) {
    return invocationMonitors.computeIfAbsent(operationName, i -> new InvocationMonitor(operationName));
  }

  public RegistryMetric toRegistryMetric(int windowTimeIndex) {
    Map<String, InvocationMetric> invocationMetrics = new HashMap<>();
    for (InvocationMonitor monitor : invocationMonitors.values()) {
      invocationMetrics.put(monitor.getOperationName(), monitor.toInvocationMetric(windowTimeIndex));
    }
    return new RegistryMetric(systemMonitor.toSystemMetric(), invocationMetrics);
  }
}
