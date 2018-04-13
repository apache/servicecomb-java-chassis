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

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementTree;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.metrics.core.publish.model.ThreadPoolPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerf;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroup;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroups;
import org.apache.servicecomb.metrics.core.publish.model.invocation.PerfInfo;
import org.apache.servicecomb.metrics.core.publish.model.invocation.Utils;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.spectator.api.patterns.ThreadPoolMonitorPublishModelFactory;

public class TestPublishUtils {
  String op = "op";

  @Test
  public void createPerfInfo() {
    MeasurementNode stageNode = Utils.createStageNode(MeterInvocationConst.STAGE_TOTAL, 10, 10, 100);

    PerfInfo perf = PublishUtils.createPerfInfo(stageNode);

    Assert.assertEquals(10, perf.getTps());
    Assert.assertEquals(1000, perf.calcMsLatency(), 0);
    Assert.assertEquals(100000, perf.getMsMaxLatency(), 0);
  }

  @Test
  public void createOperationPerf() {
    OperationPerf opPerf = Utils.createOperationPerf(op);

    PerfInfo perfInfo = opPerf.findStage(MeterInvocationConst.STAGE_TOTAL);
    Assert.assertEquals(10, perfInfo.getTps());
    Assert.assertEquals(1000, perfInfo.calcMsLatency(), 0);
    Assert.assertEquals(100000, perfInfo.getMsMaxLatency(), 0);
  }

  @Test
  public void addOperationPerfGroups() {
    OperationPerfGroups groups = new OperationPerfGroups();
    PublishUtils.addOperationPerfGroups(groups,
        Const.RESTFUL,
        op,
        Utils.createStatusNode(Status.OK.name(), Utils.totalStageNode));

    Map<String, OperationPerfGroup> statusMap = groups.getGroups().get(Const.RESTFUL);
    OperationPerfGroup group = statusMap.get(Status.OK.name());

    PerfInfo perfInfo = group.getSummary().findStage(MeterInvocationConst.STAGE_TOTAL);
    Assert.assertEquals(10, perfInfo.getTps());
    Assert.assertEquals(1000, perfInfo.calcMsLatency(), 0);
    Assert.assertEquals(100000, perfInfo.getMsMaxLatency(), 0);
  }

  @Test
  public void createThreadPoolPublishModels_empty() {
    Map<String, ThreadPoolPublishModel> threadPools = new HashMap<>();

    ThreadPoolMonitorPublishModelFactory.create(new MeasurementTree(), threadPools);

    Assert.assertTrue(threadPools.isEmpty());
  }
}
