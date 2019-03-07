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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

public class LatencyScopeMeter {
  private Id scopeId;

  private LongAdder times = new LongAdder();

  private long lastTimes = 0L;

  private long nanoMin;

  private long nanoMax;

  public LatencyScopeMeter(Id latencyDistributionId, LatencyScopeConfig config) {
    nanoMin = TimeUnit.MILLISECONDS.toNanos(config.getMsMin());
    nanoMax = TimeUnit.MILLISECONDS.toNanos(config.getMsMax());
    scopeId = latencyDistributionId.withTag("scope",
        String.format("[%d,%s)", config.getMsMin(), config.getMsMax() == Long.MAX_VALUE ? "" : config.getMsMax()));
  }

  public boolean update(long nanoLatency) {
    if (nanoMin <= nanoLatency && nanoMax > nanoLatency) {
      times.increment();
      return true;
    }

    return false;
  }

  public Measurement createMeasurement(long msNow) {
    long currentTimes = times.longValue();
    long deltaTimes = currentTimes - lastTimes;
    this.lastTimes = currentTimes;

    return new Measurement(scopeId, msNow, deltaTimes);
  }
}
