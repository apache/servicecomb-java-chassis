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

package org.apache.servicecomb.foundation.vertx.client;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * CLIENT_POOL是一个完备的连接池，支持向同一个目标建立一个或多个连接
 * 之所以再包装一层，是因为多个线程使用一个连接池的场景下
 * 会导致多个线程抢连接池的同一把锁
 * 包装之后，允许使用m个网络线程，每个线程中有1个连接池
 *
 * support both sync and reactive invoke.
 * 1.sync invoke, bind to a net thread
 * 2.async but not in eventloop, select but not bind to a net thread
 * 3.async and in eventloop, use clientPool in self thread
 *
 * sync/async is not about net operation, just about consumer invoke mode.
 */
public class ClientPoolManager<CLIENT_POOL> {
  private final Vertx vertx;

  private final String id = UUID.randomUUID().toString();

  private final ClientPoolFactory<CLIENT_POOL> factory;

  private final List<CLIENT_POOL> pools = new CopyOnWriteArrayList<>();

  // reactive mode, when call from other thread, must select a context for it
  // if we use threadId to hash a context, will always select the same context from one thread
  private final AtomicInteger reactiveNextIndex = new AtomicInteger();

  public ClientPoolManager(Vertx vertx, ClientPoolFactory<CLIENT_POOL> factory) {
    this.vertx = vertx;
    this.factory = factory;
  }

  public CLIENT_POOL createClientPool(Context context) {
    CLIENT_POOL pool = factory.createClientPool(context);
    addPool(context, pool);
    return pool;
  }

  protected void addPool(Context context, CLIENT_POOL pool) {
    context.put(id, pool);
    pools.add(pool);
  }

  public CLIENT_POOL findClientPool(boolean sync) {
    return findClientPool(sync, null);
  }

  public CLIENT_POOL findClientPool(boolean sync, Context targetContext) {
    if (sync) {
      return findThreadBindClientPool();
    }

    // reactive mode
    return findByContext(targetContext);
  }

  protected CLIENT_POOL findByContext() {
    return findByContext(null);
  }

  protected CLIENT_POOL findByContext(Context targetContext) {
    Context currentContext = targetContext != null ? targetContext : Vertx.currentContext();
    if (currentContext != null
        && currentContext.owner() == vertx
        && currentContext.isEventLoopContext()) {
      // standard reactive mode
      CLIENT_POOL clientPool = currentContext.get(id);
      if (clientPool != null) {
        return clientPool;
      }

      // Maybe executed in a call back of a reactive call.
      // The Context is created in a non-event thread and passed to the event loop
      // thread by vert.x.
    }

    // not in correct context:
    // 1.normal thread
    // 2.vertx worker thread
    // 3.other vertx thread
    // select a existing context
    assertPoolsInitialized();
    int idx = reactiveNextIndex.getAndIncrement() % pools.size();
    if (idx < 0) {
      idx = -idx;
    }
    return pools.get(idx);
  }

  public CLIENT_POOL findThreadBindClientPool() {
    assertPoolsInitialized();
    int idx = (int) (Thread.currentThread().getId() % pools.size());
    return pools.get(idx);
  }

  private void assertPoolsInitialized() {
    if (pools.isEmpty()) {
      throw new IllegalStateException("client pool not initialized successfully when making calls."
          + "Please check if system boot up is ready or some errors happened when startup.");
    }
  }
}
