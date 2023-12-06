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
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.sun.management.OperatingSystemMXBean;

public class CpuMeter {
  private OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

  private final Id allCpuUsage;

  private final Id processCpuUsage;

  public CpuMeter(Id id) {
    allCpuUsage = id.withTag(OsMeter.OS_TYPE, OsMeter.OS_TYPE_ALL_CPU);
    processCpuUsage = id.withTag(OsMeter.OS_TYPE, OsMeter.OS_TYPE_PROCESS_CPU);
  }

  public void calcMeasurements(List<Measurement> measurements, long msNow) {
    measurements.add(new Measurement(allCpuUsage, msNow, osBean.getSystemCpuLoad()));
    measurements.add(new Measurement(processCpuUsage, msNow, osBean.getProcessCpuLoad()));
  }

  @VisibleForTesting
  public void setOsBean(OperatingSystemMXBean bean) {
    this.osBean = bean;
  }
}
