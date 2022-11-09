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

public class AbortFault extends AbstractFault {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbortFault.class);

  public static final String ABORTED_ERROR_MSG = "aborted by fault inject";

  public AbortFault(String key, FaultInjectionPolicy policy) {
    super(key, policy);
  }

  @Override
  public boolean injectFault(FaultParam faultParam) {
    return shouldAbort(faultParam, policy);
  }

  private boolean shouldAbort(FaultParam param, FaultInjectionPolicy policy) {
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

  @Override
  public String getName() {
    return FaultInjectionConst.TYPE_ABORT;
  }
}
