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
package com.huaweicloud.governance.policy;

import org.springframework.util.StringUtils;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;

/**
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
public class CircuitBreakerPolicy extends AbstractPolicy {

  public static final int DEFAULT_FAILURE_RATE_THRESHOLD = 50;

  public static final int DEFAULT_SLOW_CALL_RATE_THRESHOLD = 100;

  public static final int DEFAULT_WAIT_DURATION_IN_OPEN_STATUS = 60000;

  //ms
  public static final int DEFAULT_SLOW_CALL_DURATION_THRESHOLD = 60000;

  // the number of permitted calls when the CircuitBreaker is half open.
  public static final int DEFAULT_PERMITTED = 10;

  public static final int DEFAULT_MINIMUM_NUMBER_CALLS = 100;

  public static final int DEFAULT_SLIDING_WINDOW_SIZE = 100;

  private Integer failureRateThreshold;

  private Integer slowCallRateThreshold;

  private Integer waitDurationInOpenState;

  private Integer slowCallDurationThreshold;

  private Integer permittedNumberOfCallsInHalfOpenState;

  private Integer minimumNumberOfCalls;

  private String slidingWindowType;

  private Integer slidingWindowSize;

  public CircuitBreakerPolicy() {
  }

  public Integer getFailureRateThreshold() {
    if (StringUtils.isEmpty(failureRateThreshold)) {
      failureRateThreshold = DEFAULT_FAILURE_RATE_THRESHOLD;
    }
    return failureRateThreshold;
  }

  public void setFailureRateThreshold(Integer failureRateThreshold) {
    this.failureRateThreshold = failureRateThreshold;
  }

  public Integer getSlowCallRateThreshold() {
    if (StringUtils.isEmpty(slowCallRateThreshold)) {
      slowCallRateThreshold = DEFAULT_SLOW_CALL_RATE_THRESHOLD;
    }
    return slowCallRateThreshold;
  }

  public void setSlowCallRateThreshold(Integer slowCallRateThreshold) {
    this.slowCallRateThreshold = slowCallRateThreshold;
  }

  public Integer getWaitDurationInOpenState() {
    if (StringUtils.isEmpty(waitDurationInOpenState)) {
      waitDurationInOpenState = DEFAULT_WAIT_DURATION_IN_OPEN_STATUS;
    }
    return waitDurationInOpenState;
  }

  public void setWaitDurationInOpenState(Integer waitDurationInOpenState) {
    this.waitDurationInOpenState = waitDurationInOpenState;
  }

  public Integer getSlowCallDurationThreshold() {
    if (StringUtils.isEmpty(slowCallDurationThreshold)) {
      slowCallDurationThreshold = DEFAULT_SLOW_CALL_DURATION_THRESHOLD;
    }
    return slowCallDurationThreshold;
  }

  public void setSlowCallDurationThreshold(Integer slowCallDurationThreshold) {
    this.slowCallDurationThreshold = slowCallDurationThreshold;
  }

  public Integer getPermittedNumberOfCallsInHalfOpenState() {
    if (StringUtils.isEmpty(permittedNumberOfCallsInHalfOpenState)) {
      permittedNumberOfCallsInHalfOpenState = DEFAULT_PERMITTED;
    }
    return permittedNumberOfCallsInHalfOpenState;
  }

  public void setPermittedNumberOfCallsInHalfOpenState(Integer permittedNumberOfCallsInHalfOpenState) {
    this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
  }

  public Integer getMinimumNumberOfCalls() {
    if (StringUtils.isEmpty(minimumNumberOfCalls)) {
      minimumNumberOfCalls = DEFAULT_MINIMUM_NUMBER_CALLS;
    }
    return minimumNumberOfCalls;
  }

  public void setMinimumNumberOfCalls(Integer minimumNumberOfCalls) {
    this.minimumNumberOfCalls = minimumNumberOfCalls;
  }

  public SlidingWindowType getSlidingWindowType() {
    if (StringUtils.isEmpty(slidingWindowType)) {
      slidingWindowType = "count";
    }
    switch (slidingWindowType) {
      case "time":
        return SlidingWindowType.TIME_BASED;
      case "count":
      default:
        return SlidingWindowType.COUNT_BASED;
    }
  }

  public void setSlidingWindowType(String slidingWindowType) {
    this.slidingWindowType = slidingWindowType;
  }

  // time's unit is second
  public Integer getSlidingWindowSize() {
    if (StringUtils.isEmpty(slidingWindowSize)) {
      slidingWindowSize = DEFAULT_SLIDING_WINDOW_SIZE;
    }
    return slidingWindowSize;
  }

  public void setSlidingWindowSize(Integer slidingWindowSize) {
    this.slidingWindowSize = slidingWindowSize;
  }

  @Override
  public String handler() {
    return "GovCircuitBreaker";
  }

  @Override
  public String toString() {
    return "CircuitBreakerPolicy{" +
        "failureRateThreshold=" + failureRateThreshold +
        ", slowCallRateThreshold=" + slowCallRateThreshold +
        ", waitDurationInOpenState=" + waitDurationInOpenState +
        ", slowCallDurationThreshold=" + slowCallDurationThreshold +
        ", permittedNumberOfCallsInHalfOpenState=" + permittedNumberOfCallsInHalfOpenState +
        ", minimumNumberOfCalls=" + minimumNumberOfCalls +
        ", slidingWindowType='" + slidingWindowType + '\'' +
        ", slidingWindowSize=" + slidingWindowSize +
        '}';
  }
}
