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

package org.apache.servicecomb.faultinjection;

import java.util.concurrent.CountDownLatch;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;

@Component
public class DelayFault extends AbstractFault {
  private static final Logger LOGGER = LoggerFactory.getLogger(FaultInjectionHandler.class);

  @Override
  public int getPriority() {
    return FaultInjectionConst.FAULTINJECTION_PRIORITY_MAX;
  }

  @Override
  public FaultResponse injectFault(Invocation invocation, FaultParam faultAttributes) {
    int delayPercent = FaultInjectionUtil.getFaultInjectionConfig(invocation,
        "delay.percent");

    if (delayPercent == FaultInjectionConst.FAULT_INJECTION_CFG_NULL) {
      LOGGER.info("Fault injection: delay percentage is not configured");
      return new FaultResponse();
    }

    // check fault delay condition.
    boolean isDelay = FaultInjectionUtil.checkFaultInjectionDelayAndAbort(faultAttributes.getReqCount(), delayPercent);
    if (isDelay) {
      LOGGER.info("Fault injection: delay is added for the request by fault inject handler");
      long delay = FaultInjectionUtil.getFaultInjectionConfig(invocation,
          "delay.fixedDelay");

      if (delay == FaultInjectionConst.FAULT_INJECTION_CFG_NULL) {
        LOGGER.info("Fault injection: delay is not configured");
        return new FaultResponse();
      }

      CountDownLatch latch = new CountDownLatch(1);
      Vertx vertx = VertxUtils.getOrCreateVertxByName("faultinjection", null);
      vertx.setTimer(delay, new Handler<Long>() {
        @Override
        public void handle(Long timeID) {
          latch.countDown();
        }
      });

      try {
        latch.await();
      } catch (InterruptedException e) {
        LOGGER.info("Interrupted exception is received");
      }
    }

    return new FaultResponse();
  }

}
