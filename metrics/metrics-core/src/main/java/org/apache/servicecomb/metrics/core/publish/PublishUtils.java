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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementNode;
import org.apache.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerf;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroup;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroups;
import org.apache.servicecomb.metrics.core.publish.model.invocation.PerfInfo;
import org.apache.servicecomb.swagger.invocation.Response;

import com.netflix.spectator.api.Statistic;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public final class PublishUtils {
  public static final String EMPTY_RESULT = "-";

  private PublishUtils() {
  }

  public static PerfInfo createPerfInfo(MeasurementNode stageNode) {
    PerfInfo perfInfo = new PerfInfo();
    perfInfo.setTps(stageNode.findChild(Statistic.count.name()).summary());
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
    MeasurementNode stageNode = statusNode.findChild(MeterInvocationConst.TAG_STAGE);
    stageNode.getChildren().values().forEach(mNode -> {
      PerfInfo perfInfo = createPerfInfo(mNode);
      operationPerf.getStages().put(mNode.getName(), perfInfo);
    });

    MeasurementNode latencyNode = statusNode.findChild(MeterInvocationConst.TAG_LATENCY_DISTRIBUTION);
    if (latencyNode != null && latencyNode.getMeasurements() != null) {
      operationPerf.setLatencyDistribution(latencyNode.getMeasurements().stream()
          .map(m -> (int) m.value())
          .toArray(Integer[]::new));
    }
    return operationPerf;
  }

  public static void addOperationPerfGroups(OperationPerfGroups operationPerfGroups, String transport, String operation,
      MeasurementNode statusNode) {
    Map<String, OperationPerfGroup> statusMap = operationPerfGroups
        .getGroups()
        .computeIfAbsent(transport, tn -> new HashMap<>());
    OperationPerfGroup group = statusMap
        .computeIfAbsent(statusNode.getName(), status -> new OperationPerfGroup(transport, status));
    OperationPerf operationPerf = createOperationPerf(operation, statusNode);
    group.addOperationPerf(operationPerf);
  }

  public static String refactorStatus(Invocation invocation, Response response) {
    if (invocation.getRequestEx() instanceof VertxServerRequestToHttpServletRequest) {
      RoutingContext context = ((VertxServerRequestToHttpServletRequest) invocation.getRequestEx()).getContext();
      if (context == null) {
        return String.valueOf(response.getStatusCode());
      }
      HttpServerResponse contextResponse = context.response();
      if (response.getStatusCode() == 200 && contextResponse.closed() && !contextResponse.ended()) {
        return EMPTY_RESULT;
      }
    }
    return String.valueOf(response.getStatusCode());
  }
}
