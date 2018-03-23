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

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestOperationPerfGroup {
  String op = "op";

  OperationPerfGroup group = new OperationPerfGroup(Const.RESTFUL, Status.OK.name());

  @Test
  public void construct() {
    Assert.assertEquals(Const.RESTFUL, group.getTransport());
    Assert.assertEquals(Status.OK.name(), group.getStatus());
    Assert.assertTrue(group.getOperationPerfs().isEmpty());
    Assert.assertNull(group.getSummary());
  }

  @Test
  public void addOperationPerf() {
    OperationPerf opPerf = Utils.createOperationPerf(op);
    group.addOperationPerf(opPerf);
    group.addOperationPerf(opPerf);

    Assert.assertThat(group.getOperationPerfs(), Matchers.contains(opPerf, opPerf));

    OperationPerf summary = group.getSummary();

    PerfInfo perfInfo = summary.findStage(MeterInvocationConst.STAGE_TOTAL);
    Assert.assertEquals(20, perfInfo.getTps());
    Assert.assertEquals(1000, perfInfo.calcMsLatency(), 0);
    Assert.assertEquals(100000, perfInfo.getMsMaxLatency(), 0);

    perfInfo = summary.findStage(MeterInvocationConst.STAGE_EXECUTION);
    Assert.assertEquals(20, perfInfo.getTps());
    Assert.assertEquals(1000, perfInfo.calcMsLatency(), 0);
    Assert.assertEquals(100000, perfInfo.getMsMaxLatency(), 0);
  }
}
