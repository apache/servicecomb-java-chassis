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

package io.servicecomb.foundation.metrics.event;

public class OperationFinishedEvent implements MetricsEvent {
  private final String operationName;

  private final long finishedTime;

  private final long processElapsedTime;

  private final long totalElapsedTime;

  private final String operationType;

  public String getOperationName() {
    return operationName;
  }

  public OperationFinishedEvent(String operationName, String operationType, long finishedTime, long processElapsedTime,
      long totalElapsedTime) {
    this.operationName = operationName;
    this.operationType = operationType;
    this.finishedTime = finishedTime;
    this.processElapsedTime = processElapsedTime;
    this.totalElapsedTime = totalElapsedTime;
  }
}
