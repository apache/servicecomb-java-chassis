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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.netty.channel.EventLoop;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.ThreadingModel;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.tracing.VertxTracer;

/**
 * This class is created to make vertx unit test easier
 */
public class SyncContext extends ContextBase implements ContextInternal {
  protected VertxInternal owner;

  protected Executor executor = Executors.newSingleThreadExecutor();

  public SyncContext() {
    this(0);
  }

  public SyncContext(int localsLength) {
    super(localsLength);
  }

  @Override
  public VertxInternal owner() {
    return owner;
  }

  @Override
  public Context exceptionHandler(@Nullable Handler<Throwable> handler) {
    return null;
  }

  @Override
  public @Nullable Handler<Throwable> exceptionHandler() {
    return null;
  }

  @Override
  public boolean inThread() {
    return false;
  }

  @Override
  public <T> void emit(T t, Handler<T> handler) {

  }

  @Override
  public void execute(Runnable runnable) {

  }

  @Override
  public <T> void execute(T t, Handler<T> handler) {

  }

  @Override
  public void reportException(Throwable throwable) {

  }

  @Override
  public ConcurrentMap<Object, Object> contextData() {
    return null;
  }

  @Override
  public ClassLoader classLoader() {
    return null;
  }

  @Override
  public WorkerPool workerPool() {
    return null;
  }

  @Override
  public VertxTracer tracer() {
    return null;
  }

  @Override
  public ContextInternal duplicate() {
    return null;
  }

  @Override
  public CloseFuture closeFuture() {
    return null;
  }

  public void setOwner(VertxInternal owner) {
    this.owner = owner;
  }


  public static <T> void syncExecuteBlocking(Handler<Promise<T>> blockingCodeHandler,
      Handler<AsyncResult<T>> asyncResultHandler) {
    Promise<T> res = Promise.promise();

    try {
      blockingCodeHandler.handle(res);
    } catch (Throwable e) {
      res.fail(e);
      return;
    }

    res.future().onComplete(asyncResultHandler);
  }

  private static <T> Future<T> syncExecuteBlocking(Handler<Promise<T>> blockingCodeHandler) {
    Promise<T> res = Promise.promise();

    try {
      blockingCodeHandler.handle(res);
    } catch (Throwable e) {
      res.fail(e);
      return res.future();
    }

    return res.future();
  }

  @Override
  public <T> Future<T> executeBlockingInternal(Handler<Promise<T>> action) {
    return syncExecuteBlocking(action);
  }

  @Override
  public <T> Future<T> executeBlockingInternal(Callable<T> callable) {
    return null;
  }

  @Override
  public <T> Future<T> executeBlockingInternal(Handler<Promise<T>> handler, boolean b) {
    return null;
  }

  @Override
  public <T> Future<T> executeBlockingInternal(Callable<T> callable, boolean b) {
    return null;
  }

  @Override
  public Deployment getDeployment() {
    return null;
  }

  @Override
  public Executor executor() {
    return executor;
  }

  @Override
  public EventLoop nettyEventLoop() {
    return null;
  }

  @Override
  public <T> Future<T> executeBlocking(Handler<Promise<T>> handler, TaskQueue taskQueue) {
    return null;
  }

  @Override
  public <T> Future<T> executeBlocking(Callable<T> callable, TaskQueue taskQueue) {
    return null;
  }

  @Override
  public <T> void executeBlocking(Handler<Promise<T>> blockingCodeHandler, boolean ordered,
      Handler<AsyncResult<T>> asyncResultHandler) {
    syncExecuteBlocking(blockingCodeHandler, asyncResultHandler);
  }

  @Override
  public <T> Future<@Nullable T> executeBlocking(Callable<T> callable, boolean b) {
    return null;
  }

  @Override
  public <T> Future<@Nullable T> executeBlocking(Handler<Promise<T>> handler, boolean b) {
    return null;
  }

  @Override
  public @Nullable JsonObject config() {
    return null;
  }

  @Override
  public boolean isEventLoopContext() {
    return false;
  }

  @Override
  public boolean isWorkerContext() {
    return false;
  }

  @Override
  public ThreadingModel threadingModel() {
    return null;
  }
}
