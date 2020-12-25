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

import org.apache.servicecomb.governance.handler.RateLimitingHandler;

/**
 * resilience4j 采用类似令牌桶的思想，其原理:
 * 每隔limitRefreshPeriod的时间会加入limitForPeriod个新许可
 * 如果获取不到新的许可(已经触发限流)，当前线程会park，最多等待timeoutDuration的时间
 * 采用默认单位为ms
 *
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
public class RateLimitingPolicy extends AbstractPolicy {

  public static final int DEFAULT_TIMEOUT_DURATION = 0;

  public static final int DEFAULT_LIMIT_REFRESH_PERIOD = 1000;

  public static final int DEFAULT_LIMIT_FOR_PERIOD = 1000;

  private int timeoutDuration = DEFAULT_TIMEOUT_DURATION;

  private int limitRefreshPeriod = DEFAULT_LIMIT_REFRESH_PERIOD;

  // 配置项名称使用 rate， 对应于 resilience4j 的 limitForPeriod
  private int rate = DEFAULT_LIMIT_FOR_PERIOD;

  public int getTimeoutDuration() {
    return timeoutDuration;
  }

  public void setTimeoutDuration(int timeoutDuration) {
    this.timeoutDuration = timeoutDuration;
  }

  public int getLimitRefreshPeriod() {
    return limitRefreshPeriod;
  }

  public void setLimitRefreshPeriod(int limitRefreshPeriod) {
    this.limitRefreshPeriod = limitRefreshPeriod;
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
  public String handler() {
    return RateLimitingHandler.class.getSimpleName();
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
