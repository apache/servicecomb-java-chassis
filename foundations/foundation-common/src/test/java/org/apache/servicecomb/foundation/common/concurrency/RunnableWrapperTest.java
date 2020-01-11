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

package org.apache.servicecomb.foundation.common.concurrency;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class RunnableWrapperTest {
  /**
   * {@link SuppressedRunnableWrapper} should ensure that any {@link Throwable} thrown from {@link Runnable}
   * should not interrupt the scheduled tasks.
   */
  @Test
  public void run() {
    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    CountDownLatch countDownLatch = new CountDownLatch(3);

    try {
      scheduledThreadPoolExecutor.scheduleAtFixedRate(
          new SuppressedRunnableWrapper(
              () -> {
                countDownLatch.countDown();
                switch ((int) countDownLatch.getCount()) {
                  case 2:
                    throw new Error("Normal case, this is a mocked error");
                  case 1:
                    throw new IllegalStateException("Normal case, this is a mocked exception");
                  default:
                }
              }
          ),
          1,
          1,
          TimeUnit.MILLISECONDS);

      countDownLatch.await(1, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      System.out.println("get an InterruptedException! " + e.getMessage());
      e.printStackTrace();
    } finally {
      scheduledThreadPoolExecutor.shutdownNow();
    }

    assertEquals(0, countDownLatch.getCount());
  }
}
