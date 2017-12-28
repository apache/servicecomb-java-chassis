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

package io.servicecomb.foundation.vertx.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CLIENT_POOL是一个完备的连接池，支持向同一个目标建立一个或多个连接
 * 之所以再包装一层，是因为多个线程使用一个连接池的场景下
 * 会导致多个线程抢连接池的同一把锁
 * 包装之后，允许使用m个网络线程，每个线程中有1个连接池
 * 
 * ClientPoolManager only optimized for invoker not in eventloop
 */
public class ClientPoolManager<CLIENT_POOL> {
  private List<CLIENT_POOL> pools = new CopyOnWriteArrayList<>();

  private AtomicInteger bindIndex = new AtomicInteger();

  // send的调用线程与CLIENT_POOL的绑定关系，不直接用hash，是担心分配不均
  // key是调用者的线程id
  // TODO:要不要考虑已经绑定的线程消失了的场景？
  private Map<Long, CLIENT_POOL> threadBindMap = new ConcurrentHashMap<>();

  public void addPool(CLIENT_POOL pool) {
    pools.add(pool);
  }

  public CLIENT_POOL findThreadBindClientPool() {
    long threadId = Thread.currentThread().getId();
    return threadBindMap.computeIfAbsent(threadId, tid -> {
      int idx = bindIndex.getAndIncrement() % pools.size();
      return pools.get(idx);
    });
  }
}
