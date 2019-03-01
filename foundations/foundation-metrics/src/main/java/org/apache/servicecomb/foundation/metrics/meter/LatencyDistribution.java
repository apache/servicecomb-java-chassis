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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

public class LatencyDistribution extends AbstractPeriodMeter {
  private static final Logger LOGGER = LoggerFactory.getLogger(LatencyDistribution.class);

  /**
   * the buckets has these limits as bellow, the unit of number is MilliSeconds:
   *   1. greater than 0, smaller than Integer.MAX_VALUE
   *   2. ascending order. eg: 1,2,4
   *   3. all buckets must be distinct.
   *   4. all buckets must be integer.
   *   5. use commas as separators.
   * for example:
   *   valid:
   *        1,2,10
   *   invalid:
   *        0,1,2,10
   *        1,3,2,10
   *        1,1,3,10
   *        1:2:10
   *        1,1.2,10.1
   */
  public static final String METRICS_LATENCY = DynamicPropertyFactory.getInstance()
      .getStringProperty("servicecomb.metrics.invocation.latency.buckets", "")
      .get();

  private List<LatencyScope> latencyScopes;

  public LatencyDistribution(Id id) {
    this.id = id;
    this.latencyScopes = parseLatencyScopeListFromProperty(METRICS_LATENCY, id);
  }

  public void record(long nanoAmount) {
    if (nanoAmount > 0) {
      for (LatencyScope latencyScope : latencyScopes) {
        if (latencyScope.match(nanoAmount)) {
          latencyScope.increment();
          return;
        }
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
    latencyScopes.forEach(latencyScope -> {
      measurements.add(newMeasurement(latencyScope.getId(),
          msNow, latencyScope.getAndUpdateLastTimes()));
    });
  }

  private Measurement newMeasurement(Id id, long msNow, Number n) {
    return new Measurement(id, msNow, n.doubleValue());
  }

  public static List<LatencyScope> parseLatencyScopeListFromProperty(String property, Id rootId) {
    List<LatencyScope> results = new ArrayList<>();
    try {
      String[] arrays = property.trim().split(",");
      if (arrays.length == 0) {
        return results;
      }
      results.add(new LatencyScope(0L, TimeUnit.MILLISECONDS.toNanos(Integer.parseInt(arrays[0])),
          createLatencyScopeId("0", arrays[0], rootId)));
      for (int i = 1; i < arrays.length; i++) {
        results.add(new LatencyScope(TimeUnit.MILLISECONDS.toNanos(Integer.parseInt(arrays[i - 1])),
            TimeUnit.MILLISECONDS.toNanos(Integer.parseInt(arrays[i])),
            createLatencyScopeId(arrays[i - 1], arrays[i], rootId)));
      }
      results.add(new LatencyScope(
          TimeUnit.MILLISECONDS.toNanos(Integer.parseInt(arrays[arrays.length - 1])), Long.MAX_VALUE,
          createLatencyScopeId(arrays[arrays.length - 1], " ", rootId)));
    } catch (Throwable e) {
      LOGGER.error("Failed to parse property servicecomb.metrics.invocation.latency.buckets", e);
    }
    return results;
  }

  /**
   * create LatencyScopeId with format [leftStr,rightStr)
   * @param leftStr leftValue
   * @param rightStr rightValue
   * @param rootId rootId
   * @return id
   */
  private static Id createLatencyScopeId(String leftStr, String rightStr, Id rootId) {
    return rootId.withTag("statistic", LatencyScope.getStandardLatencyScopeStr(leftStr, rightStr, 0));
  }

  public static class LatencyScope {
    private Id id;

    private LongAdder times = new LongAdder();

    private long lastUpdated = 0L;

    private long minNanoSeconds;

    private long maxNanoSeconds;

    public LatencyScope(long minNanoSeconds, long maxNanoSeconds, Id id) throws Exception {
      if (minNanoSeconds >= maxNanoSeconds) {
        throw new Exception("maxNanoSeconds must greater than minNanoSeconds");
      }
      this.minNanoSeconds = minNanoSeconds;
      this.maxNanoSeconds = maxNanoSeconds;
      this.id = id;
    }

    public boolean match(long num) {
      return minNanoSeconds <= num && maxNanoSeconds > num;
    }

    public void increment() {
      this.times.increment();
    }

    public long getAndUpdateLastTimes() {
      long currentTimes = times.longValue();
      long result = currentTimes - lastUpdated;
      this.lastUpdated = currentTimes;
      return result;
    }

    public Id getId() {
      return id;
    }

    public static String getStandardLatencyScopeStr(String leftStr, String rightStr, int minLength) {
      StringBuilder sb = new StringBuilder();
      sb.append("[").append(leftStr)
          .append(",")
          .append(rightStr)
          .append(")");
      if (sb.length() < minLength) {
        for (int i = 0; i < minLength - sb.length(); i++) {
          sb.append(' ');
        }
      }
      return sb.toString();
    }
  }
}
