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

  private AtomicLong totalRequests = new AtomicLong(0L);

  private AtomicLong successRequests = new AtomicLong(0L);

  private AtomicLong failedRequests = new AtomicLong(0L);

  public void markSuccess() {
    long time = System.currentTimeMillis();
    ensureWindow(time);
    lastVisitTime = time;
    totalRequests.incrementAndGet();
    successRequests.incrementAndGet();
    continuousFailureCount.set(0);
  }

  public void markFailure() {
    long time = System.currentTimeMillis();
    ensureWindow(time);
    lastVisitTime = time;
    totalRequests.incrementAndGet();
    failedRequests.incrementAndGet();
    continuousFailureCount.incrementAndGet();
  }

  private void ensureWindow(long time) {
    if (time - lastWindow > TIME_WINDOW_IN_MILLISECONDS) {
      synchronized (lock) {
        if (time - lastWindow > TIME_WINDOW_IN_MILLISECONDS) {
          continuousFailureCount.set(0);
          totalRequests.set(0);
          successRequests.set(0);
          failedRequests.set(0);
          lastWindow = time;
        }
      }
    }
  }

  public long getLastVisitTime() {
    return lastVisitTime;
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
    return (int)(successRequests.get() * 100 / totalRequests.get());
  }

  public int getFailedRate() {
    return (int)(failedRequests.get() * 100 / totalRequests.get());
  }
}
