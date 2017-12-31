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

package io.servicecomb.foundation.common.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import mockit.Deencapsulation;

public class RollingWindowBucketedCounterTest {

  private static final long UNIT_TIME = 100L;

  @Test
  public void testInit() {
    RollingWindowBucketedCounter counter = new RollingWindowBucketedCounter(UNIT_TIME, 5);

    long[] buckets = Deencapsulation.getField(counter, "buckets");
    assertEquals(5, buckets.length);
    for (long bucket : buckets) {
      assertEquals(0L, bucket);
    }
  }

  @Test
  public void testGetTotalCount() {
    TestCounter counter = new TestCounter(UNIT_TIME, 5);

    counter.increment();
    // current increment will be counted in next UNIT_TIME
    assertEquals(0, counter.getTotalCount());

    for (int i = 0; i < 3; ++i) {
      counter.addTimeMillis(UNIT_TIME);
      counter.increment();
      assertEquals(i + 1, counter.getTotalCount());
    }
  }

  @Test
  public void testGetLastBuckets() {
    TestCounter counter = new TestCounter(UNIT_TIME, 5);

    for (int i = 0; i < 2; ++i) {
      counter.increment();
      counter.addTimeMillis(UNIT_TIME);
    }
    counter.addTimeMillis(UNIT_TIME * 2);

    List<Long> bucketList = counter.getLastBuckets(4);
    assertEquals(4, bucketList.size());
    assertEquals(Long.valueOf(0), bucketList.get(0));
    assertEquals(Long.valueOf(0), bucketList.get(1));
    assertEquals(Long.valueOf(1), bucketList.get(2));
    assertEquals(Long.valueOf(1), bucketList.get(3));
  }

  /**
   * CounterTime = {@link RollingWindowBucketedCounter#bucketSizeMs} * {@link RollingWindowBucketedCounter#bucketNum},
   * that means the longest time the counter can hold.
   */
  @Test
  public void testIncrementOnTimeIntervalLessThanCounterTime() {
    TestCounter counter = new TestCounter(UNIT_TIME, 5);

    for (int i = 0; i < 6; ++i) {
      counter.addTimeMillis(UNIT_TIME);
      counter.increment();
      assertEquals(i, counter.getTotalCount());
    }

    counter.addTimeMillis(3 * UNIT_TIME);
    long totalCount = counter.getTotalCount();
    assertEquals(3, totalCount);

    for (int i = 0; i < 2; ++i) {
      counter.increment();
      counter.addTimeMillis(UNIT_TIME);
    }
    assertEquals(3, counter.getTotalCount());
    assertEquals(0, ((AtomicLong) Deencapsulation.getField(counter, "workingBucket")).get());
  }

  @Test
  public void testIncrementOnTimeIntervalEqualsToCounterTime() {
    TestCounter counter = new TestCounter(UNIT_TIME, 5);
    for (int i = 0; i < 6; ++i) {
      counter.addTimeMillis(UNIT_TIME);
      counter.increment();
    }

    counter.addTimeMillis(UNIT_TIME * 5);

    assertEquals(1, counter.getTotalCount());
  }

  @Test
  public void testIncrementOnTimeIntervalMoreThanCounterTime() {
    TestCounter counter = new TestCounter(UNIT_TIME, 5);

    for (int i = 0; i < 6; ++i) {
      counter.addTimeMillis(UNIT_TIME);
      counter.increment();
      assertEquals(i, counter.getTotalCount());
    }

    counter.addTimeMillis(6 * UNIT_TIME);
    long totalCount = counter.getTotalCount();
    assertEquals(0, totalCount);

    for (int i = 0; i < 2; ++i) {
      counter.increment();
      counter.addTimeMillis(UNIT_TIME);
    }
    assertEquals(2, counter.getTotalCount());
    assertEquals(0, ((AtomicLong) Deencapsulation.getField(counter, "workingBucket")).get());
  }

  @Test
  public void concurrentTest() throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    RollingWindowBucketedCounter counter = new RollingWindowBucketedCounter(20, 512);
    CountDownLatch latch = new CountDownLatch(5);
    final Runnable task = () -> {
      for (int i = 0; i < 100; ++i) {
        try {
          Thread.sleep(1L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        counter.increment();
      }
      latch.countDown();
    };
    for (int i = 0; i < 5; ++i) {
      executorService.submit(task);
    }

    latch.await();
    // to ensure all of the increment will be counted(not in working bucket)
    Thread.sleep(30L);
    assertEquals(100 * 5, counter.getTotalCount());
  }

  /**
   * If the time is longer than the CounterTime, some of the olde bucket will be overwritten.
   */
  @Test
  public void concurrentTestOnTimeExceedsCounterTime() throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    ConcurrentTestCounter counter = new ConcurrentTestCounter(10, 8);
    CountDownLatch latch = new CountDownLatch(5);
    final Runnable task = new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 100; ++i) {
          sleep(1L);
          counter.increment();
        }
        latch.countDown();
      }

      private void sleep(long millis) {
        try {
          Thread.sleep(millis);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    };
    for (int i = 0; i < 5; ++i) {
      executorService.submit(task);
    }

    latch.await();
    Thread.sleep(30L);
    counter.getTotalCount();
  }

  /**
   * For test purpose, to avoid the uncertainty of {@link Thread#sleep(long)}
   */
  private static class TestCounter extends RollingWindowBucketedCounter {
    private long timeMillis;

    TestCounter(long bucketSizeMs, int bucketNum) {
      super(bucketSizeMs, bucketNum);
    }

    void addTimeMillis(long millis) {
      this.timeMillis += millis;
    }

    @Override
    long currentTimeMillis() {
      return timeMillis;
    }
  }

  /**
   * For concurrent test purpose
   */
  private static class ConcurrentTestCounter extends RollingWindowBucketedCounter {
    private int bucketNum;

    private List<List<Long>> bucketsSnapshots = new ArrayList<>();

    ConcurrentTestCounter(long bucketSizeMs, int bucketNum) {
      super(bucketSizeMs, bucketNum);
      this.bucketNum = bucketNum;
    }

    @Override
    protected void refreshTotalCount() {
      bucketsSnapshots.add(super.getLastBuckets(bucketNum + 2));
      super.refreshTotalCount();
      checkState();
    }

    void checkState() {
      long result = this.getTotalCount();
      List<Long> lastBuckets = this.getLastBuckets(bucketNum);
      long expected = 0;
      for (Long bucket : lastBuckets) {
        expected += bucket;
      }

      assertEquals(expected, result);
    }
  }
}
