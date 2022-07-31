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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

public class RetryPolicy extends AbstractPolicy {

  public static final int DEFAULT_MAX_ATTEMPTS = 3;

  public static final Duration DEFAULT_WAIT_DURATION = Duration.ofMillis(1);

  public static final String DEFAULT_RETRY_ON_RESPONSE_STATUS_502 = "502";

  public static final String DEFAULT_RETRY_ON_RESPONSE_STATUS_503 = "503";

  public static final List<String> DEFAULT_STATUS_LIST = Arrays.asList(DEFAULT_RETRY_ON_RESPONSE_STATUS_502,
      DEFAULT_RETRY_ON_RESPONSE_STATUS_503);

  private static final Duration INITIAL_INTERVAL = Duration.ofMillis(1000);

  private static final float MULTIPLIER = 2;

  private static final double RANDOMIZATION_FACTOR = 0.5;

  private static final String DEFAULT_RETRY_STRATEGY = "FixedInterval";

  //max retry attempts
  private int maxAttempts = DEFAULT_MAX_ATTEMPTS;

  //wait duration for each retry
  private String waitDuration = DEFAULT_WAIT_DURATION.toString();

  //status code that need retry
  private List<String> retryOnResponseStatus = DEFAULT_STATUS_LIST;

  //retry strategy
  private String retryStrategy = DEFAULT_RETRY_STRATEGY;

  // initial interval for backoff retry
  private String initialInterval = INITIAL_INTERVAL.toString();

  // multiplier for backoff retry
  private float multiplier = MULTIPLIER;

  // randomization factor for backoff retry
  private double randomizationFactor = RANDOMIZATION_FACTOR;

  // if throw an MaxRetriesExceededException if retry condition is based on result
  private boolean failAfterMaxAttempts = false;

  // if retry on the same instance. This property is not directly used in
  // RetryHandler, but used for loadbalancers
  private int retryOnSame = 0;

  public List<String> getRetryOnResponseStatus() {
    if (CollectionUtils.isEmpty(retryOnResponseStatus)) {
      return DEFAULT_STATUS_LIST;
    }
    return retryOnResponseStatus;
  }

  public void setRetryOnResponseStatus(List<String> retryOnResponseStatus) {
    if (retryOnResponseStatus == null) {
      return;
    }
    this.retryOnResponseStatus = retryOnResponseStatus.stream().filter(e -> !StringUtils.isEmpty(e))
        .collect(Collectors.toList());
  }

  public int getMaxAttempts() {
    return maxAttempts;
  }

  public void setMaxAttempts(int maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  public String getWaitDuration() {
    return Duration.parse(waitDuration).toMillis() < 1 ? DEFAULT_WAIT_DURATION.toString() : waitDuration;
  }

  public void setWaitDuration(String waitDuration) {
    this.waitDuration = stringOfDuration(waitDuration, DEFAULT_WAIT_DURATION);
  }

  public String getRetryStrategy() {
    if (StringUtils.isEmpty(retryStrategy)) {
      retryStrategy = DEFAULT_RETRY_STRATEGY;
    }
    return retryStrategy;
  }

  public void setRetryStrategy(String retryStrategy) {
    this.retryStrategy = retryStrategy;
  }

  public String getInitialInterval() {
    return initialInterval;
  }

  public void setInitialInterval(String initialInterval) {
    this.initialInterval = stringOfDuration(initialInterval, INITIAL_INTERVAL);
  }

  public float getMultiplier() {
    return multiplier;
  }

  public void setMultiplier(float multiplier) {
    this.multiplier = multiplier;
  }

  public double getRandomizationFactor() {
    return randomizationFactor;
  }

  public void setRandomizationFactor(double randomizationFactor) {
    this.randomizationFactor = randomizationFactor;
  }

  public boolean isFailAfterMaxAttempts() {
    return failAfterMaxAttempts;
  }

  public void setFailAfterMaxAttempts(boolean failAfterMaxAttempts) {
    this.failAfterMaxAttempts = failAfterMaxAttempts;
  }

  public int getRetryOnSame() {
    return retryOnSame;
  }

  public void setRetryOnSame(int retryOnSame) {
    this.retryOnSame = retryOnSame;
  }

  @Override
  public boolean isValid() {
    if (maxAttempts < 1) {
      return false;
    }
    if (Duration.parse(waitDuration).toMillis() < 0) {
      return false;
    }
    if (Duration.parse(initialInterval).toMillis() < 10) {
      return false;
    }
    return super.isValid();
  }

  @Override
  public String toString() {
    return "RetryPolicy{" +
        "maxAttempts=" + maxAttempts +
        ", waitDuration=" + waitDuration +
        ", retryOnResponseStatus='" + retryOnResponseStatus + '\'' +
        '}';
  }
}
