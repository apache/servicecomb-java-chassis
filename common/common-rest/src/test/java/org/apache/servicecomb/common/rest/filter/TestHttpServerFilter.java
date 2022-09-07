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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestHttpServerFilter {

  @Test
  public void asyncSucc() throws InterruptedException, ExecutionException {
    HttpServerFilter filter = new HttpServerFilterBaseForTest();

    CompletableFuture<Void> future = filter.beforeSendResponseAsync(null, null);
    Assertions.assertNull(future.get());
  }

  @Test
  public void asyncFailed() throws InterruptedException, ExecutionException {
    HttpServerFilter filter = new HttpServerFilterBaseForTest() {
      @Override
      public CompletableFuture<Void> beforeSendResponseAsync(Invocation invocation, HttpServletResponseEx responseEx) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        result.completeExceptionally(new RuntimeExceptionWithoutStackTrace());
        return result;
      }
    };

    ExecutionException exception = Assertions.assertThrows(ExecutionException.class,
        () -> {
          CompletableFuture<Void> future = filter.beforeSendResponseAsync(null, null);
          future.get();
        });
    Assertions.assertTrue(exception.getCause() instanceof RuntimeExceptionWithoutStackTrace);
  }
}
