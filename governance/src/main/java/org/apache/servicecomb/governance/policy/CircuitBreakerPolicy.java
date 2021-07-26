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
package org.apache.servicecomb.governance.policy;

import org.apache.servicecomb.governance.utils.GovernanceUtils;
import org.springframework.util.StringUtils;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;

import java.time.Duration;

public class CircuitBreakerPolicy extends AbstractPolicy {

  public static final float DEFAULT_FAILURE_RATE_THRESHOLD = 50;

  public static final float DEFAULT_SLOW_CALL_RATE_THRESHOLD = 100;

  public static final Duration DEFAULT_WAIT_DURATION_IN_OPEN_STATUS = Duration.ofMillis(60000);

  //ms
  public static final Duration DEFAULT_SLOW_CALL_DURATION_THRESHOLD = Duration.ofMillis(60000);

  // the number of permitted calls when the CircuitBreaker is half open.
  public static final int DEFAULT_PERMITTED = 10;

  public static final int DEFAULT_MINIMUM_NUMBER_CALLS = 100;

  public static final String DEFAULT_SLIDING_WINDOW_SIZE = "100";

  private float failureRateThreshold = DEFAULT_FAILURE_RATE_THRESHOLD;

  private float slowCallRateThreshold = DEFAULT_SLOW_CALL_RATE_THRESHOLD;

  private String waitDurationInOpenState = DEFAULT_WAIT_DURATION_IN_OPEN_STATUS.toString();

  private String slowCallDurationThreshold = DEFAULT_SLOW_CALL_DURATION_THRESHOLD.toString();

  private int permittedNumberOfCallsInHalfOpenState = DEFAULT_PERMITTED;

  private int minimumNumberOfCalls = DEFAULT_MINIMUM_NUMBER_CALLS;

  private String slidingWindowType;

  private String slidingWindowSize = DEFAULT_SLIDING_WINDOW_SIZE;

  public CircuitBreakerPolicy() {
  }

  @Override
  public boolean isValid() {
    if (failureRateThreshold > 100.0F || failureRateThreshold <= 0.0F) {
      return false;
    }
    if (slowCallRateThreshold > 100.0F || slowCallRateThreshold <= 0.0F) {
      return false;
    }
    if (Duration.parse(waitDurationInOpenState).toMillis() <= 0) {
      return false;
    }
    if (Duration.parse(slowCallDurationThreshold).toMillis() <= 0) {
      return false;
    }
    if (permittedNumberOfCallsInHalfOpenState <= 0) {
      return false;
    }
    if (minimumNumberOfCalls <= 0) {
      return false;
    }

    return super.isValid();
  }

  public float getFailureRateThreshold() {
    return failureRateThreshold;
  }

  public void setFailureRateThreshold(float failureRateThreshold) {
    this.failureRateThreshold = failureRateThreshold;
  }

  public float getSlowCallRateThreshold() {
    return slowCallRateThreshold;
  }

  public void setSlowCallRateThreshold(float slowCallRateThreshold) {
    this.slowCallRateThreshold = slowCallRateThreshold;
  }

  public String getWaitDurationInOpenState() {
    return waitDurationInOpenState;
  }

  public void setWaitDurationInOpenState(String waitDurationInOpenState) {
    this.waitDurationInOpenState = stringOfDuration(waitDurationInOpenState, DEFAULT_WAIT_DURATION_IN_OPEN_STATUS);
  }

  public String getSlowCallDurationThreshold() {
    return slowCallDurationThreshold;
  }

  public void setSlowCallDurationThreshold(String slowCallDurationThreshold) {
    this.slowCallDurationThreshold = stringOfDuration(slowCallDurationThreshold, DEFAULT_SLOW_CALL_DURATION_THRESHOLD);
  }

  public int getPermittedNumberOfCallsInHalfOpenState() {
    return permittedNumberOfCallsInHalfOpenState;
  }

  public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
    this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
  }

  public int getMinimumNumberOfCalls() {
    return minimumNumberOfCalls;
  }

  public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
    this.minimumNumberOfCalls = minimumNumberOfCalls;
  }

  public SlidingWindowType getSlidingWindowTypeEnum() {
    if (StringUtils.isEmpty(slidingWindowType)) {
      return SlidingWindowType.TIME_BASED;
    }
    if (SlidingWindowType.COUNT_BASED.equals(slidingWindowType)) {
      return SlidingWindowType.COUNT_BASED;
    }
    return SlidingWindowType.TIME_BASED;
  }

  public String getSlidingWindowType() {
    return this.slidingWindowType;
  }

  public void setSlidingWindowType(String slidingWindowType) {
    this.slidingWindowType = slidingWindowType;
  }

  // time's unit is second
  public String getSlidingWindowSize() {
    return slidingWindowSize;
  }

  public void setSlidingWindowSize(String slidingWindowSize) {
    this.slidingWindowSize = getValue(slidingWindowSize);
  }

  private String getValue(String slidingWindowSize) {
    if (StringUtils.isEmpty(slidingWindowSize)) {
      return DEFAULT_SLIDING_WINDOW_SIZE;
    }
    if (slidingWindowSize.matches(GovernanceUtils.DIGIT_REGEX)) {
      if (Long.valueOf(slidingWindowSize) < 0) {
        throw new RuntimeException("The value should be more than 0.");
      }
      return slidingWindowSize;
    }
    Duration duration = Duration.parse(GovernanceUtils.DIGIT_PREFIX + slidingWindowSize);
    return String.valueOf(duration.getSeconds());
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
