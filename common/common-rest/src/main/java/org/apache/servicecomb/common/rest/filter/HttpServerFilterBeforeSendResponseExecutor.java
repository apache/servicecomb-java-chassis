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
  private final List<HttpServerFilter> httpServerFilters;

  private final Invocation invocation;

  private final HttpServletResponseEx responseEx;

  private int currentIndex;

  private final CompletableFuture<Void> future = new CompletableFuture<>();

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
      if (httpServerFilter.enabled()) {
        CompletableFuture<Void> future = httpServerFilter.beforeSendResponseAsync(invocation, responseEx);
        if (future == null) {
          future = new CompletableFuture<>();
          future.completeExceptionally(new IllegalStateException(
              "HttpServerFilter beforeSendResponseAsync can not return null. Class="
                  + httpServerFilter.getClass()
                  .getName()));
        }
        return future;
      } else {
        CompletableFuture<Void> eFuture = new CompletableFuture<>();
        eFuture.complete(null);
        return eFuture;
      }
    } catch (Throwable e) {
      CompletableFuture<Void> eFuture = new CompletableFuture<>();
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
