package com.huaweicloud.governance.policy;

import com.huaweicloud.governance.handler.BulkheadHandler;

public class BulkheadPolicy extends AbstractPolicy {

  public static final int DEFAULT_MAX_CONCURRENT_CALLS = 1000;

  public static final int DEFAULT_MAX_WAIT_DURATION = 0;

  private int maxConcurrentCalls = DEFAULT_MAX_CONCURRENT_CALLS;

  private int maxWaitDuration = DEFAULT_MAX_WAIT_DURATION;

  public int getMaxConcurrentCalls() {
    return maxConcurrentCalls;
  }

  public void setMaxConcurrentCalls(int maxConcurrentCalls) {
    this.maxConcurrentCalls = maxConcurrentCalls;
  }

  public int getMaxWaitDuration() {
    return maxWaitDuration;
  }

  public void setMaxWaitDuration(int maxWaitDuration) {
    this.maxWaitDuration = maxWaitDuration;
  }

  @Override
  public String handler() {
    return BulkheadHandler.class.getSimpleName();
  }

  @Override
  public String toString() {
    return "BulkheadPolicy{" +
        "maxConcurrentCalls=" + maxConcurrentCalls +
        ", maxWaitDuration=" + maxWaitDuration +
        '}';
  }
}
