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

public class InvocationLevelMetricsModel extends AbstractMetricsModel {

  private final String operationName;

  public String getOperationName() {
    return operationName;
  }

  public InvocationLevelMetricsModel(String operationName) {
    this(operationName, 0, 0, 0, 0, 0, 0, 0);
  }

  public InvocationLevelMetricsModel(String operationName, long countInQueue,
      double lifeTimeInQueueMax, double lifeTimeInQueueMin,
      double lifeTimeInQueueAverage, double executionTimeMax,
      double executionTimeMin, double executionTimeAverage) {
    super(countInQueue, lifeTimeInQueueMax, lifeTimeInQueueMin, lifeTimeInQueueAverage, executionTimeMax,
        executionTimeMin, executionTimeAverage);
    this.operationName = operationName;
  }
}
