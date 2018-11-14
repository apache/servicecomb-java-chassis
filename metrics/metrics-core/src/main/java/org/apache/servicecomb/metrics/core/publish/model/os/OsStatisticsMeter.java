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
package org.apache.servicecomb.metrics.core.publish.model.os;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.apache.servicecomb.foundation.metrics.PollEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Registry;

public class OsStatisticsMeter implements Meter {
  public static final String OS_STATISTICS_NAME = "os";

  // cpu net
  public static final String OS_STATISTICS_TYPE = "type";

  public static final String OS_STATISTICS_INTERFACE = "interface";

  public static final String OS_STATISTIC_DETAIL = "statistic";

  private Map<String, OsNetMeter> osNetMeterMap = new HashMap<>();

  private List<Measurement> allMeasurements = new ArrayList<>();

  private Id id;

  private Registry registry;

  private OsCpuMeter osCpuMeter;

  public OsStatisticsMeter(Registry registry, EventBus eventBus) {
    this.registry = registry;
    this.id = registry.createId(OS_STATISTICS_NAME);
    osCpuMeter = new OsCpuMeter(id);
    eventBus.register(this);
  }

  @Subscribe
  public void syncOsData(PollEvent pollEvent) {
    if (!SystemUtils.IS_OS_LINUX) {
      return;
    }
    //refresh cpu
    final List<Measurement> tmpCpuMeasurements = new ArrayList<>();
    final long now = registry.clock().wallTime();
    OsCpuMeter.refreshCpu(osCpuMeter);
    osCpuMeter.calcMeasurements(tmpCpuMeasurements, now);
    //reset allMeasurements
    allMeasurements = tmpCpuMeasurements;
    //refresh net
    final List<Measurement> tmpNetMeasurements = new ArrayList<>();
    OsNetMeter.refreshNet(id, osNetMeterMap, pollEvent.getMsPollInterval());
    osNetMeterMap.values().forEach(osNetMeter -> osNetMeter.calcMeasurements(tmpNetMeasurements, now));
    allMeasurements.addAll(tmpNetMeasurements);
  }

  @Override
  public Id id() {
    return id;
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
