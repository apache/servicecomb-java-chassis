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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Statistic;
import com.netflix.spectator.impl.AtomicDouble;

/**
 * ServoTimer is too slow
 * this is a faster timer
 */
public class SimpleTimer extends AbstractPeriodMeter {
  private static final double CNV_SECONDS = 1.0 / TimeUnit.SECONDS.toNanos(1L);

  private final Id idCount;

  private final Id idTotalTime;

  private final Id idMax;

  private final LongAdder count = new LongAdder();

  private final LongAdder totalTime = new LongAdder();

  private final AtomicDouble max = new AtomicDouble();

  private long lastCount = 0;

  private long lastTotalTime = 0;

  public SimpleTimer(Id id) {
    this.id = id;
    this.idCount = id.withTag(Statistic.count);
    this.idTotalTime = id.withTag(Statistic.totalTime);
    this.idMax = id.withTag(Statistic.max);
  }

  public void record(long nanoAmount) {
    if (nanoAmount >= 0) {
      totalTime.add(nanoAmount);
      count.increment();
      max.max(nanoAmount);
    }
  }

  private Measurement newMeasurement(Id id, long msNow, Number n) {
    return new Measurement(id, msNow, n.doubleValue());
  }

  @Override
  public void calcMeasurements(long msNow, long secondInterval) {
    List<Measurement> measurements = new ArrayList<>(3);
    calcMeasurements(measurements, msNow, secondInterval);
    allMeasurements = measurements;
  }

  @Override
  public void calcMeasurements(List<Measurement> measurements, long msNow, long secondInterval) {
    long currentCount = count.longValue();
    long currentTotalTime = totalTime.longValue();

    measurements.add(newMeasurement(idCount, msNow, (double) (currentCount - lastCount) / secondInterval));
    measurements
        .add(newMeasurement(idTotalTime, msNow, (currentTotalTime - lastTotalTime) / secondInterval * CNV_SECONDS));
    measurements.add(newMeasurement(idMax, msNow, max.get() * CNV_SECONDS));

    lastCount = currentCount;
    lastTotalTime = currentTotalTime;
    // maybe lost some max value, but not so important?
    max.set(0);
  }
}
