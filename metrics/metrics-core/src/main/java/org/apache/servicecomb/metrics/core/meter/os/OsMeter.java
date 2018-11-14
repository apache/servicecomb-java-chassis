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

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.foundation.metrics.PollEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Registry;

/**
 * name=os type=cpu value=10.0
 * name=os type=net interface=eth0 statistic=send value=100
 * name=os type=net interface=eth0 statistic=receive value=100
 */
public class OsMeter implements Meter {
  public static final String OS_NAME = "os";

  public static final String OS_TYPE = "type";

  public static final String OS_TYPE_CPU = "cpu";

  public static final String OS_TYPE_NET = "net";

  private List<Measurement> measurements = new ArrayList<>();

  private Id id;

  private Registry registry;

  private CpuMeter cpuMeter;

  private NetMeter netMeter;

  public OsMeter(Registry registry, EventBus eventBus) {
    this.registry = registry;
    this.id = registry.createId(OS_NAME);

    cpuMeter = new CpuMeter(id.withTag(OS_TYPE, OS_TYPE_CPU));
    netMeter = new NetMeter(id.withTag(OS_TYPE, OS_TYPE_NET));

    eventBus.register(this);
  }

  @Subscribe
  public void calcMeasurements(PollEvent pollEvent) {
    final long now = registry.clock().wallTime();

    final List<Measurement> tmpCpuMeasurements = new ArrayList<>();
    cpuMeter.calcMeasurements(tmpCpuMeasurements, now);
    netMeter.calcMeasurements(tmpCpuMeasurements, now, pollEvent);

    measurements = tmpCpuMeasurements;
  }

  @Override
  public Id id() {
    return id;
  }

  @Override
  public Iterable<Measurement> measure() {
    return measurements;
  }

  @Override
  public boolean hasExpired() {
    return false;
  }

  public CpuMeter getCpuMeter() {
    return cpuMeter;
  }

  public NetMeter getNetMeter() {
    return netMeter;
  }
}
