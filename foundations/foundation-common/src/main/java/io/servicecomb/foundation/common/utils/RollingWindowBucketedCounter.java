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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread-safe time-windowed rolling counter.
 * <p>
 *   In consideration of performance, records are stored in {@link #buckets}, and the {@link #workingBucket} will not be
 *   counted in the {@link #totalCount}.<br/>
 *   i.e. If {@link #increment()} is invoked, it will be counted in the {@link #workingBucket}, but this result won't be
 *   reflected in {@link #totalCount} instantly. <br/>
 *   When {@link #bucketSizeMs} milliseconds passes, the cursor is shifted, and the value in the {@link #workingBucket}
 *   will be copied into {@link #buckets}, the oldest value will be overwritten. Then the {@link #workingBucket} will be
 *   reset to zero. All of the value in {@link #buckets} will be added into {@link #totalCount}.
 * </p>
 */
public class RollingWindowBucketedCounter {
  /**
   * how much time a bucket holds, in millisecond
   */
  private long bucketSizeMs;

  /**
   * how many buckets will be counted
   */
  private int bucketNum;

  private long[] buckets;

  private AtomicLong workingBucket;

  /**
   * index of the next written bucket
   */
  private int cursor;

  private long shiftTime;

  private long totalCount;

  public RollingWindowBucketedCounter(long bucketSizeMs, int bucketNum) {
    this.bucketSizeMs = bucketSizeMs;
    this.bucketNum = bucketNum;
    this.shiftTime = currentTimeMillis() + bucketSizeMs;
    this.buckets = new long[this.bucketNum];
    this.workingBucket = new AtomicLong();
  }

  public long getTotalCount() {
    checkAndShift();
    return totalCount;
  }

  /**
   * return the last {@code lastCount} buckets as a list in descending order by time.
   * <p>
   *   the first element in the list is the most recent bucket
   * </p>
   * @param lastCount how many buckets to list
   * @return a list of buckets
   */
  public List<Long> getLastBuckets(int lastCount) {
    checkAndShift();

    if (lastCount > bucketNum) {
      lastCount = bucketNum;
    }

    List<Long> lastBuckets = new ArrayList<>(lastCount);
    int offset = cursor - 1;
    for (int i = 0; i < lastCount; ++i) {
      if (offset < 0) {
        offset = buckets.length - 1;
      }

      lastBuckets.add(buckets[offset]);
      --offset;
    }

    return lastBuckets;
  }

  public long getCurrentBucket() {
    return this.workingBucket.get();
  }

  public void increment() {
    checkAndShift();
    workingBucket.incrementAndGet();
  }

  /**
   * check if it's time to shift bucket, do shift if necessary.
   */
  private void checkAndShift() {
    if (shiftTime <= currentTimeMillis()) {
      shift();
    }
  }

  long currentTimeMillis() {
    return System.currentTimeMillis();
  }

  private synchronized void shift() {
    long current = currentTimeMillis();
    if (shiftTime > current) {
      // has been shifted
      return;
    }

    long step = (current - shiftTime) / bucketSizeMs + 1;
    incrementShiftTime(step);
    buckets[cursor] = workingBucket.getAndSet(0L);
    shiftCursor();

    // if this counter is not invoked for more than bucketSizeMs milliseconds, additional cursor shift is needed.
    if (step > 1) {
      --step;
      if (step > buckets.length) {
        // to avoid repeated operation
        step = buckets.length;
      }
      for (int i = 0; i < step; ++i) {
        buckets[cursor] = 0L;
        shiftCursor();
      }
    }

    refreshTotalCount();
  }

  private void incrementShiftTime(long step) {
    shiftTime += bucketSizeMs * step;
  }

  private void shiftCursor() {
    ++cursor;
    if (cursor >= buckets.length) {
      cursor = 0;
    }
  }

  void refreshTotalCount() {
    long count = 0;
    for (int i = 0; i < buckets.length; ++i) {
      count += buckets[i];
    }

    totalCount = count;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("RollingWindowBucketedCounter{");
    sb.append("bucketSizeMs=").append(bucketSizeMs);
    sb.append(", bucketNum=").append(bucketNum);
    sb.append(", buckets=").append(Arrays.toString(buckets));
    sb.append(", workingBucket=").append(workingBucket);
    sb.append(", cursor=").append(cursor);
    sb.append(", shiftTime=").append(shiftTime);
    sb.append(", totalCount=").append(totalCount);
    sb.append('}');
    return sb.toString();
  }
}
