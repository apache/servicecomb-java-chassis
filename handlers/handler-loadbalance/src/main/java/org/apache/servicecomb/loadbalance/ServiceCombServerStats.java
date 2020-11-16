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

package org.apache.servicecomb.loadbalance;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special stats that com.netflix.loadbalancer.ServerStats not provided.
 *
 * In concurrent scenarios, we can't count statistics accurately, but it's fine.
 */
public class ServiceCombServerStats {
  private static final long TIME_WINDOW_IN_MILLISECONDS = 60000;

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombServerStats.class);

  private final Object lock = new Object();

  /**
   * There is not more than 1 server allowed to stay in TRYING status concurrently.
   * This flag is designed to ensure such mechanism. And it makes the ServiceCombServerStats stateful.
   * Therefore, the flag should be reset correctly after the trying server gets handled.
   */
  static AtomicReference<TryingIsolatedServerMarker> globalAllowIsolatedServerTryingFlag = new AtomicReference<>();

  Clock clock;

  private long lastWindow;

  private AtomicLong continuousFailureCount;

  private long lastVisitTime;

  private long lastActiveTime;

  private long isolatedTime;

  private AtomicLong totalRequests;

  private AtomicLong successRequests;

  private AtomicLong failedRequests;

  private boolean isolated = false;

  private String microserviceName;

  public ServiceCombServerStats(String microserviceName) {
    this(microserviceName, TimeUtils.getSystemDefaultZoneClock());
  }

  public ServiceCombServerStats(String microserviceName, Clock clock) {
    this.clock = clock;
    this.microserviceName = microserviceName;
    init();
  }

  private void init() {
    lastWindow = clock.millis();
    continuousFailureCount = new AtomicLong(0);
    lastVisitTime = clock.millis();
    lastActiveTime = clock.millis();
    totalRequests = new AtomicLong(0L);
    successRequests = new AtomicLong(0L);
    failedRequests = new AtomicLong(0L);
  }

  public static boolean isolatedServerCanTry() {
    TryingIsolatedServerMarker marker = globalAllowIsolatedServerTryingFlag.get();
    if (marker == null) {
      return true;
    }
    return marker.isOutdated();
  }

  /**
   * Applying for a trying chance for the isolated server. There is only 1 trying chance globally concurrently.
   *
   * @return true if the chance is applied successfully, otherwise false
   */
  public static boolean applyForTryingChance(Invocation invocation) {
    TryingIsolatedServerMarker marker = globalAllowIsolatedServerTryingFlag.get();
    if (marker == null) {
      return globalAllowIsolatedServerTryingFlag.compareAndSet(null, new TryingIsolatedServerMarker(invocation));
    }
    if (marker.isOutdated()) {
      return globalAllowIsolatedServerTryingFlag.compareAndSet(marker, new TryingIsolatedServerMarker(invocation));
    }
    return false;
  }

  public static void checkAndReleaseTryingChance(Invocation invocation) {
    TryingIsolatedServerMarker marker = globalAllowIsolatedServerTryingFlag.get();
    if (marker == null || marker.getInvocation() != invocation) {
      return;
    }
    globalAllowIsolatedServerTryingFlag.compareAndSet(marker, null);
  }

  public void markIsolated(boolean isolated) {
    this.isolated = isolated;
    this.isolatedTime = System.currentTimeMillis();
  }

  public void markSuccess() {
    long time = clock.millis();
    ensureWindow(time);

    if (isolated) {
      if (Configuration.INSTANCE.isRecoverImmediatelyWhenSuccess(microserviceName)
          && time - this.isolatedTime > Configuration.INSTANCE
          .getMinIsolationTime(microserviceName)) {
        resetStats();
        LOGGER.info("trying server invocation success, and reset stats.");
      } else {
        LOGGER.info("trying server invocation success!");
      }
    }

    totalRequests.incrementAndGet();
    successRequests.incrementAndGet();
    continuousFailureCount.set(0);
    lastVisitTime = time;
    lastActiveTime = time;
  }

  public void markFailure() {
    long time = clock.millis();
    ensureWindow(time);
    lastVisitTime = time;

    // when isolated, do not update any failure statistics, or we can not recover from failure very quickly
    if (!isolated) {
      totalRequests.incrementAndGet();
      failedRequests.incrementAndGet();
      continuousFailureCount.incrementAndGet();
    }
  }

  private void ensureWindow(long time) {
    if (time - lastWindow > TIME_WINDOW_IN_MILLISECONDS) {
      synchronized (lock) {
        if (time - lastWindow > TIME_WINDOW_IN_MILLISECONDS) {
          if (!isolated) {
            resetStats();
          }
          lastWindow = time;
        }
      }
    }
  }

  private void resetStats() {
    continuousFailureCount.set(0);
    totalRequests.set(0);
    successRequests.set(0);
    failedRequests.set(0);
  }

  public long getLastVisitTime() {
    return lastVisitTime;
  }

  public long getIsolatedTime() {
    return isolatedTime;
  }

  public long getLastActiveTime() {
    return lastActiveTime;
  }

  public long getContinuousFailureCount() {
    return continuousFailureCount.get();
  }

  public long getTotalRequests() {
    return totalRequests.get();
  }

  public long getSuccessRequests() {
    return successRequests.get();
  }

  public long getFailedRequests() {
    return failedRequests.get();
  }

  public int getSuccessRate() {
    return calcRequestRate(successRequests);
  }

  public int getFailedRate() {
    return calcRequestRate(failedRequests);
  }

  private int calcRequestRate(AtomicLong requestCnt) {
    long totalReqs = totalRequests.get();
    if (totalReqs == 0L) {
      return 0;
    }
    return (int) (requestCnt.get() * 100 / totalReqs);
  }

  public boolean isIsolated() {
    return isolated;
  }
}
