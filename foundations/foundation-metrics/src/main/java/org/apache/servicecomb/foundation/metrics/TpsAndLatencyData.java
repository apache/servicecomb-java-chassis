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

package org.apache.servicecomb.foundation.metrics;

public class TpsAndLatencyData {
  private final long successCount;

  private final long failureCount;

  private final int operationLatency;

  private final long windowInMilliseconds;

  public long getSuccessCount() {
    return successCount;
  }

  public long getFailureCount() {
    return failureCount;
  }

  public int getOperationLatency() {
    return operationLatency;
  }

  public long getWindowInMilliseconds() {
    return windowInMilliseconds;
  }

  public TpsAndLatencyData(long successCount, long failureCount, int operationLatency, long windowInMilliseconds) {
    this.successCount = successCount;
    this.failureCount = failureCount;
    this.operationLatency = operationLatency;
    this.windowInMilliseconds = windowInMilliseconds;
  }
}
