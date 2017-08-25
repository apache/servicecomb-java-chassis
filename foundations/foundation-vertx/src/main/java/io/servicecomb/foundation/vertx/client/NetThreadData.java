/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.vertx.client;

import java.util.concurrent.atomic.AtomicInteger;

public class NetThreadData<CLIENT_POOL> {
  // 每个网络线程提供一个factory
  private ClientPoolFactory<CLIENT_POOL> factory;

  // 每个网络线程有多个连接池
  private CLIENT_POOL[] pools;

  private AtomicInteger bindIndex = new AtomicInteger();

  @SuppressWarnings("unchecked")
  public NetThreadData(ClientPoolFactory<CLIENT_POOL> factory, int poolCount) {
    this.factory = factory;
    pools = (CLIENT_POOL[]) new Object[poolCount];
  }

  public ClientPoolFactory<CLIENT_POOL> getFactory() {
    return factory;
  }

  public CLIENT_POOL[] getPools() {
    return pools;
  }

  public AtomicInteger getBindIndex() {
    return bindIndex;
  }

  /**
   * 在ClientPoolManager中被调用，是被锁保护的
   */
  public CLIENT_POOL selectClientPool() {
    int idx = bindIndex.getAndIncrement() % pools.length;
    CLIENT_POOL clientPool = pools[idx];
    if (clientPool == null) {
      clientPool = factory.createClientPool();
      pools[idx] = clientPool;
    }
    return clientPool;
  }
}
