package com.huaweicloud.governance.policy;

/**
 * 默认只有信号量,不提供线程池模式
 *
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
public class BulkheadPolicy extends AbstractPolicy {

  public static final int DEFAULT_MAX_CONCURRENT_CALLS = 1000;

  public static final int DEFAULT_MAX_WAIT_DURATION = 0;

  private Integer maxConcurrentCalls;

  private Integer maxWaitDuration;

  public Integer getMaxConcurrentCalls() {
    if (maxConcurrentCalls == null) {
      maxConcurrentCalls = DEFAULT_MAX_CONCURRENT_CALLS;
    }
    return maxConcurrentCalls;
  }

  public void setMaxConcurrentCalls(Integer maxConcurrentCalls) {
    this.maxConcurrentCalls = maxConcurrentCalls;
  }

  public Integer getMaxWaitDuration() {
    if (maxWaitDuration == null) {
      maxWaitDuration = DEFAULT_MAX_WAIT_DURATION;
    }
    return maxWaitDuration;
  }

  public void setMaxWaitDuration(Integer maxWaitDuration) {
    this.maxWaitDuration = maxWaitDuration;
  }

  @Override
  public String handler() {
    return "GovBulkhead";
  }

  @Override
  public String toString() {
    return "BulkheadPolicy{" +
        "maxConcurrentCalls=" + maxConcurrentCalls +
        ", maxWaitDuration=" + maxWaitDuration +
        '}';
  }
}
