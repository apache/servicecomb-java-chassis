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

import java.util.Map;

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementTree;
import org.apache.servicecomb.metrics.core.meter.ThreadPoolMonitorPublishModelFactory;
import org.apache.servicecomb.metrics.core.publish.model.ThreadPoolPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerf;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroup;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroups;
import org.apache.servicecomb.metrics.core.publish.model.invocation.PerfInfo;
import org.apache.servicecomb.metrics.core.publish.model.invocation.Utils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.ws.rs.core.Response.Status;

public class TestPublishUtils {
  String op = "op";

  @Test
  public void createPerfInfo() {
    MeasurementNode stageNode = Utils.createStageNode(InvocationStageTrace.STAGE_TOTAL, 10, 10, 100);

    PerfInfo perf = PublishUtils.createPerfInfo(stageNode);

    Assertions.assertEquals(10, perf.getTotalRequests(), 0);
    Assertions.assertEquals(1, perf.calcMsLatency(), 0);
    Assertions.assertEquals(100, perf.getMsMaxLatency(), 0);
  }

  @Test
  public void createOperationPerf() {
    OperationPerf opPerf = Utils.createOperationPerf(op);

    PerfInfo perfInfo = opPerf.findStage(InvocationStageTrace.STAGE_TOTAL);
    Integer[] latencyDistribution = opPerf.getLatencyDistribution();
    Assertions.assertEquals(10, perfInfo.getTotalRequests(), 0);
    Assertions.assertEquals(1, perfInfo.calcMsLatency(), 0);
    Assertions.assertEquals(100, perfInfo.getMsMaxLatency(), 0);
    Assertions.assertEquals(2, latencyDistribution.length);
    Assertions.assertEquals(1, latencyDistribution[0].intValue());
    Assertions.assertEquals(2, latencyDistribution[1].intValue());
  }

  @Test
  public void addOperationPerfGroups() {
    OperationPerfGroups groups = new OperationPerfGroups();
    PublishUtils.addOperationPerfGroups(groups,
        CoreConst.RESTFUL,
        op,
        Utils.createStatusNode(Status.OK.name(), Utils.totalStageNode));

    Map<String, OperationPerfGroup> statusMap = groups.getGroups().get(CoreConst.RESTFUL);
    OperationPerfGroup group = statusMap.get(Status.OK.name());

    PerfInfo perfInfo = group.getSummary().findStage(InvocationStageTrace.STAGE_TOTAL);
    Integer[] latencyDistribution = group.getSummary().getLatencyDistribution();
    Assertions.assertEquals(10, perfInfo.getTotalRequests(), 0);
    Assertions.assertEquals(1, perfInfo.calcMsLatency(), 0);
    Assertions.assertEquals(100, perfInfo.getMsMaxLatency(), 0);
    Assertions.assertEquals(2, latencyDistribution.length);
    Assertions.assertEquals(1, latencyDistribution[0].intValue());
    Assertions.assertEquals(2, latencyDistribution[1].intValue());
  }

  @Test
  public void createThreadPoolPublishModels_empty() {
    Map<String, ThreadPoolPublishModel> threadPools =
        ThreadPoolMonitorPublishModelFactory.create(new MeasurementTree());

    Assertions.assertTrue(threadPools.isEmpty());
  }
}
