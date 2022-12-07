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

public abstract class AbstractFault implements Fault {
  protected String key;

  protected FaultInjectionPolicy policy;

  public AbstractFault(String key, FaultInjectionPolicy policy) {
    this.key = key;
    this.policy = policy;
  }

  @Override
  public boolean injectFault() {
    if (policy.isForceClosed()) {
      return false;
    }
    FaultParam faultParam = FaultInjectionUtil.initFaultParam(key);
    return injectFault(faultParam);
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public FaultInjectionPolicy getPolicy() {
    return policy;
  }
}
