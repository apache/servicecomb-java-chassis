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

public class RateLimitingPolicy extends AbstractPolicy {

  public static final Duration DEFAULT_TIMEOUT_DURATION = Duration.ofMillis(0);

  public static final Duration DEFAULT_LIMIT_REFRESH_PERIOD = Duration.ofMillis(1000);

  public static final int DEFAULT_LIMIT_FOR_PERIOD = 1000;

  private Duration timeoutDuration=DEFAULT_TIMEOUT_DURATION;

  private Duration limitRefreshPeriod=DEFAULT_LIMIT_REFRESH_PERIOD;

  // 配置项名称使用 rate， 对应于 resilience4j 的 limitForPeriod
  private int rate = DEFAULT_LIMIT_FOR_PERIOD;

  public Duration getTimeoutDuration() {
    return timeoutDuration;
  }

  public void setTimeoutDuration(String timeoutDuration) {
    this.timeoutDuration = parseToDuration(timeoutDuration,DEFAULT_TIMEOUT_DURATION);
  }

  public Duration getLimitRefreshPeriod() {
    return limitRefreshPeriod;
  }

  public void setLimitRefreshPeriod(String limitRefreshPeriod) {
    this.limitRefreshPeriod = parseToDuration(limitRefreshPeriod,DEFAULT_LIMIT_REFRESH_PERIOD);
  }

  public int getRate() {
    return rate;
  }

  public void setRate(int rate) {
    this.rate = rate;
  }

  public RateLimitingPolicy() {
  }

  @Override
  public boolean isValid() {
    if (timeoutDuration.toMillis() < 0) {
      return false;
    }
    if (limitRefreshPeriod.toMillis() <= 0) {
      return false;
    }
    if (rate <= 0) {
      return false;
    }
    return super.isValid();
  }

  @Override
  public String toString() {
    return "RateLimitingPolicy{" +
        "timeoutDuration=" + timeoutDuration +
        ", limitRefreshPeriod=" + limitRefreshPeriod +
        ", rate=" + rate + " req/s" +
        '}';
  }
}
