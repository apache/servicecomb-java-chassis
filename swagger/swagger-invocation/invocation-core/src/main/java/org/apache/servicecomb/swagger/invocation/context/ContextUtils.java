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

package org.apache.servicecomb.swagger.invocation.context;

import org.slf4j.MDC;

import java.util.concurrent.CompletableFuture;

/**
 * 传递调用过程的上下文数据
 */
public final class ContextUtils {
  public static final String TRACE_ID_NAME = "X-B3-TraceId";

  public static final String KEY_TRACE_ID = "SERVICECOMB_TRACE_ID";

  private ContextUtils() {
  }

  private static ThreadLocal<InvocationContext> contextMgr = new ThreadLocal<>();

  public static InvocationContext getInvocationContext() {
    return contextMgr.get();
  }

  public static InvocationContext getAndRemoveInvocationContext() {
    InvocationContext context = contextMgr.get();
    if (context != null) {
      MDC.remove(KEY_TRACE_ID);
      contextMgr.remove();
    }
    return context;
  }

  public static void setInvocationContext(InvocationContext invocationContext) {
    String traceId = invocationContext.getContext(TRACE_ID_NAME);
    if (traceId != null) {
      MDC.put(KEY_TRACE_ID, traceId);
    }
    contextMgr.set(invocationContext);
  }

  public static void removeInvocationContext() {
    MDC.remove(KEY_TRACE_ID);
    contextMgr.remove();
  }

  /**
   *
   * @param future must be InvocationContextCompletableFuture, that is returned from consumer api
   * @return
   */
  public static InvocationContext getFromCompletableFuture(CompletableFuture<?> future) {
    if (future instanceof InvocationContextCompletableFuture) {
      return ((InvocationContextCompletableFuture<?>) future).getContext();
    }

    return null;
  }
}
