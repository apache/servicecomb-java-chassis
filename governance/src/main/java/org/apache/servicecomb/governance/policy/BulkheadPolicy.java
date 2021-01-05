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

package org.apache.servicecomb.governance.policy;

import org.apache.servicecomb.governance.handler.BulkheadHandler;

public class BulkheadPolicy extends AbstractPolicy {

  public static final int DEFAULT_MAX_CONCURRENT_CALLS = 1000;

  public static final int DEFAULT_MAX_WAIT_DURATION = 0;

  private int maxConcurrentCalls = DEFAULT_MAX_CONCURRENT_CALLS;

  private int maxWaitDuration = DEFAULT_MAX_WAIT_DURATION;

  public int getMaxConcurrentCalls() {
    return maxConcurrentCalls;
  }

  public void setMaxConcurrentCalls(int maxConcurrentCalls) {
    this.maxConcurrentCalls = maxConcurrentCalls;
  }

  public int getMaxWaitDuration() {
    return maxWaitDuration;
  }

  public void setMaxWaitDuration(int maxWaitDuration) {
    this.maxWaitDuration = maxWaitDuration;
  }

  @Override
  public boolean isValid() {
    if (maxConcurrentCalls <= 0) {
      return false;
    }
    if (maxWaitDuration < 0) {
      return false;
    }
    return super.isValid();
  }

  @Override
  public String handler() {
    return BulkheadHandler.class.getSimpleName();
  }

  @Override
  public String toString() {
    return "BulkheadPolicy{" +
        "maxConcurrentCalls=" + maxConcurrentCalls +
        ", maxWaitDuration=" + maxWaitDuration +
        '}';
  }
}
