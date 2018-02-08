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

package org.apache.servicecomb.metrics.core.monitor;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.metrics.common.MetricsConst;

public class ConsumerInvocationMonitor {
  private final TimerMonitor consumerLatency;

  private final CallMonitor consumerCall;

  public TimerMonitor getConsumerLatency() {
    return consumerLatency;
  }

  public CallMonitor getConsumerCall() {
    return consumerCall;
  }

  public ConsumerInvocationMonitor(String operation) {
    this.consumerLatency = new TimerMonitor(operation, MetricsConst.STAGE_WHOLE, MetricsConst.ROLE_CONSUMER);
    this.consumerCall = new CallMonitor(operation, MetricsConst.STAGE_WHOLE, MetricsConst.ROLE_CONSUMER);
  }

  public Map<String, Double> measure(int windowTimeIndex, boolean calculateLatency) {
    Map<String, Double> measurements = new HashMap<>();
    measurements.putAll(consumerCall.measure(windowTimeIndex));
    measurements.putAll(consumerLatency.measure(windowTimeIndex, calculateLatency));
    return measurements;
  }
}
