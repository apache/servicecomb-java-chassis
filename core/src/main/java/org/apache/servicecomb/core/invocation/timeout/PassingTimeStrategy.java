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

package org.apache.servicecomb.core.invocation.timeout;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.invocation.InvocationTimeoutStrategy;

/**
 * <pre>
 * based on time synchronization
 * any time to calculate timeout: now - start time of invocation chain
 *
 * consumer: c
 * producer: p
 * ---------------------------------------------------------------
 * | process 2                                                   |
 * |                 c-send(T5)                       c-send(T8) |
 * |                    ↑                               ↑      |
 * | p-start(T3) → c-start(T4) → p-operation(T6) → c-start(T7)|
 * -----↑--------------------------------------------------------
 *      ↑
 * -----↑-----------------
 * |    ↑     process 1  |
 * |  c-send(T2)          |
 * |    ↑                |
 * |  c-start(T1)         |
 * ------------------------
 *
 * T2 timeout: T2 - T1
 * T3 timeout: T3 - T1
 * T4 timeout: T4 - T1
 * ......
 * </pre>
 */
public class PassingTimeStrategy implements InvocationTimeoutStrategy {
  public static final String NAME = "passing-time";

  // milliseconds
  // depend on time synchronization
  // transfer between processes
  public static final String CHAIN_START_TIME = "x-scb-chain-start";

  private Clock clock = Clock.systemDefaultZone();

  public PassingTimeStrategy setClock(Clock clock) {
    this.clock = clock;
    return this;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public void start(Invocation invocation) {
    if (invocation.getLocalContext(CHAIN_START_TIME) != null) {
      return;
    }

    long startTimeMillis = invocation.getInvocationStageTrace().getStartTimeMillis();
    String contextChainStartTime = invocation.getContext(CHAIN_START_TIME);
    if (StringUtils.isEmpty(contextChainStartTime)) {
      invocation.addContext(CHAIN_START_TIME, String.valueOf(startTimeMillis));
      invocation.addLocalContext(CHAIN_START_TIME, startTimeMillis);
      return;
    }

    long chainStartTime = NumberUtils.toLong(contextChainStartTime, startTimeMillis);
    invocation.addLocalContext(CHAIN_START_TIME, chainStartTime);
  }

  @Override
  public long calculateElapsedNanoTime(Invocation invocation) {
    long passingTimeMillis = clock.millis() - invocation.<Long>getLocalContext(CHAIN_START_TIME);
    return TimeUnit.MILLISECONDS.toNanos(passingTimeMillis);
  }
}
