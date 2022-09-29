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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedWindowStrategy extends AbstractQpsStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(FixedWindowStrategy.class);

  // Interval begin time
  private volatile long msCycleBegin;

  // Request count between Interval begin and now in one interval
  private final AtomicLong requestCount = new AtomicLong();

  // request count  before an interval
  private volatile long lastRequestCount = 1;

  private static final int CYCLE_LENGTH = 1000;

  private static final String STRATEGY_NAME = "FixedWindow";

  // return true means new request need to be rejected
  public boolean isLimitNewRequest() {
    if (this.getQpsLimit() == null) {
      throw new IllegalStateException("should not happen");
    }
    long newCount = requestCount.incrementAndGet();
    long msNow = System.currentTimeMillis();
    //Time jump cause the new request injected
    if (msNow - msCycleBegin > CYCLE_LENGTH || msNow < msCycleBegin) {
      //no need worry about concurrency problem
      lastRequestCount = newCount;
      msCycleBegin = msNow;
    }

    // Configuration update and use is at the situation of multi-threaded concurrency
    // It is possible that operation level updated to null,but schema level or microservice level does not updated
    boolean isLimitRequest = newCount - lastRequestCount >= this.getQpsLimit();
    if (isLimitRequest){
      LOGGER.warn("qps flowcontrol open, qpsLimit is {} and tps is {}", this.getQpsLimit(), newCount - lastRequestCount + 1);
    }
    return isLimitRequest;
  }

  @Override
  public String name() {
    return STRATEGY_NAME;
  }
}
