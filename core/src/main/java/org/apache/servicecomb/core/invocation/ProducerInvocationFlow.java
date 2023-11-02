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

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.foundation.common.utils.ExceptionUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProducerInvocationFlow {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerInvocationFlow.class);

  private final long startTime = System.nanoTime();

  private final InvocationCreator invocationCreator;

  protected final HttpServletRequestEx requestEx;

  protected final HttpServletResponseEx responseEx;

  public ProducerInvocationFlow(InvocationCreator invocationCreator) {
    this(invocationCreator, null, null);
  }

  public ProducerInvocationFlow(InvocationCreator invocationCreator,
      HttpServletRequestEx requestEx, HttpServletResponseEx responseEx) {
    this.invocationCreator = invocationCreator;
    this.requestEx = requestEx;
    this.responseEx = responseEx;
  }

  public void run() {
    CompletableFuture.completedFuture(null)
        .thenCompose(v -> invocationCreator.createAsync())
        .exceptionally(this::sendCreateInvocationException)
        .thenAccept(this::tryRunInvocation);
  }

  private void tryRunInvocation(Invocation invocation) {
    if (invocation == null) {
      return;
    }

    invocation.onStart(requestEx, startTime);
    if (invocation.isEdge()) {
      invocation.getMicroserviceMeta().getEdgeFilterChain()
          .onFilter(invocation)
          .whenComplete((response, Throwable) -> sendResponse(invocation, response))
          .whenComplete((response, Throwable) -> finishInvocation(invocation, response, Throwable));
      return;
    }
    invocation.getMicroserviceMeta().getProviderFilterChain()
        .onFilter(invocation)
        .whenComplete((response, Throwable) -> sendResponse(invocation, response))
        .whenComplete((response, Throwable) -> finishInvocation(invocation, response, Throwable));
  }

  private void finishInvocation(Invocation invocation, Response response, Throwable throwable) {
    invocation.onFinish(response);

    tryLogException(invocation, throwable);
  }

  private void tryLogException(Invocation invocation, Throwable throwable) {
    if (throwable == null) {
      return;
    }

    throwable = Exceptions.unwrap(throwable);
    if (requestEx == null) {
      logException(invocation, throwable);
      return;
    }

    logException(invocation, requestEx, throwable);
  }

  private void logException(Invocation invocation, Throwable throwable) {
    if (Exceptions.isPrintInvocationStackTrace()) {
      LOGGER.error("Failed to finish invocation, operation:{}.", invocation.getMicroserviceQualifiedName(), throwable);
      return;
    }

    LOGGER.error("Failed to finish invocation, operation:{}, message={}.", invocation.getMicroserviceQualifiedName(),
        ExceptionUtils.getExceptionMessageWithoutTrace(throwable));
  }

  private void logException(Invocation invocation, HttpServletRequestEx requestEx, Throwable throwable) {
    if (Exceptions.isPrintInvocationStackTrace()) {
      LOGGER.error("Failed to finish invocation, operation:{}, request uri:{}.",
          invocation.getMicroserviceQualifiedName(), requestEx.getRequestURI(), throwable);
      return;
    }

    LOGGER.error("Failed to finish invocation, operation:{}, request uri:{}, message={}.",
        invocation.getMicroserviceQualifiedName(), requestEx.getRequestURI(),
        ExceptionUtils.getExceptionMessageWithoutTrace(throwable));
  }

  protected abstract Invocation sendCreateInvocationException(Throwable throwable);

  protected abstract void sendResponse(Invocation invocation, Response response);
}
