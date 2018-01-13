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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Used to store invocation specific metrics for queue.
 */
public class QueueMetrics {

  private AtomicLong queueStartTime = new AtomicLong();

  private AtomicLong endOperTime = new AtomicLong();

  private AtomicLong queueEndTime = new AtomicLong();

  private String operQualifiedName;

  /**
   * Returns the time when it enters the queue.
   * @return long
   */
  public long getQueueStartTime() {
    return queueStartTime.get();
  }

  /**
   * Sets the time when it enters the queue.
   * @param startTime Entering time in queue
   */
  public void setQueueStartTime(long startTime) {
    this.queueStartTime.set(startTime);
  }

  /**
   * Returns the time when the operation ends.
   * @return long
   */
  public long getEndOperTime() {
    return endOperTime.get();
  }

  /**
   * Sets the time when the operation ends.
   * @param stopOperTime Start time of operation
   */
  public void setEndOperTime(long stopOperTime) {
    this.endOperTime.set(stopOperTime);
  }

  /**
   * Returns the time when it leaves the queue.
   * @return long
   */
  public long getQueueEndTime() {
    return queueEndTime.get();
  }

  /**
   * Sets the time when it leaves the queue.
   * @param endTime Leaving time from queue
   */
  public void setQueueEndTime(long endTime) {
    this.queueEndTime.set(endTime);
  }

  /**
   * Get the microservice qualified name.
   * @return microservice qualified name
   */
  public String getOperQualifiedName() {
    return operQualifiedName;
  }

  /**
   * Set the microservice qualified name.
   * @param operQualifiedName microservice qualified name
   */
  public void setOperQualifiedName(String operQualifiedName) {
    this.operQualifiedName = operQualifiedName;
  }
}
