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

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

public class ConsumerDelayFaultFilter implements ConsumerFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerDelayFaultFilter.class);

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER + 1030;
  }

  @Override
  public String getName() {
    return "consumer-delay-fault";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    if (!shouldDelay(invocation)) {
      return nextNode.onFilter(invocation);
    }

    LOGGER.debug("Fault injection: delay is added for the request by fault inject handler");
    long delay = FaultInjectionUtil.getFaultInjectionConfig(invocation,
        "delay.fixedDelay");
    if (delay == FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE) {
      LOGGER.debug("Fault injection: delay is not configured");
      return nextNode.onFilter(invocation);
    }

    return executeDelay(invocation, nextNode, delay);
  }

  private CompletableFuture<Response> executeDelay(Invocation invocation, FilterNode nextNode,
      long delay) {
    Context currentContext = Vertx.currentContext();
    if (currentContext != null && currentContext.isEventLoopContext()) {
      CompletableFuture<Response> result = new CompletableFuture<>();
      currentContext.owner().setTimer(delay, timeID -> nextNode.onFilter(invocation).whenComplete((r, e) -> {
            if (e == null) {
              result.complete(r);
            } else {
              result.completeExceptionally(e);
            }
          }
      ));
      return result;
    }

    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      LOGGER.info("Interrupted exception is received");
    }
    return nextNode.onFilter(invocation);
  }

  private boolean shouldDelay(Invocation invocation) {
    int delayPercent = FaultInjectionUtil.getFaultInjectionConfig(invocation,
        "delay.percent");
    if (delayPercent == FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE) {
      LOGGER.debug("Fault injection: delay percentage is not configured");
      return false;
    }

    // check fault delay condition.
    return FaultInjectionUtil.isFaultNeedToInject(delayPercent);
  }
}
