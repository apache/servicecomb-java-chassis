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
package org.apache.servicecomb.core.filter.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;

public class ScheduleFilter implements ProviderFilter {
  public static final String NAME = "schedule";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.PROVIDER_SCHEDULE_FILTER_ORDER;
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode next) {
    invocation.getInvocationStageTrace().startSchedule();
    Executor executor = invocation.getOperationMeta().getExecutor();
    return CompletableFuture.completedFuture(null)
        .thenComposeAsync(response -> runInExecutor(invocation, next), executor);
  }

  protected CompletableFuture<Response> runInExecutor(Invocation invocation, FilterNode next) {
    invocation.onExecuteStart();

    try {
      InvocationStageTrace trace = invocation.getInvocationStageTrace();
      trace.startServerFiltersRequest();
      invocation.onStartHandlersRequest();

      checkInQueueTimeout(invocation);

      return next.onFilter(invocation)
          .whenComplete((response, throwable) -> whenComplete(invocation));
    } finally {
      invocation.onExecuteFinish();
    }
  }

  private void checkInQueueTimeout(Invocation invocation) {
    long nanoTimeout = invocation.getOperationMeta().getConfig()
        .getNanoRequestWaitInPoolTimeout(invocation.getTransport().getName());

    if (System.nanoTime() - invocation.getInvocationStageTrace().getStart() > nanoTimeout) {
      throw Exceptions.genericProducer("Request in the queue timed out.");
    }
  }

  private void whenComplete(Invocation invocation) {
    invocation.getInvocationStageTrace().finishHandlersResponse();
    invocation.getInvocationStageTrace().finishServerFiltersResponse();
  }
}
