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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OperationPerf {
  private String operation;

  private Map<String, PerfInfo> stages = new HashMap<>();

  private List<Integer> latencyDistribution = new ArrayList<>();

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public Map<String, PerfInfo> getStages() {
    return stages;
  }

  public List<Integer> getLatencyDistribution() {
    return latencyDistribution;
  }

  public void setLatencyDistribution(List<Integer> latencyDistribution) {
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
    if (latencyDistribution.size() <= operationPerf.latencyDistribution.size()) {
      latencyDistribution = IntStream.range(0, operationPerf.latencyDistribution.size())
          .map(i -> {
            if (latencyDistribution.size() > i) {
              return latencyDistribution.get(i) + operationPerf.latencyDistribution.get(i);
            }
            return operationPerf.latencyDistribution.get(i);
          })
          .boxed()
          .collect(Collectors.toList());
      return;
    }
    latencyDistribution = IntStream.range(0, latencyDistribution.size())
        .map(i -> {
          if (operationPerf.latencyDistribution.size() > i) {
            return latencyDistribution.get(i) + operationPerf.latencyDistribution.get(i);
          }
          return latencyDistribution.get(i);
        })
        .boxed()
        .collect(Collectors.toList());
  }
}
