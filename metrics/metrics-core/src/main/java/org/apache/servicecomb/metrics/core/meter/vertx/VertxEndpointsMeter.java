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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.metrics.meter.AbstractPeriodMeter;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultEndpointMetric;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

public class VertxEndpointsMeter extends AbstractPeriodMeter {
  private Map<String, DefaultEndpointMetric> endpointMetricMap;

  private Map<String, EndpointMeter> endpointMeterMap = new ConcurrentHashMapEx<>();

  @SuppressWarnings("unchecked")
  public <T extends DefaultEndpointMetric> VertxEndpointsMeter(Id id, Map<String, T> endpointMetricMap) {
    this.id = id;
    this.endpointMetricMap = (Map<String, DefaultEndpointMetric>) endpointMetricMap;
  }

  @Override
  public void calcMeasurements(long msNow, long secondInterval) {
    List<Measurement> measurements = new ArrayList<>();
    calcMeasurements(measurements, msNow, secondInterval);
    allMeasurements = measurements;
  }

  @Override
  public void calcMeasurements(List<Measurement> measurements, long msNow, long secondInterval) {
    syncMeters();

    for (EndpointMeter meter : endpointMeterMap.values()) {
      meter.calcMeasurements(measurements, msNow, secondInterval);
    }
  }

  private void syncMeters() {
    for (EndpointMeter meter : endpointMeterMap.values()) {
      if (!endpointMetricMap.containsKey(meter.getMetric().getAddress())) {
        endpointMeterMap.remove(meter.getMetric().getAddress());
      }
    }
    for (DefaultEndpointMetric metric : endpointMetricMap.values()) {
      endpointMeterMap.computeIfAbsent(metric.getAddress(), addr -> createEndpointMeter(id, metric));
    }
  }

  protected EndpointMeter createEndpointMeter(Id id, DefaultEndpointMetric metric) {
    return new EndpointMeter(id, metric);
  }

  @Override
  public Iterable<Measurement> measure() {
    return allMeasurements;
  }

  @Override
  public boolean hasExpired() {
    return false;
  }
}
