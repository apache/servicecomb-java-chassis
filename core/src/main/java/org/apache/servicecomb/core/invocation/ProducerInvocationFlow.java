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
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response.Status;

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
    invocation.getInvocationStageTrace().startCreateInvocation(this.startTime);
    invocation.getInvocationStageTrace().finishCreateInvocation();
    invocation.onStart(requestEx);
    if (invocation.isEdge()) {
      invocation.getMicroserviceMeta().getEdgeFilterChain()
          .onFilter(invocation)
          .whenComplete((response, throwable) -> {
            if (throwable != null) {
              // Server codec operates on Response. So the filter chain result should be Response and
              // will never throw exception.
              LOGGER.error("Maybe a fatal bug that should be addressed.", throwable);
              response = Response.createFail(new InvocationException(Status.INTERNAL_SERVER_ERROR,
                  new CommonExceptionData("Internal error, check logs for details.")));
            }
            sendResponse(invocation, response);
            finishInvocation(invocation, response);
          });
      return;
    }
    invocation.getMicroserviceMeta().getProviderFilterChain()
        .onFilter(invocation)
        .whenComplete((response, throwable) -> {
          if (throwable != null) {
            // Server codec operates on Response. So the filter chain result should be Response and
            // will never throw exception.
            LOGGER.error("Maybe a fatal bug that should be addressed.", throwable);
            response = Response.createFail(new InvocationException(Status.INTERNAL_SERVER_ERROR,
                new CommonExceptionData("Internal error, check logs for details.")));
          }
          sendResponse(invocation, response);
          finishInvocation(invocation, response);
        });
  }

  private void finishInvocation(Invocation invocation, Response response) {
    invocation.onFinish(response);
  }

  protected abstract Invocation sendCreateInvocationException(Throwable throwable);

  protected abstract void sendResponse(Invocation invocation, Response response);
}
