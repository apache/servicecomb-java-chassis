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
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerAbortFaultFilter implements ConsumerFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerAbortFaultFilter.class);

  private static final String SUCCESS_RESPONSE = "success";

  public static final String ABORTED_ERROR_MSG = "aborted by fault inject";

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER + 1020;
  }

  @Override
  public String getName() {
    return "consumer-abort-fault";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    if (!shouldAbort(invocation)) {
      return nextNode.onFilter(invocation);
    }

    // get the config values related to abort percentage.
    int errorCode = FaultInjectionUtil.getFaultInjectionConfig(invocation, "abort.httpStatus");
    if (errorCode == FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE) {
      LOGGER.debug("Fault injection: Abort error code is not configured");
      return CompletableFuture.completedFuture(Response.succResp(SUCCESS_RESPONSE));
    }

    // if request need to be abort then return failure with given error code
    CommonExceptionData errorData = new CommonExceptionData(ABORTED_ERROR_MSG);
    return CompletableFuture.failedFuture(new InvocationException(errorCode, ABORTED_ERROR_MSG, errorData));
  }

  private boolean shouldAbort(Invocation invocation) {
    // get the config values related to abort.
    int abortPercent = FaultInjectionUtil.getFaultInjectionConfig(invocation, "abort.percent");
    if (abortPercent == FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE) {
      LOGGER.debug("Fault injection: Abort percentage is not configured");
      return false;
    }

    // check fault abort condition.
    return FaultInjectionUtil.isFaultNeedToInject(abortPercent);
  }
}
