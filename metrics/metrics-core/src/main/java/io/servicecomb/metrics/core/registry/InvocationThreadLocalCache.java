/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.core.registry;

import io.servicecomb.swagger.invocation.InvocationType;

public class InvocationThreadLocalCache {

  private final String operationName;

  private long countInQueue = 0;

  private long providerCallCount = 0;

  private long consumerCallCount = 0;

  private double lifeTimeInQueue = 0;

  private double lifeTimeInQueueMax = 0;

  private double lifeTimeInQueueMin = 0;

  private double executionTime = 0;

  private double executionTimeMax = 0;

  private double executionTimeMin = 0;

  public String getOperationName() {
    return operationName;
  }

  public long getCountInQueue() {
    return countInQueue;
  }

  public long getProviderCallCount() {
    return providerCallCount;
  }

  public long getConsumerCallCount() {
    return consumerCallCount;
  }

  public double getLifeTimeInQueue() {
    return lifeTimeInQueue;
  }

  public double getLifeTimeInQueueMax() {
    return lifeTimeInQueueMax;
  }

  public double getLifeTimeInQueueMin() {
    return lifeTimeInQueueMin;
  }

  public double getExecutionTime() {
    return executionTime;
  }

  public double getExecutionTimeMax() {
    return executionTimeMax;
  }

  public double getExecutionTimeMin() {
    return executionTimeMin;
  }

  public void increaseLifeTimeInQueue(double value) {
    lifeTimeInQueue += value;
    if (isValidMin(lifeTimeInQueueMin, value)) {
      lifeTimeInQueueMin = value;
    }
    if (isValidMax(lifeTimeInQueueMax, value)) {
      lifeTimeInQueueMax = value;
    }
  }

  public void increaseExecutionTime(double value) {
    executionTime += value;
    if (isValidMin(executionTimeMin, value)) {
      executionTimeMin = value;
    }
    if (isValidMax(executionTimeMax, value)) {
      executionTimeMax = value;
    }
  }

  public void increaseCountInQueue() {
    countInQueue++;
  }

  public void decreaseCountInQueue() {
    countInQueue--;
  }

  public void increaseCallCount(InvocationType type) {
    if (InvocationType.CONSUMER.equals(type)) {
      consumerCallCount++;
    } else {
      providerCallCount++;
    }
  }

  public InvocationThreadLocalCache(String operationName) {
    this.operationName = operationName;
  }

  public InvocationThreadLocalCache(String operationName, long countInQueue, long providerCallCount, long consumerCallCount,
      double lifeTimeInQueue, double lifeTimeInQueueMax, double lifeTimeInQueueMin, double executionTime,
      double executionTimeMax, double executionTimeMin) {
    this.operationName = operationName;
    this.countInQueue = countInQueue;
    this.providerCallCount = providerCallCount;
    this.consumerCallCount = consumerCallCount;
    this.lifeTimeInQueue = lifeTimeInQueue;
    this.lifeTimeInQueueMax = lifeTimeInQueueMax;
    this.lifeTimeInQueueMin = lifeTimeInQueueMin;
    this.executionTime = executionTime;
    this.executionTimeMax = executionTimeMax;
    this.executionTimeMin = executionTimeMin;
  }

  public void merge(InvocationThreadLocalCache localCache) {
    countInQueue += localCache.countInQueue;
    providerCallCount += localCache.providerCallCount;
    consumerCallCount += localCache.consumerCallCount;
    lifeTimeInQueue += localCache.lifeTimeInQueue;
    executionTime += localCache.executionTime;

    if (isValidMin(lifeTimeInQueueMin, localCache.lifeTimeInQueueMin)) {
      lifeTimeInQueueMin = localCache.lifeTimeInQueueMin;
    }
    if (isValidMax(lifeTimeInQueueMax, localCache.lifeTimeInQueueMax)) {
      lifeTimeInQueueMax = localCache.lifeTimeInQueueMax;
    }
    if (isValidMin(executionTimeMin, localCache.executionTimeMin)) {
      executionTimeMin = localCache.executionTimeMin;
    }
    if (isValidMax(executionTimeMax, localCache.executionTimeMax)) {
      executionTimeMax = localCache.executionTimeMax;
    }
  }

  public InvocationThreadLocalCache collect() {
    InvocationThreadLocalCache cloneCache = new InvocationThreadLocalCache(operationName, countInQueue, providerCallCount,
        consumerCallCount,
        lifeTimeInQueue, lifeTimeInQueueMax, lifeTimeInQueueMin, executionTime, executionTimeMax, executionTimeMin);
    //reset max and min after collected (stat runner polled)
    lifeTimeInQueueMax = 0;
    lifeTimeInQueueMin = 0;
    executionTimeMax = 0;
    executionTimeMin = 0;
    return cloneCache;
  }

  private boolean isValidMax(double source, double value) {
    return source < value && value != 0;
  }

  private boolean isValidMin(double source, double value) {
    return source == 0 || (source > value && value != 0);
  }
}
