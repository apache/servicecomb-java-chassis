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

package org.apache.servicecomb.core.invocation;

import static jakarta.ws.rs.core.Response.Status.REQUEST_TIMEOUT;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionCodes;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

public interface InvocationTimeoutStrategy {
  // indicate whether invocation already timeout inside a process
  // null is not timeout
  // other value is timeout
  String CHAIN_ALREADY_TIMED_OUT = "x-scb-chain-timed-out";

  String name();

  void start(Invocation invocation);

  default void startRunInExecutor(Invocation invocation) {
    checkTimeout(invocation);
  }

  default void startHandlers(Invocation invocation) {
    checkTimeout(invocation);
  }

  default void startBusinessMethod(Invocation invocation) {
    checkTimeout(invocation);
  }

  default void finishBusinessMethod(Invocation invocation) {
    checkTimeout(invocation);
  }

  default void beforeSendRequest(Invocation invocation) {
    checkTimeout(invocation);
  }

  default void checkTimeout(Invocation invocation) {
    long nanoInvocationTimeout = invocation.getOperationMeta().getConfig().getNanoInvocationTimeout();
    if (nanoInvocationTimeout <= 0 || alreadyTimeout(invocation)) {
      return;
    }

    long nanoTime = calculateElapsedNanoTime(invocation);
    if (nanoTime <= nanoInvocationTimeout) {
      return;
    }

    invocation.addLocalContext(CHAIN_ALREADY_TIMED_OUT, true);
    throw new InvocationException(REQUEST_TIMEOUT, ExceptionCodes.INVOCATION_TIMEOUT, "Invocation Timeout.");
  }

  default boolean alreadyTimeout(Invocation invocation) {
    return invocation.getLocalContext(CHAIN_ALREADY_TIMED_OUT) != null;
  }

  long calculateElapsedNanoTime(Invocation invocation);
}
