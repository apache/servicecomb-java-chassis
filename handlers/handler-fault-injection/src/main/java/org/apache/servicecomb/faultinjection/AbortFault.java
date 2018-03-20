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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AbortFault extends AbstractFault {
  private static final Logger LOGGER = LoggerFactory.getLogger(FaultInjectionConfig.class);

  @Override
  public FaultResponse injectFault(Invocation invocation, FaultParam faultParam) {
    // get the config values related to delay.
    int abortPercent = FaultInjectionUtil.getFaultInjectionConfig(invocation,
        "abort.percent");

    if (abortPercent == FaultInjectionConst.FAULT_INJECTION_CFG_NULL) {
      LOGGER.info("Fault injection: Abort percentage is not configured");
      return new FaultResponse();
    }

    // check fault abort condition.
    boolean isAbort = FaultInjectionUtil.checkFaultInjectionDelayAndAbort(faultParam.getReqCount(), abortPercent);
    if (isAbort) {
      // get the config values related to delay percentage.
      int errorCode = FaultInjectionUtil.getFaultInjectionConfig(invocation,
          "abort.httpStatus");

      if (errorCode == FaultInjectionConst.FAULT_INJECTION_CFG_NULL) {
        LOGGER.info("Fault injection: Abort error code is not configured");
        return new FaultResponse();
      }
      // if request need to be abort then return failure with given error code
      CommonExceptionData errorData = new CommonExceptionData("aborted by fault inject");
      return new FaultResponse(-1, errorCode, errorData);
    }

    return new FaultResponse();
  }

  @Override
  public int getPriority() {
    return FaultInjectionConst.FAULTINJECTION_PRIORITY_MIN;
  }
}
