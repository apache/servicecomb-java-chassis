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

package org.apache.servicecomb.foundation.metrics.performance;

/**
 * Used for holding the request queue related timings like start time, operation time and end time.
 */
public class QueueMetricsData {

  // after invocation polled from queue
  private Long totalTime = 0L;

  // succ and fail
  // after invocation polled from queue
  private Long totalCount = 0L;

  // after invocation finished
  private Long totalServExecutionTime = 0L;

  // after invocation finished
  private Long totalServExecutionCount = 0L;

  // inc and dec
  // addToQueue inc
  // pollFromQueue inc
  // pollFromQueue - addToQueue = countInQueue
  // after invocation polled from queue
  private Long countInQueue = 0L;

  // after invocation polled from queue
  private Long minLifeTimeInQueue = 0L;

  // after invocation polled from queue
  private Long maxLifeTimeInQueue = 0L;

  /**
   * Sets total count in queue.
   */
  public void incrementCountInQueue() {
    this.countInQueue++;
  }

  /**
   * Deletes total count in queue.
   */
  public void decrementCountInQueue() {
    this.countInQueue--;
  }

  /**
   * default constructor.
   */
  public QueueMetricsData() {

  }

  /**
   * Returns the count for calculating average count value in queue.
   * @return Long
   */
  public Long getTotalTime() {
    return totalTime;
  }

  /**
   * Sets the total time for calculating average time in queue.
   * @param totalTime total time value
   */
  public void setTotalTime(long totalTime) {
    this.totalTime = totalTime;
  }

  /**
   * Returns the total count for calculating average count value in queue.
   * @return Long
   */
  public Long getTotalCount() {
    return totalCount;
  }

  /**
   * Sets the total count for calculating average count value in queue.
   */
  public void incrementTotalCount() {
    this.totalCount++;
  }

  /**
   * Sets the total count for calculating average count value in queue.
   * @param totalCount total count
   */
  public void setTotalCount(long totalCount) {
    this.totalCount = totalCount;
  }

  /**
   * Returns the count for calculating average value of working time after queue.
   * @return Long
   */
  public Long getTotalServExecutionTime() {
    return totalServExecutionTime;
  }

  /**
   * Sets the count for calculating average value of working time after queue.
   * @param totalCountAfterQueue count value
   */
  public void setTotalServExecutionTime(long totalCountAfterQueue) {
    this.totalServExecutionTime = totalCountAfterQueue;
  }

  /**
   * Returns the total count for calculating average value of working time after queue.
   * @return Long
   */
  public Long getTotalServExecutionCount() {
    return totalServExecutionCount;
  }

  /**
   * Sets the total count for calculating average value of working time after queue. 
   */
  public void incrementTotalServExecutionCount() {
    this.totalServExecutionCount++;
  }

  /**
   * Sets the total count for calculating average value of working time after queue.
   * @param totalServExecutionCount total service execution time count
   */
  public void setTotalServExecutionCount(long totalServExecutionCount) {
    this.totalServExecutionCount = totalServExecutionCount;
  }

  /** 
   * Returns total count in queue.
   * @return Long
   */
  public Long getCountInQueue() {
    return countInQueue;
  }

  /**  
   * Returns total count in queue.
   * @param countInQueue queue count
   */
  public void setCountInQueue(long countInQueue) {
    this.countInQueue = countInQueue;
  }

  /** 
   * Returns the minimum lifetime in queue.
   * @return Long
   */
  public Long getMinLifeTimeInQueue() {
    return minLifeTimeInQueue;
  }

  /** 
   * Sets the minimum lifetime in queue.
   * @param minLifeTimeInQueue minimum lifetime
   */
  public void setMinLifeTimeInQueue(long minLifeTimeInQueue) {
    if ((this.minLifeTimeInQueue <= 0) || (minLifeTimeInQueue < this.minLifeTimeInQueue)) {
      this.minLifeTimeInQueue = minLifeTimeInQueue;
    }
  }

  /** 
   * Returns maximum lifetime in queue.
   * @return Long
   */
  public Long getMaxLifeTimeInQueue() {
    return maxLifeTimeInQueue;
  }

  /** 
   * Sets maximum lifetime in queue.
   * @param maxLifeTimeInQueue maximum lifetime
   */
  public void setMaxLifeTimeInQueue(long maxLifeTimeInQueue) {
    if ((this.maxLifeTimeInQueue <= 0) || (maxLifeTimeInQueue > this.maxLifeTimeInQueue)) {
      this.maxLifeTimeInQueue = maxLifeTimeInQueue;
    }
  }

  /** 
   * Resets minimum lifetime in queue.
   */
  public void resetMinLifeTimeInQueue() {
    this.minLifeTimeInQueue = 0L;
  }

  /** 
   * Resets maximum lifetime in queue.
   */
  public void resetMaxLifeTimeInQueue() {
    this.maxLifeTimeInQueue = 0L;
  }
}
