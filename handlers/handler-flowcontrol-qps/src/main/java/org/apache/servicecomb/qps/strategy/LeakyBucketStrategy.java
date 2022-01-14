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

package org.apache.servicecomb.qps.strategy;

import java.util.concurrent.atomic.AtomicLong;

/**
 * leaky bucket algorithm include 2 implementation :
 * 1. as a meter : it's same as the token bucket.
 * 2. as a queue : the bucket size equal to qpsLimit.
 *
 **/
public class LeakyBucketStrategy extends AbstractQpsStrategy {

  // Request count between Interval begin and now in one interval
  private volatile AtomicLong requestCount = new AtomicLong();

  private volatile long lastTime;

  private long remainder = 0;

  private static final String STRATEGY_NAME = "LeakyBucket";

  public AtomicLong getRequestCount() {
    return requestCount;
  }

  @Override
  public boolean isLimitNewRequest() {
    if (this.getQpsLimit() == null) {
      throw new IllegalStateException("should not happen");
    }
    if (this.getBucketLimit() == null) {
      this.setBucketLimit(Math.max(2 * this.getQpsLimit(), Integer.MAX_VALUE));
    }
    long nowTime = System.currentTimeMillis();
    //get the num of te period time
    long leakCount = ((nowTime - lastTime + remainder) / 1000) * this.getQpsLimit();
    remainder = (nowTime - lastTime + remainder) % 1000;
    // leak the request
    if (requestCount.longValue() > leakCount) {
      requestCount.addAndGet(-leakCount);
    } else {
      requestCount.set(0);
    }
    lastTime = nowTime;
    //compute this time
    if (requestCount.longValue() < this.getBucketLimit()) {
      requestCount.incrementAndGet();
      return false;
    }
    return true;
  }

  @Override
  public String name() {
    return STRATEGY_NAME;
  }
}
