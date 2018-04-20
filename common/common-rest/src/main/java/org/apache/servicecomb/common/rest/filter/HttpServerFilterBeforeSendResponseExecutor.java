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
package org.apache.servicecomb.common.rest.filter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;

public class HttpServerFilterBeforeSendResponseExecutor {
  private List<HttpServerFilter> httpServerFilters;

  private Invocation invocation;

  private HttpServletResponseEx responseEx;

  private int currentIndex;

  private CompletableFuture<Void> future = new CompletableFuture<Void>();

  public HttpServerFilterBeforeSendResponseExecutor(List<HttpServerFilter> httpServerFilters, Invocation invocation,
      HttpServletResponseEx responseEx) {
    this.httpServerFilters = httpServerFilters;
    this.invocation = invocation;
    this.responseEx = responseEx;
  }

  public CompletableFuture<Void> run() {
    doRun();

    return future;
  }

  protected CompletableFuture<Void> safeInvoke(HttpServerFilter httpServerFilter) {
    try {
      return httpServerFilter.beforeSendResponseAsync(invocation, responseEx);
    } catch (Throwable e) {
      CompletableFuture<Void> eFuture = new CompletableFuture<Void>();
      eFuture.completeExceptionally(e);
      return eFuture;
    }
  }

  protected void doRun() {
    if (currentIndex == httpServerFilters.size()) {
      future.complete(null);
      return;
    }

    HttpServerFilter httpServerFilter = httpServerFilters.get(currentIndex);
    currentIndex++;

    CompletableFuture<Void> stepFuture = safeInvoke(httpServerFilter);
    stepFuture.whenComplete((v, e) -> {
      if (e == null) {
        doRun();
        return;
      }

      future.completeExceptionally(e);
    });
  }
}
