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
package org.apache.servicecomb.metrics.core.meter.os;

import java.io.IOException;

import org.apache.servicecomb.foundation.metrics.meter.PeriodMeter;
import org.apache.servicecomb.metrics.core.meter.os.cpu.OsCpuUsage;
import org.apache.servicecomb.metrics.core.meter.os.cpu.ProcessCpuUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public class CpuMeter implements PeriodMeter {
  private static final Logger LOGGER = LoggerFactory.getLogger(CpuMeter.class);

  // read from /proc/stat
  private final OsCpuUsage allCpuUsage;

  // read from /proc/self/stat /proc/uptime
  private final ProcessCpuUsage processCpuUsage;

  public CpuMeter(MeterRegistry meterRegistry, String name) {
    allCpuUsage = new OsCpuUsage();
    processCpuUsage = new ProcessCpuUsage();

    Gauge.builder(name, allCpuUsage::getUsage).tags(Tags.of(OsMeter.OS_TYPE, OsMeter.OS_TYPE_ALL_CPU))
        .register(meterRegistry);

    Gauge.builder(name, processCpuUsage::getUsage).tags(Tags.of(OsMeter.OS_TYPE, OsMeter.OS_TYPE_PROCESS_CPU))
        .register(meterRegistry);
  }

  @Override
  public void poll(long msNow, long secondInterval) {
    try {
      allCpuUsage.update();
      processCpuUsage.update();
    } catch (IOException e) {
      LOGGER.error("Failed to update cpu usage", e);
    }
  }

  @VisibleForTesting
  public OsCpuUsage getAllCpuUsage() {
    return allCpuUsage;
  }

  @VisibleForTesting
  public ProcessCpuUsage getProcessCpuUsage() {
    return processCpuUsage;
  }
}
