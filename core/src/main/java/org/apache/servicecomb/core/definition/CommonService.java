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

package org.apache.servicecomb.core.definition;

import java.util.Collection;

import org.apache.servicecomb.foundation.common.RegisterManager;

public class CommonService<OPERATION> {
  protected String name;

  protected RegisterManager<String, OPERATION> operationMgr;

  public void createOperationMgr(String operationMgrName) {
    operationMgr = new RegisterManager<>(operationMgrName);
  }

  public void regOperation(String operationName, OPERATION operation) {
    operationMgr.register(operationName, operation);
  }

  public OPERATION findOperation(String operation) {
    return operationMgr.findValue(operation);
  }

  public OPERATION ensureFindOperation(String operation) {
    return operationMgr.ensureFindValue(operation);
  }

  public Collection<OPERATION> getOperations() {
    return operationMgr.values();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
