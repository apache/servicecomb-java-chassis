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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkedBlockingQueueEx<E> extends LinkedBlockingQueue<E> {
  private static final long serialVersionUID = -1L;

  private static final int COUNT_BITS = Integer.SIZE - 3;

  private static final int CAPACITY = (1 << COUNT_BITS) - 1;

  private static int workerCountOf(int c) {
    return c & CAPACITY;
  }

  private static Method addWrokerMethod;

  private transient volatile ThreadPoolExecutorEx owner = null;

  private AtomicInteger ctl;

  public LinkedBlockingQueueEx(int capacity) {
    super(capacity);
  }

  public void setOwner(ThreadPoolExecutorEx owner) {
    this.owner = owner;
    try {
      addWrokerMethod = ThreadPoolExecutor.class.getDeclaredMethod("addWorker", Runnable.class, boolean.class);
      addWrokerMethod.setAccessible(true);

      Field field = ThreadPoolExecutor.class.getDeclaredField("ctl");
      field.setAccessible(true);
      ctl = (AtomicInteger) field.get(owner);
    } catch (Throwable e) {
      throw new IllegalStateException("failed to init queue.", e);
    }
  }

  @Override
  public boolean offer(E runnable) {
    // task can come before owner available
    if (owner == null) {
      return super.offer(runnable);
    }

    // can not create more thread, just queue the task
    if (workerCountOf(ctl.get()) == owner.getMaximumPoolSize()) {
      return super.offer(runnable);
    }
    // no need to create more thread, just queue the task
    if (owner.getNotFinished() <= workerCountOf(ctl.get())) {
      return super.offer(runnable);
    }
    // all threads are busy, and can create new thread, not queue the task
    if (workerCountOf(ctl.get()) < owner.getMaximumPoolSize()) {
      try {
        // low frequency event, reflect is no problem
        if (!(Boolean) addWrokerMethod.invoke(owner, runnable, false)) {
          // failed to create new thread, queue the task
          // if failed to queue the task, owner will try to addWorker again,
          // if still failed, the will reject the task
          return super.offer(runnable);
        }

        // create new thread successfully, treat it as queue success
        return true;
      } catch (Throwable e) {
        // reflection exception, should never happened
        return super.offer(runnable);
      }
    }

    return super.offer(runnable);
  }
}
