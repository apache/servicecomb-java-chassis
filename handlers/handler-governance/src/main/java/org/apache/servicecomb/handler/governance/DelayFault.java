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

package org.apache.servicecomb.handler.governance;

import java.time.Duration;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;
import org.apache.servicecomb.injection.FaultInjectionConst;
import org.apache.servicecomb.injection.FaultInjectionUtil;
import org.apache.servicecomb.injection.FaultParam;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.Vertx;

@Component
public class DelayFault implements Fault {
  private static final Logger LOGGER = LoggerFactory.getLogger(DelayFault.class);

  @Override
  public int getOrder() {
    return 100;
  }

  @Override
  public void injectFault(Invocation invocation, FaultParam param, FaultInjectionPolicy policy,
      AsyncResponse asynResponse) {
    if (!shouldDelay(invocation, param, policy, asynResponse)) {
      asynResponse.success(SUCCESS_RESPONSE);
      return;
    }

    LOGGER.debug("Fault injection: delay is added for the request by fault inject handler");
    long delay = Duration.parse(policy.getDelayTime()).toMillis();
    if (delay == FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE) {
      LOGGER.debug("Fault injection: delay is not configured");
      asynResponse.success(SUCCESS_RESPONSE);
      return;
    }

    executeDelay(param, asynResponse, delay);
  }

  private void executeDelay(FaultParam faultParam, AsyncResponse asynResponse, long delay) {
    Vertx vertx = faultParam.getVertx();
    if (vertx != null) {
      vertx.setTimer(delay, timeID -> asynResponse.success(SUCCESS_RESPONSE));
      return;
    }

    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      LOGGER.info("Interrupted exception is received");
    }
    asynResponse.success(SUCCESS_RESPONSE);
  }

  private boolean shouldDelay(Invocation invocation, FaultParam param, FaultInjectionPolicy policy,
      AsyncResponse asynResponse) {
    int delayPercent = policy.getPercentage();
    if (delayPercent == FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE) {
      LOGGER.debug("Fault injection: delay percentage is not configured");
      return false;
    }
    // check fault delay condition.
    return FaultInjectionUtil.isFaultNeedToInject(param.getReqCount(), delayPercent);
  }
}
