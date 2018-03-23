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
package org.apache.servicecomb.metrics.core.publish;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementNode;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerf;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroup;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroups;
import org.apache.servicecomb.metrics.core.publish.model.invocation.PerfInfo;

import com.netflix.spectator.api.Statistic;

public final class PublishUtils {
  private PublishUtils() {
  }

  public static PerfInfo createPerfInfo(MeasurementNode stageNode) {
    PerfInfo perfInfo = new PerfInfo();

    perfInfo.setTps((int) stageNode.findChild(Statistic.count.name()).summary());
    perfInfo.setMsTotalTime(stageNode.findChild(Statistic.totalTime.name()).summary() * 1000);
    // when UT with DefaultRegistry, there is no max value
    MeasurementNode maxNode = stageNode.findChild(Statistic.max.name());
    if (maxNode != null) {
      perfInfo.setMsMaxLatency(maxNode.summary() * 1000);
    }

    return perfInfo;
  }

  public static OperationPerf createOperationPerf(String operation, MeasurementNode statusNode) {
    OperationPerf operationPerf = new OperationPerf();

    operationPerf.setOperation(operation);
    for (MeasurementNode stageNode : statusNode.getChildren().values()) {
      PerfInfo perfInfo = createPerfInfo(stageNode);
      operationPerf.getStages().put(stageNode.getName(), perfInfo);
    }

    return operationPerf;
  }

  public static void addOperationPerfGroups(OperationPerfGroups operationPerfGroups, String transport, String operation,
      MeasurementNode statusNode) {
    Map<String, OperationPerfGroup> statusMap = operationPerfGroups
        .getGroups()
        .computeIfAbsent(transport, tn -> {
          return new HashMap<>();
        });
    OperationPerfGroup group = statusMap.computeIfAbsent(statusNode.getName(), status -> {
      return new OperationPerfGroup(transport, status);
    });

    OperationPerf operationPerf = createOperationPerf(operation, statusNode);
    group.addOperationPerf(operationPerf);
  }
}
