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
package io.vertx.core.impl;

import java.util.concurrent.Executor;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.spi.metrics.PoolMetrics;

public class SyncContext extends EventLoopContext {
  public SyncContext() {
    this(null);
  }

  public SyncContext(VertxInternal vertx) {
    super(vertx, null, null, null, null, null, null);
    if (SyncVertx.class.isInstance(vertx)) {
      ((SyncVertx) vertx).setContext(this);
    }
  }

  @Override
  public void runOnContext(Handler<Void> task) {
    task.handle(null);
  }

  public static <T> void syncExecuteBlocking(Handler<Future<T>> blockingCodeHandler,
      Handler<AsyncResult<T>> asyncResultHandler) {
    Future<T> res = Future.future();

    try {
      blockingCodeHandler.handle(res);
    } catch (Throwable e) {
      res.fail(e);
      return;
    }

    res.setHandler(asyncResultHandler);
  }

  @Override
  public <T> void executeBlocking(Action<T> action, Handler<AsyncResult<T>> resultHandler) {
    syncExecuteBlocking((future) -> {
      try {
        future.complete(action.perform());
      } catch (Throwable e) {
        future.fail(e);
      }
    }, resultHandler);
  }

  @Override
  public <T> void executeBlocking(Handler<Future<T>> blockingCodeHandler, boolean ordered,
      Handler<AsyncResult<T>> asyncResultHandler) {
    syncExecuteBlocking(blockingCodeHandler, asyncResultHandler);
  }

  @Override
  <T> void executeBlocking(Action<T> action, Handler<Future<T>> blockingCodeHandler,
      Handler<AsyncResult<T>> resultHandler,
      Executor exec, TaskQueue queue, @SuppressWarnings("rawtypes") PoolMetrics metrics) {
    syncExecuteBlocking(blockingCodeHandler, resultHandler);
  }
}
