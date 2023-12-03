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
package org.apache.servicecomb.metrics.core.meter.vertx;

import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.metrics.meter.PeriodMeter;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultEndpointMetric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public class VertxEndpointsMeter implements PeriodMeter {
  private final Map<String, DefaultEndpointMetric> endpointMetricMap;

  protected final MeterRegistry meterRegistry;

  protected final String name;

  protected final Tags tags;

  private final Map<String, EndpointMeter> endpointMeterMap = new ConcurrentHashMapEx<>();

  @SuppressWarnings("unchecked")
  public <T extends DefaultEndpointMetric> VertxEndpointsMeter(MeterRegistry meterRegistry, String name, Tags tags,
      Map<String, T> endpointMetricMap) {
    this.meterRegistry = meterRegistry;
    this.name = name;
    this.tags = tags;
    this.endpointMetricMap = (Map<String, DefaultEndpointMetric>) endpointMetricMap;
  }

  private void syncMeters(long msNow, long secondInterval) {
    for (EndpointMeter meter : endpointMeterMap.values()) {
      if (!endpointMetricMap.containsKey(meter.getMetric().getAddress())) {
        EndpointMeter removed = endpointMeterMap.remove(meter.getMetric().getAddress());
        removed.destroy();
      }
    }
    for (DefaultEndpointMetric metric : endpointMetricMap.values()) {
      EndpointMeter updated = endpointMeterMap.computeIfAbsent(metric.getAddress(),
          address -> createEndpointMeter(metric));
      updated.poll(msNow, secondInterval);
    }
  }

  protected EndpointMeter createEndpointMeter(DefaultEndpointMetric metric) {
    return new EndpointMeter(meterRegistry, name, tags, metric);
  }

  @Override
  public void poll(long msNow, long secondInterval) {
    syncMeters(msNow, secondInterval);
  }
}
