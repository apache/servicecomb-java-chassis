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

package io.servicecomb.metrics.core.model;

public class InvocationModel {

  private final String operationName;

  private final long countInQueue;

  private final double lifeTimeInQueueMax;

  private final double lifeTimeInQueueMin;

  private final double lifeTimeInQueueAverage;

  private final double executionTimeMax;

  private final double executionTimeMin;

  private final double executionTimeAverage;

  public String getOperationName() {
    return operationName;
  }

  public long getCountInQueue() {
    return countInQueue;
  }

  public double getLifeTimeInQueueMax() {
    return lifeTimeInQueueMax;
  }

  public double getLifeTimeInQueueMin() {
    return lifeTimeInQueueMin;
  }

  public double getLifeTimeInQueueAverage() {
    return lifeTimeInQueueAverage;
  }

  public double getExecutionTimeMax() {
    return executionTimeMax;
  }

  public double getExecutionTimeMin() {
    return executionTimeMin;
  }

  public double getExecutionTimeAverage() {
    return executionTimeAverage;
  }

  public InvocationModel(String operationName) {
    this(operationName, 0, 0, 0, 0, 0, 0, 0);
  }

  public InvocationModel(String operationName, long countInQueue,
      double lifeTimeInQueueMax, double lifeTimeInQueueMin,
      double lifeTimeInQueueAverage, double executionTimeMax,
      double executionTimeMin, double executionTimeAverage) {
    this.operationName = operationName;
    this.countInQueue = countInQueue;
    this.lifeTimeInQueueMax = lifeTimeInQueueMax;
    this.lifeTimeInQueueMin = lifeTimeInQueueMin;
    this.lifeTimeInQueueAverage = lifeTimeInQueueAverage;
    this.executionTimeMax = executionTimeMax;
    this.executionTimeMin = executionTimeMin;
    this.executionTimeAverage = executionTimeAverage;
  }
}
