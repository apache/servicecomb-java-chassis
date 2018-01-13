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

package org.apache.servicecomb.core.metrics;

import org.apache.servicecomb.foundation.common.event.Event;
import org.apache.servicecomb.swagger.invocation.InvocationType;

public class InvocationStartedEvent implements Event {
  private final String operationName;

  private final InvocationType invocationType;

  private final long startedTime;

  public String getOperationName() {
    return operationName;
  }

  public InvocationType getInvocationType() {
    return invocationType;
  }

  public long getStartedTime() {
    return startedTime;
  }

  public InvocationStartedEvent(String operationName, InvocationType invocationType, long startedTime) {
    this.operationName = operationName;
    this.invocationType = invocationType;
    this.startedTime = startedTime;
  }
}
