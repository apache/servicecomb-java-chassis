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

import java.util.HashMap;
import java.util.List;

import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementNode;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.metrics.core.publish.PublishUtils;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tags;
import jakarta.ws.rs.core.Response.Status;

public class Utils {
  public static MeasurementNode totalStageNode = Utils.createStageNode(InvocationStageTrace.STAGE_TOTAL, 10, 10, 100);

  public static MeasurementNode executeStageNode =
      Utils.createStageNode(InvocationStageTrace.STAGE_PROVIDER_BUSINESS, 10, 10, 100);

  public static Id initId = new Id("id", Tags.empty(), null, null, Type.OTHER);

  public static MeasurementNode createStageNode(String stage,
      double count,
      double totalTime,
      double max) {
    Id id = initId;
    Measurement countMeasurement = new Measurement(() -> count, Statistic.COUNT);
    Measurement totalTimeMeasurement = new Measurement(() -> totalTime, Statistic.TOTAL_TIME);
    Measurement maxMeasurement = new Measurement(() -> max, Statistic.MAX);

    MeasurementNode stageNode = new MeasurementNode(stage, id, null);
    stageNode.addChild(Statistic.COUNT.name(), id, countMeasurement);
    stageNode.addChild(Statistic.TOTAL_TIME.name(), id, totalTimeMeasurement);
    stageNode.addChild(Statistic.MAX.name(), id, maxMeasurement);

    return stageNode;
  }

  public static MeasurementNode createStatusNode(String status, MeasurementNode... stageNodes) {
    Id id = initId;
    MeasurementNode statusNode = new MeasurementNode(status, id, new HashMap<>());
    MeasurementNode typeNode = new MeasurementNode(MeterInvocationConst.TAG_STAGE, id, new HashMap<>());
    MeasurementNode latencyNode = new MeasurementNode(MeterInvocationConst.TAG_LATENCY_DISTRIBUTION, id,
        new HashMap<>());
    List<Measurement> measurements = latencyNode.getMeasurements();
    measurements.add(new Measurement(() -> 1, Statistic.VALUE));
    measurements.add(new Measurement(() -> 2, Statistic.VALUE));
    for (MeasurementNode stageNode : stageNodes) {
      typeNode.getChildren().put(stageNode.getName(), stageNode);
    }
    statusNode.getChildren().put(latencyNode.getName(), latencyNode);
    statusNode.getChildren().put(typeNode.getName(), typeNode);
    return statusNode;
  }

  public static OperationPerf createOperationPerf(String op) {
    MeasurementNode statusNode = createStatusNode(Status.OK.name(), totalStageNode, executeStageNode);
    return PublishUtils.createOperationPerf(op, statusNode);
  }
}
