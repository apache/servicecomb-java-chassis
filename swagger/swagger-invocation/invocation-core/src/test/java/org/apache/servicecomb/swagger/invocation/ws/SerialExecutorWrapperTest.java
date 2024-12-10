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

package org.apache.servicecomb.swagger.invocation.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * SerialExecutorWrapper UT
 */
public class SerialExecutorWrapperTest {
  private ExecutorService workerPools;

  @Before
  public void before() {
    workerPools = Executors.newFixedThreadPool(20);
  }

  @After
  public void after() {
    workerPools.shutdown();
  }

  @Test
  public void executeSingleWrapper() throws InterruptedException {
    // queueCapacity(10) + bufferCapacity(50) + taskInWorking(1) = 61, this is the max task count that not trigger queue full exception
    final int taskCount = 61;
    final SerialExecutorWrapper wrapper = new SerialExecutorWrapper(InvocationType.PRODUCER,
        "testSerialWrapper", workerPools, 10, 3);
    wrapper.subscribeQueueDrainEvent(() -> {
    });
    final Object lock = new Object();
    final List<Integer> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(taskCount);
    synchronized (lock) {
      final CountDownLatch continueLoopLatch = new CountDownLatch(1);
      wrapper.execute(() -> {
        continueLoopLatch.countDown();
        synchronized (lock) {
          resultList.add(0);
          countDownLatch.countDown();
        }
      });
      continueLoopLatch.await(1, TimeUnit.MINUTES); // wait the first task start running to ensure a stable result.
      for (int i = 1; i < taskCount; ++i) {
        final int index = i;
        wrapper.execute(() -> {
          resultList.add(index);
          countDownLatch.countDown();
        });
      }
      MatcherAssert.assertThat(resultList, Matchers.empty());
    }
    countDownLatch.await(1, TimeUnit.MINUTES);
    MatcherAssert.assertThat(resultList, Matchers.hasSize(taskCount));
    for (int i = 0; i < taskCount; ++i) {
      // must execute
      MatcherAssert.assertThat(resultList.get(i), Matchers.equalTo(i));
    }
  }

  @Test
  public void executeSingleWrapperQueueFull() throws InterruptedException {
    final int taskCount = 62; // taskCount +1 than executeSingleWrapper method
    final SerialExecutorWrapper wrapper = new SerialExecutorWrapper(InvocationType.PRODUCER,
        "testSerialWrapper", workerPools, 10, 3);
    wrapper.subscribeQueueDrainEvent(() -> {
    });
    final Object lock = new Object();
    final AtomicBoolean exceptionFlag = new AtomicBoolean();
    final List<Integer> resultList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(taskCount - 1);
    synchronized (lock) {
      final CountDownLatch continueLoopLatch = new CountDownLatch(1);
      wrapper.execute(() -> {
        continueLoopLatch.countDown();
        synchronized (lock) {
          resultList.add(0);
          countDownLatch.countDown();
        }
      });
      continueLoopLatch.await(1, TimeUnit.MINUTES); // wait the first task start running to ensure a stable result.
      for (int i = 1; i < taskCount; ++i) {
        final int index = i;
        try {
          wrapper.execute(() -> {
            resultList.add(index);
            countDownLatch.countDown();
          });
        } catch (Exception e) {
          exceptionFlag.set(true);
          MatcherAssert.assertThat(i, Matchers.equalTo(taskCount - 1));
          MatcherAssert.assertThat(e, Matchers.instanceOf(IllegalStateException.class));
        }
      }
      MatcherAssert.assertThat(resultList, Matchers.empty());
    }
    countDownLatch.await(1, TimeUnit.MINUTES);
    MatcherAssert.assertThat(exceptionFlag.get(), Matchers.equalTo(true));
    MatcherAssert.assertThat(resultList, Matchers.hasSize(taskCount - 1));
    for (int i = 0; i < taskCount - 1; ++i) {
      MatcherAssert.assertThat(resultList.get(i), Matchers.equalTo(i));
    }
  }
}
