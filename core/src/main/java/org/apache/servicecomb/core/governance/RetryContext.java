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
package org.apache.servicecomb.core.governance;

public class RetryContext {
  public static final String RETRY_CONTEXT = "x-context-retry";

  public static final String RETRY_LOAD_BALANCE = "x-context-retry-loadbalance";

  private boolean retry;

  private int triedCount;

  private final int retryOnSame;

  public RetryContext(int retryOnSame) {
    this.retryOnSame = retryOnSame;
    this.retry = false;
    this.triedCount = 0;
  }

  public boolean isRetry() {
    return retry;
  }

  public void incrementRetry() {
    this.retry = true;
    this.triedCount++;
  }

  public boolean trySameServer() {
    return triedCount <= retryOnSame;
  }
}
