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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolExecutorEx extends ThreadPoolExecutor {
  private AtomicInteger submittedCount = new AtomicInteger();

  private AtomicInteger finishedCount = new AtomicInteger();

  private AtomicInteger rejectedCount = new AtomicInteger();

  public ThreadPoolExecutorEx(int coreThreads, int maxThreads, int maxIdleInSecond, TimeUnit timeUnit,
      BlockingQueue<Runnable> queue, ThreadFactory threadFactory) {
    super(coreThreads, maxThreads, maxIdleInSecond, timeUnit, queue, threadFactory);
    if (queue instanceof LinkedBlockingQueueEx) {
      ((LinkedBlockingQueueEx) queue).setOwner(this);
    }
    setRejectedExecutionHandler(this::rejectedExecution);
  }

  @Override
  public void execute(Runnable command) {
    submittedCount.incrementAndGet();
    super.execute(command);
  }

  public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
    rejectedCount.incrementAndGet();
    finishedCount.incrementAndGet();

    throw new RejectedExecutionException("Task " + r.toString() +
        " rejected from " +
        e.toString());
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);
    finishedCount.incrementAndGet();
  }

  public int getNotFinished() {
    return submittedCount.get() - finishedCount.get();
  }

  public int getRejectedCount() {
    return rejectedCount.get();
  }
}
