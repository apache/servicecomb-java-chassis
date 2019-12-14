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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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

  /**
   * There is not more than 1 server allowed to stay in TRYING status concurrently.
   * If the value of globalAllowIsolatedServerTryingFlag is false, there have been 1 server in
   * TRYING status, so the other isolated servers cannot be transferred into
   * TRYING status; otherwise the value of globalAllowIsolatedServerTryingFlag is true.
   */
  private static AtomicBoolean globalAllowIsolatedServerTryingFlag = new AtomicBoolean(true);

  private final Object lock = new Object();

  Clock clock;

  private long lastWindow;

  private AtomicLong continuousFailureCount;

  private long lastVisitTime;

  private long lastActiveTime;

  private AtomicLong totalRequests;

  private AtomicLong successRequests;

  private AtomicLong failedRequests;

  private boolean isolated = false;

  public ServiceCombServerStats() {
    this.clock = TimeUtils.getSystemDefaultZoneClock();
    init();
  }

  public ServiceCombServerStats(Clock clock) {
    this.clock = clock;
    init();
  }

  private void init(){
    lastWindow = clock.millis();
    continuousFailureCount = new AtomicLong(0);
    lastVisitTime = clock.millis();
    lastActiveTime = clock.millis();
    totalRequests = new AtomicLong(0L);
    successRequests = new AtomicLong(0L);
    failedRequests = new AtomicLong(0L);
  }

  public static boolean isolatedServerCanTry() {
    return globalAllowIsolatedServerTryingFlag.get();
  }

  /**
   * Applying for a trying chance for the isolated server. There is only 1 trying chance globally concurrently.
   *
   * @return true if the chance is applied successfully, otherwise false
   */
  public static boolean applyForTryingChance() {
    return isolatedServerCanTry() && globalAllowIsolatedServerTryingFlag.compareAndSet(true, false);
  }

  public static void releaseTryingChance() {
    globalAllowIsolatedServerTryingFlag.set(true);
  }

  public void markIsolated(boolean isolated) {
    this.isolated = isolated;
  }

  public void markSuccess() {
    long time = clock.millis();
    ensureWindow(time);
    lastVisitTime = time;
    lastActiveTime = time;
    totalRequests.incrementAndGet();
    successRequests.incrementAndGet();
    continuousFailureCount.set(0);
    if (isolated) {
      LOGGER.info("trying server invocation success!");
    }
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
            continuousFailureCount.set(0);
            totalRequests.set(0);
            successRequests.set(0);
            failedRequests.set(0);
          }
          lastWindow = time;
        }
      }
    }
  }

  public long getLastVisitTime() {
    return lastVisitTime;
  }

  public long getLastActiveTime() {
    return lastActiveTime;
  }

  public long getCountinuousFailureCount() {
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
