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

import org.apache.servicecomb.foundation.metrics.meter.PeriodMeter;

import com.google.common.annotations.VisibleForTesting;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * name=os type=cpu value = 0
 * name=os type=processCpu value = 0
 * name=os type=net interface=eth0 statistic=send value=100
 * name=os type=net interface=eth0 statistic=receive value=100
 * name=os type=net interface=eth0 statistic=sendPackets value=100
 * name=os type=net interface=eth0 statistic=receivePackets value=100
 */
public class OsMeter implements PeriodMeter {
  public static final String OS_NAME = "os";

  public static final String OS_TYPE = "type";

  public static final String OS_TYPE_NET = "net";

  private final SystemMeter systemMeter;

  private final NetMeter netMeter;

  public OsMeter(MeterRegistry meterRegistry) {
    systemMeter = new SystemMeter(meterRegistry, OS_NAME);
    netMeter = new NetMeter(meterRegistry, OS_NAME, Tags.of(OS_TYPE, OS_TYPE_NET));
  }

  @Override
  public void poll(long msNow, long secondInterval) {
    systemMeter.poll(msNow, secondInterval);
    netMeter.poll(msNow, secondInterval);
  }

  @VisibleForTesting
  public SystemMeter getCpuMeter() {
    return systemMeter;
  }

  @VisibleForTesting
  public NetMeter getNetMeter() {
    return netMeter;
  }
}
