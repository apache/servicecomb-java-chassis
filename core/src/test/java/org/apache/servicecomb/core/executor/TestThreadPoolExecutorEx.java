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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestThreadPoolExecutorEx {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestThreadPoolExecutorEx.class);

  static class TestTask implements Runnable {
    CountDownLatch notify = new CountDownLatch(1);

    Future<?> future;

    public void quit() throws ExecutionException, InterruptedException {
      notify.countDown();
      future.get();
    }

    @Override
    public void run() {
      try {
        notify.await();
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  ThreadPoolExecutorEx executorEx = new ThreadPoolExecutorEx(2, 4, 2, TimeUnit.SECONDS,
      new LinkedBlockingQueueEx(2), Executors.defaultThreadFactory());

  public TestTask submitTask() {
    TestTask task = new TestTask();
    task.future = executorEx.submit(task);
    return task;
  }

  @Test
  public void schedule() throws ExecutionException, InterruptedException {
    // init
    Assertions.assertEquals(0, executorEx.getPoolSize());
    Assertions.assertEquals(0, executorEx.getRejectedCount());
    Assertions.assertEquals(0, executorEx.getNotFinished());
    Assertions.assertEquals(0, executorEx.getQueue().size());

    // use core threads
    TestTask t1 = submitTask();
    Assertions.assertEquals(1, executorEx.getPoolSize());
    Assertions.assertEquals(0, executorEx.getRejectedCount());
    Assertions.assertEquals(1, executorEx.getNotFinished());
    Assertions.assertEquals(0, executorEx.getQueue().size());

    TestTask t2 = submitTask();
    Assertions.assertEquals(2, executorEx.getPoolSize());
    Assertions.assertEquals(0, executorEx.getRejectedCount());
    Assertions.assertEquals(2, executorEx.getNotFinished());
    Assertions.assertEquals(0, executorEx.getQueue().size());

    // extend threads
    TestTask t3 = submitTask();
    Assertions.assertEquals(3, executorEx.getPoolSize());
    Assertions.assertEquals(0, executorEx.getRejectedCount());
    Assertions.assertEquals(3, executorEx.getNotFinished());
    Assertions.assertEquals(0, executorEx.getQueue().size());

    TestTask t4 = submitTask();
    Assertions.assertEquals(4, executorEx.getPoolSize());
    Assertions.assertEquals(0, executorEx.getRejectedCount());
    Assertions.assertEquals(4, executorEx.getNotFinished());
    Assertions.assertEquals(0, executorEx.getQueue().size());

    // queue the tasks
    TestTask t5 = submitTask();
    Assertions.assertEquals(4, executorEx.getPoolSize());
    Assertions.assertEquals(0, executorEx.getRejectedCount());
    Assertions.assertEquals(5, executorEx.getNotFinished());
    Assertions.assertEquals(1, executorEx.getQueue().size());

    TestTask t6 = submitTask();
    Assertions.assertEquals(4, executorEx.getPoolSize());
    Assertions.assertEquals(0, executorEx.getRejectedCount());
    Assertions.assertEquals(6, executorEx.getNotFinished());
    Assertions.assertEquals(2, executorEx.getQueue().size());

    // reject the task
    try {
      submitTask();
    } catch (RejectedExecutionException e) {

    }
    Assertions.assertEquals(4, executorEx.getPoolSize());
    Assertions.assertEquals(1, executorEx.getRejectedCount());
    Assertions.assertEquals(6, executorEx.getNotFinished());
    Assertions.assertEquals(2, executorEx.getQueue().size());

    // t1/t2/t3 finish
    t1.quit();
    t2.quit();
    t3.quit();
    Assertions.assertEquals(4, executorEx.getPoolSize());
    Assertions.assertEquals(1, executorEx.getRejectedCount());
    waitForResult(3, executorEx::getNotFinished);
    waitForResult(0, executorEx.getQueue()::size);

    // reuse thread
    t3 = submitTask();
    Assertions.assertEquals(4, executorEx.getPoolSize());
    Assertions.assertEquals(1, executorEx.getRejectedCount());
    waitForResult(4, executorEx::getNotFinished);
    waitForResult(0, executorEx.getQueue()::size);

    t3.quit();
    t4.quit();
    t5.quit();
    t6.quit();
    waitForResult(2, executorEx::getPoolSize);
    executorEx.shutdown();
  }

  private void waitForResult(int expect, IntSupplier supplier) {
    long max = 30000;
    long waited = 0;

    for (; ; ) {
      if (waited > max) {
        throw new IllegalStateException("timed out waiting.");
      }
      int actual = supplier.getAsInt();
      if (expect == actual) {
        return;
      }

      LOGGER.info("waiting for thread result, expect:{}, actual: {}.", expect, actual);
      try {
        TimeUnit.MILLISECONDS.sleep(200);
        waited += 200;
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
