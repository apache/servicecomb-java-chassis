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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class TestThreadPoolExecutorEx {
  static class TestTask implements Runnable {
    CountDownLatch notify = new CountDownLatch(1);

    CountDownLatch wait = new CountDownLatch(1);

    public void quit() {
      notify.countDown();
      try {
        wait.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void run() {
      try {
        notify.await();
        wait.countDown();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  ThreadPoolExecutorEx executorEx = new ThreadPoolExecutorEx(2, 4, 60, TimeUnit.SECONDS,
      new LinkedBlockingQueueEx<>(2));

  public TestTask submitTask() {
    TestTask task = new TestTask();
    executorEx.execute(task);
    return task;
  }

  @Test
  public void schedule() {
    // init
    Assert.assertEquals(0, executorEx.getPoolSize());
    Assert.assertEquals(0, executorEx.getRejectedCount());
    Assert.assertEquals(0, executorEx.getNotFinished());
    Assert.assertEquals(0, executorEx.getQueue().size());

    // use core threads
    TestTask t1 = submitTask();
    Assert.assertEquals(1, executorEx.getPoolSize());
    Assert.assertEquals(0, executorEx.getRejectedCount());
    Assert.assertEquals(1, executorEx.getNotFinished());
    Assert.assertEquals(0, executorEx.getQueue().size());

    TestTask t2 = submitTask();
    Assert.assertEquals(2, executorEx.getPoolSize());
    Assert.assertEquals(0, executorEx.getRejectedCount());
    Assert.assertEquals(2, executorEx.getNotFinished());
    Assert.assertEquals(0, executorEx.getQueue().size());

    // extend threads
    TestTask t3 = submitTask();
    Assert.assertEquals(3, executorEx.getPoolSize());
    Assert.assertEquals(0, executorEx.getRejectedCount());
    Assert.assertEquals(3, executorEx.getNotFinished());
    Assert.assertEquals(0, executorEx.getQueue().size());

    TestTask t4 = submitTask();
    Assert.assertEquals(4, executorEx.getPoolSize());
    Assert.assertEquals(0, executorEx.getRejectedCount());
    Assert.assertEquals(4, executorEx.getNotFinished());
    Assert.assertEquals(0, executorEx.getQueue().size());

    // queue the tasks
    TestTask t5 = submitTask();
    Assert.assertEquals(4, executorEx.getPoolSize());
    Assert.assertEquals(0, executorEx.getRejectedCount());
    Assert.assertEquals(5, executorEx.getNotFinished());
    Assert.assertEquals(1, executorEx.getQueue().size());

    TestTask t6 = submitTask();
    Assert.assertEquals(4, executorEx.getPoolSize());
    Assert.assertEquals(0, executorEx.getRejectedCount());
    Assert.assertEquals(6, executorEx.getNotFinished());
    Assert.assertEquals(2, executorEx.getQueue().size());

    // reject the task
    try {
      submitTask();
    } catch (RejectedExecutionException e) {

    }
    Assert.assertEquals(4, executorEx.getPoolSize());
    Assert.assertEquals(1, executorEx.getRejectedCount());
    Assert.assertEquals(6, executorEx.getNotFinished());
    Assert.assertEquals(2, executorEx.getQueue().size());

    // t1/t2/t3 finish
    t1.quit();
    t2.quit();
    t3.quit();
    Assert.assertEquals(4, executorEx.getPoolSize());
    Assert.assertEquals(1, executorEx.getRejectedCount());
    Assert.assertEquals(3, executorEx.getNotFinished());
    // multi thread, not sure
    // Assert.assertEquals(0, executorEx.getQueue().size());

    // reuse thread
    t3 = submitTask();
    Assert.assertEquals(4, executorEx.getPoolSize());
    Assert.assertEquals(1, executorEx.getRejectedCount());
    Assert.assertEquals(4, executorEx.getNotFinished());
    // multi thread, not sure
    // Assert.assertEquals(1, executorEx.getQueue().size());

    t3.quit();
    t4.quit();
    t5.quit();
    t6.quit();
    executorEx.shutdown();
  }
}
