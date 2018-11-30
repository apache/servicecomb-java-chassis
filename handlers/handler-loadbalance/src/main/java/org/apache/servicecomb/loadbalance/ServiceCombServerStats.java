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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Special stats that com.netflix.loadbalancer.ServerStats not provided.
 *
 * In concurrent scenarios, we can't count statistics accurately, but it's fine.
 */
public class ServiceCombServerStats {
  private static final long TIME_WINDOW_IN_MILLISECONDS = 60000;

  private long lastWindow = System.currentTimeMillis();

  private Object lock = new Object();

  private AtomicLong continuousFailureCount = new AtomicLong(0);

  private long lastVisitTime = System.currentTimeMillis();

  private long lastActiveTime = System.currentTimeMillis();

  private AtomicLong totalRequests = new AtomicLong(0L);

  private AtomicLong successRequests = new AtomicLong(0L);

  private AtomicLong failedRequests = new AtomicLong(0L);

  private boolean isolated = false;

  public void markIsolated(boolean isolated) {
    this.isolated = isolated;
  }

  public void markSuccess() {
    long time = System.currentTimeMillis();
    ensureWindow(time);
    lastVisitTime = time;
    lastActiveTime = time;
    totalRequests.incrementAndGet();
    successRequests.incrementAndGet();
    continuousFailureCount.set(0);
  }

  public void markFailure() {
    long time = System.currentTimeMillis();
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
    if (totalRequests.get() == 0L) {
      return 0;
    }
    return (int) (successRequests.get() * 100 / totalRequests.get());
  }

  public int getFailedRate() {
    if (totalRequests.get() == 0L) {
      return 0;
    }
    return (int) (failedRequests.get() * 100 / totalRequests.get());
  }

  public boolean isIsolated() {
    return isolated;
  }
}
