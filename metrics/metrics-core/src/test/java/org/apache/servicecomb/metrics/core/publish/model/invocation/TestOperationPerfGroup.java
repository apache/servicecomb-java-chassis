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

import jakarta.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestOperationPerfGroup {
  String op = "op";

  OperationPerfGroup group = new OperationPerfGroup(CoreConst.RESTFUL, Status.OK.name());

  @Test
  public void construct() {
    Assertions.assertEquals(CoreConst.RESTFUL, group.getTransport());
    Assertions.assertEquals(Status.OK.name(), group.getStatus());
    Assertions.assertTrue(group.getOperationPerfs().isEmpty());
    Assertions.assertNull(group.getSummary());
  }

  @Test
  public void addOperationPerf() {
    OperationPerf opPerf = Utils.createOperationPerf(op);
    group.addOperationPerf(opPerf);
    group.addOperationPerf(opPerf);

    Assertions.assertTrue(group.getOperationPerfs().contains(opPerf));

    OperationPerf summary = group.getSummary();

    PerfInfo perfInfo = summary.findStage(MeterInvocationConst.STAGE_TOTAL);
    Assertions.assertEquals(20, perfInfo.getTps(), 0);
    Assertions.assertEquals(1000, perfInfo.calcMsLatency(), 0);
    Assertions.assertEquals(100000, perfInfo.getMsMaxLatency(), 0);

    perfInfo = summary.findStage(MeterInvocationConst.STAGE_EXECUTION);
    Assertions.assertEquals(20, perfInfo.getTps(), 0);
    Assertions.assertEquals(1000, perfInfo.calcMsLatency(), 0);
    Assertions.assertEquals(100000, perfInfo.getMsMaxLatency(), 0);
  }
}
