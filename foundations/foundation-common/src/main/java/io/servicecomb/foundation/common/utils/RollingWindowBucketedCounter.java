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
 *   In consider of performance, records are counted in buckets, and the current working bucket will not be counted
 *   in the {@link #totalCount}.<br/>
 *   i.e. If {@link #increment()} is invoked, it will be counted in {@link #currentBucket}, but this result won't be
 *   reflected in {@link #totalCount} instantly. <br/>
 *   When {@link #bucketSizeMs} milliseconds passes, the buckets are shifted,
 *   and the next bucket will act as the working bucket. The counted number in the previous working bucket will be added
 *   into {@link #totalCount}, and the number in oldest bucket will be removed from {@link #totalCount}.
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

  private AtomicLong[] buckets;

  private AtomicLong currentBucket;

  /**
   * index of current working bucket
   */
  private int cursor;

  private long shiftTime;

  private long totalCount;

  /**
   * The size of {@link #buckets} should be one more than the {@link #bucketNum} to spare a bucket for current counting.
   * i.e. the rest buckets in {@link #buckets} will be counted to {@link #totalCount}
   */
  public RollingWindowBucketedCounter(long bucketSizeMs, int bucketNum) {
    this.bucketSizeMs = bucketSizeMs;
    this.bucketNum = bucketNum;
    this.shiftTime = currentTimeMillis() + bucketSizeMs;
    this.buckets = new AtomicLong[this.bucketNum + 1];
    for (int i = 0; i < this.buckets.length; ++i) {
      this.buckets[i] = new AtomicLong(0);
    }
    this.currentBucket = this.buckets[this.cursor];
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

    List<Long> lastBuckets = new ArrayList<>();
    int offset = cursor - 1;
    for (int i = 0; i < lastCount; ++i) {
      if (offset < 0) {
        offset = buckets.length - 1;
      }

      lastBuckets.add(buckets[offset].get());
      --offset;
    }

    return lastBuckets;
  }

  public long getCurrentBucket() {
    return this.currentBucket.get();
  }

  public void increment() {
    checkAndShift();
    currentBucket.incrementAndGet();
  }

  /**
   * check if it's time to shift bucket, do shift if necessary.
   */
  private void checkAndShift() {
    if (shiftTime <= currentTimeMillis()) {
      // is shifting
      shift();
    }
  }

  long currentTimeMillis() {
    return System.currentTimeMillis();
  }

  private synchronized void shift() {
    long current = currentTimeMillis();
    if (shiftTime > current) {
      return;
    }

    long step = (current - shiftTime) / bucketSizeMs + 1;
    incrementShiftTime(step);
    if (step > buckets.length) {
      // to avoid repeated operation
      step = buckets.length;
    }

    for (int i = 0; i < step; ++i) {
      shiftCursor();
      buckets[cursor].set(0L);
    }

    currentBucket = buckets[cursor];
    refreshTotalCount();
  }

  private void incrementShiftTime(long step) {
    shiftTime += bucketSizeMs * step;
  }

  private void shiftCursor() {
    ++cursor;
    if (cursor >= buckets.length) {
      cursor = cursor % buckets.length;
    }
  }

  void refreshTotalCount() {
    long count = 0;
    for (int i = 0; i < buckets.length; ++i) {
      if (i == cursor) {
        // current working bucket will not be counted
        continue;
      }
      count += buckets[i].get();
    }

    totalCount = count;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("RollingWindowBucketedCounter{");
    sb.append("bucketSizeMs=").append(bucketSizeMs);
    sb.append(", bucketNum=").append(bucketNum);
    sb.append(", buckets=").append(Arrays.toString(buckets));
    sb.append(", currentBucket=").append(currentBucket.get());
    sb.append(", cursor=").append(cursor);
    sb.append(", shiftTime=").append(shiftTime);
    sb.append(", totalCount=").append(totalCount);
    sb.append('}');
    return sb.toString();
  }
}
