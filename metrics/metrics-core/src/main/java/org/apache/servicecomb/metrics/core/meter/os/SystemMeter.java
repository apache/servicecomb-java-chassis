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

import java.lang.management.ManagementFactory;

import org.apache.servicecomb.foundation.metrics.meter.PeriodMeter;

import com.google.common.annotations.VisibleForTesting;
import com.sun.management.OperatingSystemMXBean;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public class SystemMeter implements PeriodMeter {
  public static final String SYSTEM_LOAD_AVERAGE = "sla";

  public static final String CPU_USAGE = "cpu";

  public static final String PROCESS_CPU_USAGE = "processCpu";

  public static final String MEMORY_USAGE = "memory";

  private OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

  public SystemMeter(MeterRegistry meterRegistry, String name) {
    Gauge.builder(name, () -> getOsBean().getSystemLoadAverage())
        .tags(Tags.of(OsMeter.OS_TYPE, SYSTEM_LOAD_AVERAGE))
        .register(meterRegistry);

    Gauge.builder(name, () -> getOsBean().getCpuLoad())
        .tags(Tags.of(OsMeter.OS_TYPE, CPU_USAGE))
        .register(meterRegistry);

    Gauge.builder(name, () -> getOsBean().getProcessCpuLoad())
        .tags(Tags.of(OsMeter.OS_TYPE, PROCESS_CPU_USAGE))
        .register(meterRegistry);

    Gauge.builder(name, () -> (getOsBean().getTotalMemorySize() - getOsBean().getFreeMemorySize())
            / (double) getOsBean().getTotalMemorySize())
        .tags(Tags.of(OsMeter.OS_TYPE, MEMORY_USAGE))
        .register(meterRegistry);
  }

  @VisibleForTesting
  public void setOsBean(OperatingSystemMXBean mock) {
    this.osBean = mock;
  }

  private OperatingSystemMXBean getOsBean() {
    return this.osBean;
  }

  @Override
  public void poll(long msNow, long secondInterval) {

  }
}
