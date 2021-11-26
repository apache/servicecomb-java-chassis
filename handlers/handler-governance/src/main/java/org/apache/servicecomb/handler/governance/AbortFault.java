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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;
import org.apache.servicecomb.injection.FaultInjectionConst;
import org.apache.servicecomb.injection.FaultInjectionUtil;
import org.apache.servicecomb.injection.FaultParam;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AbortFault implements Fault {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbortFault.class);

  public static final String ABORTED_ERROR_MSG = "aborted by fault inject";

  @Override
  public void injectFault(Invocation invocation, FaultParam param, FaultInjectionPolicy policy,
      AsyncResponse asyncResponse) {
    if (!shouldAbort(invocation, param, policy)) {
      asyncResponse.success(SUCCESS_RESPONSE);
      return;
    }

    // get the config values related to abort percentage.
    int errorCode = policy.getErrorCode();
    if (errorCode == FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE) {
      LOGGER.debug("Fault injection: Abort error code is not configured");
      asyncResponse.success(SUCCESS_RESPONSE);
      return;
    }

    // if request need to be abort then return failure with given error code
    CommonExceptionData errorData = new CommonExceptionData(ABORTED_ERROR_MSG);
    asyncResponse.consumerFail(new InvocationException(errorCode, ABORTED_ERROR_MSG, errorData));
  }

  private boolean shouldAbort(Invocation invocation, FaultParam param, FaultInjectionPolicy policy) {
    // get the config values related to abort.
    int abortPercent = policy.getPercentage();
    if (abortPercent == FaultInjectionConst.FAULT_INJECTION_DEFAULT_VALUE) {
      LOGGER.debug("Fault injection: Abort percentage is not configured");
      return false;
    }

    // check fault abort condition.
    return FaultInjectionUtil.isFaultNeedToInject(param.getReqCount(), abortPercent);
  }

  @Override
  public int getOrder() {
    return 200;
  }
}
