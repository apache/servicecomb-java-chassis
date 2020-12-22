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
package org.apache.servicecomb.core.filter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.swagger.invocation.Response;

public class SimpleRetryFilter implements ConsumerFilter {
  protected int maxRetry = 3;

  @Nonnull
  @Override
  public String getName() {
    return "simple-retry";
  }

  public SimpleRetryFilter setMaxRetry(int maxRetry) {
    this.maxRetry = maxRetry;
    return this;
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    return new RetrySession(invocation, nextNode).run();
  }

  protected Throwable unwrapException(Throwable throwable) {
    return Exceptions.unwrapIncludeInvocationException(throwable);
  }

  protected boolean isRetryException(Throwable throwable) {
    return !(throwable instanceof IOException);
  }

  class RetrySession {
    Invocation invocation;

    FilterNode nextNode;

    int retryCount;

    CompletableFuture<Response> future = new CompletableFuture<>();

    RetrySession(Invocation invocation, FilterNode nextNode) {
      this.invocation = invocation;
      this.nextNode = nextNode;
    }

    CompletableFuture<Response> run() {
      nextNode.onFilter(invocation)
          .whenComplete(this::whenNextComplete);
      return future;
    }

    private void whenNextComplete(Response response, Throwable throwable) {
      if (throwable == null) {
        future.complete(response);
        return;
      }

      Throwable unwrapped = unwrapException(throwable);
      if (isRetryException(unwrapped)) {
        future.completeExceptionally(throwable);
        return;
      }

      retryCount++;
      if (retryCount >= maxRetry) {
        future.completeExceptionally(throwable);
        return;
      }

      run();
    }
  }
}
