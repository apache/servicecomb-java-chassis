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

import java.util.List;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;
import org.apache.servicecomb.injection.FaultParam;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;


/**
 * Implements the fault feature execution one after other.
 */
public class FaultExecutor {
  private List<Fault> faultInjectList;

  private int handlerIndex = 0;

  private Invocation invocation;

  private FaultParam param;

  private FaultInjectionPolicy policy;

  private Fault fault;

  public FaultExecutor(List<Fault> faultInjectList, Invocation invocation, FaultParam param,
      FaultInjectionPolicy policy) {
    this.faultInjectList = faultInjectList;
    this.invocation = invocation;
    this.param = param;
    this.policy = policy;
  }

  public void execute(AsyncResponse asyncResponse) {
    if (policy.getType().equals("delay")) {
      fault = new DelayFault();
    } else {
      fault = new AbortFault();
    }
    fault.injectFault(invocation, param, policy, response -> {
      if (response.isFailed()) {
        asyncResponse.complete(response);
      } else {
        asyncResponse.complete(Response.succResp("success"));
      }
    });
  }
}
