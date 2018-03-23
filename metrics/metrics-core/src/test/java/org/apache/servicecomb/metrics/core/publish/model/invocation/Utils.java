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

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementNode;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.metrics.core.publish.PublishUtils;

import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Statistic;

public class Utils {
  static Registry registry = new DefaultRegistry();

  public static MeasurementNode totalStageNode = Utils.createStageNode(MeterInvocationConst.STAGE_TOTAL, 10, 10, 100);

  public static MeasurementNode executeStageNode =
      Utils.createStageNode(MeterInvocationConst.STAGE_EXECUTION, 10, 10, 100);

  public static MeasurementNode createStageNode(String stage,
      double count,
      double totalTime,
      double max) {
    Id id = registry.createId("id").withTag(Statistic.count);
    Measurement countMeasurement = new Measurement(id.withTag(Statistic.count), 0, count);
    Measurement totalTimeMeasurement = new Measurement(id.withTag(Statistic.totalTime), 0, totalTime);
    Measurement maxMeasurement = new Measurement(id.withTag(Statistic.max), 0, max);

    MeasurementNode stageNode = new MeasurementNode(stage, null);
    stageNode.addChild(Statistic.count.name(), countMeasurement);
    stageNode.addChild(Statistic.totalTime.name(), totalTimeMeasurement);
    stageNode.addChild(Statistic.max.name(), maxMeasurement);

    return stageNode;
  }

  public static MeasurementNode createStatusNode(String status, MeasurementNode... stageNodes) {
    MeasurementNode statusNode = new MeasurementNode(status, new HashMap<>());

    for (MeasurementNode stageNode : stageNodes) {
      statusNode.getChildren().put(stageNode.getName(), stageNode);
    }
    return statusNode;
  }

  public static OperationPerf createOperationPerf(String op) {
    MeasurementNode statusNode = createStatusNode(Status.OK.name(), totalStageNode, executeStageNode);
    return PublishUtils.createOperationPerf(op, statusNode);
  }
}
