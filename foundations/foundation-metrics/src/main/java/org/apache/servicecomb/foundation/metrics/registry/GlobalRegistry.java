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
package org.apache.servicecomb.foundation.metrics.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.servicecomb.foundation.metrics.PolledEvent;
import org.apache.servicecomb.foundation.metrics.meter.PeriodMeter;

import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.SpectatorUtils;

public class GlobalRegistry {
  private Clock clock;

  private List<Registry> registries = new CopyOnWriteArrayList<>();

  private Registry defaultRegistry;

  public GlobalRegistry() {
    this(Clock.SYSTEM);
  }

  public GlobalRegistry(Clock clock) {
    this.clock = clock;
  }

  public Clock getClock() {
    return clock;
  }

  public List<Registry> getRegistries() {
    return registries;
  }

  public Registry getDefaultRegistry() {
    return defaultRegistry;
  }

  public synchronized void add(Registry registry) {
    if (registries.isEmpty()) {
      defaultRegistry = registry;
    }
    registries.add(registry);
  }

  public synchronized void remove(Registry registry) {
    registries.remove(registry);
    if (registry != defaultRegistry) {
      return;
    }

    if (registries.isEmpty()) {
      defaultRegistry = null;
      return;
    }

    defaultRegistry = registries.get(0);
  }

  public synchronized void removeAll() {
    registries.clear();
    defaultRegistry = null;
  }

  @SuppressWarnings("unchecked")
  public <T extends Registry> T find(Class<T> cls) {
    for (Registry registry : registries) {
      if (cls.isAssignableFrom(registry.getClass())) {
        return (T) registry;
      }
    }
    return null;
  }

  public PolledEvent poll(long secondInterval) {
    long msNow = clock.wallTime();
    List<Meter> meters = new ArrayList<>();
    List<Measurement> measurements = new ArrayList<>();
    for (Registry registry : registries) {
      SpectatorUtils.removeExpiredMeters(registry);

      for (Meter meter : registry) {
        if (meter instanceof PeriodMeter) {
          ((PeriodMeter) meter).calcMeasurements(msNow, secondInterval);
        }

        meters.add(meter);
        meter.measure().forEach(measurements::add);
      }
    }

    return new PolledEvent(meters, measurements);
  }
}
