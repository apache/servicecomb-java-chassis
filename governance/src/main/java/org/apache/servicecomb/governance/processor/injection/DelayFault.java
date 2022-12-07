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

package org.apache.servicecomb.governance.processor.injection;

import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayFault extends AbstractFault {
  private static final Logger LOGGER = LoggerFactory.getLogger(DelayFault.class);

  public DelayFault(String key, FaultInjectionPolicy policy) {
    super(key, policy);
  }

  @Override
  public int getOrder() {
    return 100;
  }

  @Override
  public boolean injectFault(FaultParam faultParam) {
    if (!shouldDelay(faultParam, policy)) {
      return false;
    }

    LOGGER.debug("Fault injection: delay is added for the request by fault inject handler");
    long delay = policy.getDelayTimeToMillis();
    if (delay == FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE) {
      LOGGER.debug("Fault injection: delay is not configured");
      return false;
    }

    executeDelay(faultParam, delay);
    return false;
  }

  private void executeDelay(FaultParam faultParam, long delay) {
    Sleepable sleepable = faultParam.getSleepable();
    if (sleepable != null) {
      sleepable.sleep(delay);
    }
  }

  private boolean shouldDelay(FaultParam param, FaultInjectionPolicy policy) {
    int delayPercent = policy.getPercentage();
    if (delayPercent == FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE) {
      LOGGER.debug("Fault injection: delay percentage is not configured");
      return false;
    }
    // check fault delay condition.
    return FaultInjectionUtil.isFaultNeedToInject(param.getReqCount(), delayPercent);
  }

  @Override
  public String getName() {
    return FaultInjectionConst.TYPE_DELAY;
  }
}
