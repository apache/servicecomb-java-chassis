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
package org.apache.servicecomb.metrics.core.publish.model.invocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OperationPerf {
  private String operation;

  private Map<String, PerfInfo> stages = new HashMap<>();

  private Integer[] latencyDistribution;

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public Map<String, PerfInfo> getStages() {
    return stages;
  }

  public Integer[] getLatencyDistribution() {
    return latencyDistribution;
  }

  public void setLatencyDistribution(Integer[] latencyDistribution) {
    this.latencyDistribution = latencyDistribution;
  }

  public void setStages(Map<String, PerfInfo> stages) {
    this.stages = stages;
  }

  public PerfInfo findStage(String stage) {
    return stages.get(stage);
  }

  public void add(OperationPerf operationPerf) {
    operationPerf.stages.forEach((key, value) -> {
      PerfInfo perfInfo = stages.computeIfAbsent(key, n -> new PerfInfo());
      perfInfo.add(value);
    });

    if (operationPerf.getLatencyDistribution() == null) {
      return;
    }

    if (latencyDistribution == null) {
      latencyDistribution = new Integer[operationPerf.getLatencyDistribution().length];
      Arrays.fill(latencyDistribution, 0);
    }
    for (int idx = 0; idx < operationPerf.getLatencyDistribution().length; idx++) {
      latencyDistribution[idx] += operationPerf.getLatencyDistribution()[idx];
    }
  }
}
