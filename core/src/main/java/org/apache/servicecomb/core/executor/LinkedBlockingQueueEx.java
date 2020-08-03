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
package org.apache.servicecomb.core.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

public class LinkedBlockingQueueEx extends LinkedBlockingQueue<Runnable> {
  private static final long serialVersionUID = -1L;

  private transient volatile ThreadPoolExecutorEx owner = null;

  public LinkedBlockingQueueEx(int capacity) {
    super(capacity);
  }

  public void setOwner(ThreadPoolExecutorEx owner) {
    this.owner = owner;
  }

  @Override
  public boolean offer(Runnable runnable) {
    // task can come before owner available
    if (owner == null) {
      return super.offer(runnable);
    }
    // can not create more thread, just queue the task
    if (owner.getPoolSize() == owner.getMaximumPoolSize()) {
      return super.offer(runnable);
    }
    // no need to create more thread, just queue the task
    if (owner.getNotFinished() <= owner.getPoolSize()) {
      return super.offer(runnable);
    }
    // all threads are busy, and can create new thread, not queue the task
    if (owner.getPoolSize() < owner.getMaximumPoolSize()) {
      return false;
    }
    return super.offer(runnable);
  }

  /*
   * when task is rejected (thread pool if full), force the item onto queue.
   */
  public boolean force(Runnable runnable) {
    if (owner == null || owner.isShutdown()) {
      throw new RejectedExecutionException("queue is not running.");
    }
    return super.offer(runnable);
  }
}
