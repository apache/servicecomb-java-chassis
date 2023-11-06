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

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.invocation.InvocationTimeoutStrategy;

import com.google.common.base.Ticker;

/**
 * <pre>
 * Cumulative Processing Time
 * not depend on time synchronization
 * but lost network and framework outside of servicecomb processing time
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
 * T3 timeout: (T2 - T1) + (T3 - T3)
 * T4 timeout: (T2 - T1) + (T4 - T3)
 * T5 timeout: (T2 - T1) + (T5 - T3)
 * T6 timeout: (T2 - T1) + (T6 - T3)
 * T7 timeout: (T2 - T1) + (T7 - T3)
 * T8 timeout: (T2 - T1) + (T8 - T3)
 * ......
 * </pre>
 */
public class ProcessingTimeStrategy implements InvocationTimeoutStrategy {
  public static final String NAME = "processing-time";

  // nanoseconds
  // used inside one process
  // not depend on time synchronization
  public static final String CHAIN_START_TIME = "x-scb-process-chain-start";

  // nanoseconds
  // processing time of all previous process
  // transfer between processes
  public static final String CHAIN_PROCESSING = "x-scb-processing-time";

  private Ticker ticker = Ticker.systemTicker();

  public ProcessingTimeStrategy setTicker(Ticker ticker) {
    this.ticker = ticker;
    return this;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public void start(Invocation invocation) {
    initProcessChainStart(invocation);
    initChainProcessing(invocation);
  }

  private void initProcessChainStart(Invocation invocation) {
    if (invocation.getLocalContext(CHAIN_START_TIME) != null) {
      return;
    }

    invocation.addLocalContext(CHAIN_START_TIME, invocation.getInvocationStageTrace().getStart());
  }

  private void initChainProcessing(Invocation invocation) {
    if (invocation.getLocalContext(CHAIN_PROCESSING) != null) {
      return;
    }

    String contextChainProcessing = invocation.getContext(CHAIN_PROCESSING);
    long chainProcessingTime = NumberUtils.toLong(contextChainProcessing, 0L);
    invocation.addLocalContext(CHAIN_PROCESSING, chainProcessingTime);
  }

  @Override
  public void beforeSendRequest(Invocation invocation) {
    InvocationTimeoutStrategy.super.beforeSendRequest(invocation);

    long processingTime = calculateElapsedNanoTime(invocation);
    invocation.addContext(CHAIN_PROCESSING, Long.toString(processingTime));
  }

  @Override
  public long calculateElapsedNanoTime(Invocation invocation) {
    long chainStartTime = invocation.getLocalContext(CHAIN_START_TIME);
    long previousProcessingTime = invocation.getLocalContext(CHAIN_PROCESSING);
    return ticker.read() - chainStartTime + previousProcessingTime;
  }
}
