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
package org.apache.servicecomb.foundation.metrics.meter;

import java.util.ArrayList;
import java.util.List;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

public class LatencyDistributionMeter extends AbstractPeriodMeter {
  private List<LatencyScopeMeter> latencyScopeMeters = new ArrayList<>();

  public LatencyDistributionMeter(Id id, String config) {
    this.id = id;

    LatencyDistributionConfig latencyDistributionConfig = new LatencyDistributionConfig(config);
    for (LatencyScopeConfig scopeConfig : latencyDistributionConfig.getScopeConfigs()) {
      latencyScopeMeters.add(new LatencyScopeMeter(id, scopeConfig));
    }
  }

  public void record(long nanoLatency) {
    if (nanoLatency < 0) {
      return;
    }

    for (LatencyScopeMeter latencyScopeMeter : latencyScopeMeters) {
      if (latencyScopeMeter.update(nanoLatency)) {
        return;
      }
    }
  }

  @Override
  public void calcMeasurements(long msNow, long secondInterval) {
    List<Measurement> measurements = new ArrayList<>();
    calcMeasurements(measurements, msNow, secondInterval);
    allMeasurements = measurements;
  }

  @Override
  public void calcMeasurements(List<Measurement> measurements, long msNow, long secondInterval) {
    latencyScopeMeters.forEach(latencyScopeMeter -> measurements.add(latencyScopeMeter.createMeasurement(msNow)));
  }
}
