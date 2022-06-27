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

package org.apache.servicecomb.handler.governance.injection;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;
import org.apache.servicecomb.injection.Fault;
import org.apache.servicecomb.injection.FaultInjectionUtil;
import org.apache.servicecomb.injection.FaultParam;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * Implements the fault feature execution one after other.
 */
public class FaultExecutor {
  private static final List<Fault> FAULT_INJECTION_LIST = SPIServiceUtils.getSortedService(Fault.class);

  private final String key;

  private final FaultInjectionPolicy policy;

  public FaultExecutor(String key, FaultInjectionPolicy policy) {
    this.key = key;
    this.policy = policy;
  }

  public void execute(AsyncResponse asyncResponse) {
    Fault fault = null;
    for (Fault f : FAULT_INJECTION_LIST) {
      if (policy.getType().equals(f.getName())) {
        fault = f;
        break;
      }
    }

    if (fault != null) {
      FaultParam param = initFaultParam();
      fault.injectFault(param, policy, faultResponse -> {
        if (!faultResponse.isSuccess()) {
          // if request need to be abort then return failure with given error code
          CommonExceptionData errorData = new CommonExceptionData(faultResponse.getErrorMsg());
          asyncResponse.complete(Response.failResp(
              new InvocationException(faultResponse.getErrorCode(), faultResponse.getErrorMsg(), errorData)));
          //throw new InvocationException(faultResponse.getErrorCode(), faultResponse.getErrorMsg(), errorData);
        } else {
          asyncResponse.complete(Response.succResp("success"));
        }
      });
    } else {
      asyncResponse.complete(Response.succResp("success"));
    }
  }

  private FaultParam initFaultParam() {
    AtomicLong reqCount = FaultInjectionUtil.getOperMetTotalReq(key);
    // increment the request count here after checking the delay/abort condition.
    long reqCountCurrent = reqCount.getAndIncrement();

    FaultParam param = new FaultParam(reqCountCurrent);
    Context currentContext = Vertx.currentContext();
    if (currentContext != null && currentContext.owner() != null && currentContext.isEventLoopContext()) {
      param.setSleepable(
          (delay, sleepCallback) -> currentContext.owner().setTimer(delay, timeId -> sleepCallback.callback()));
    }
    return param;
  }

  public static FaultExecutor getFaultExecutor(Invocation invocation, GovernanceRequest request) {
    FaultInjectionHandler faultInjectionHandler = BeanUtils.getBean(FaultInjectionHandler.class);
    if (faultInjectionHandler != null) {
      FaultInjectionPolicy faultInject = faultInjectionHandler.getActuator(request);
      if (faultInject != null) {
        return new FaultExecutor(getInjectFaultKey(invocation), faultInject);
      }
    }
    return null;
  }

  private static String getInjectFaultKey(Invocation invocation) {
    return invocation.getTransport().getName() + invocation.getMicroserviceQualifiedName();
  }
}
