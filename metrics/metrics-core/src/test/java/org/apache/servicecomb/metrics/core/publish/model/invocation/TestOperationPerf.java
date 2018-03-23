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

import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.junit.Assert;
import org.junit.Test;

public class TestOperationPerf {
  String op = "op";

  OperationPerf opPerf = new OperationPerf();

  @Test
  public void add() {
    Assert.assertTrue(opPerf.getStages().isEmpty());

    OperationPerf otherOpPerf = Utils.createOperationPerf(op);
    opPerf.add(otherOpPerf);

    Assert.assertEquals(op, otherOpPerf.getOperation());

    PerfInfo perfInfo = opPerf.findStage(MeterInvocationConst.STAGE_TOTAL);
    Assert.assertEquals(10, perfInfo.getTps());
    Assert.assertEquals(1000, perfInfo.calcMsLatency(), 0);
    Assert.assertEquals(100000, perfInfo.getMsMaxLatency(), 0);

    perfInfo = opPerf.findStage(MeterInvocationConst.STAGE_EXECUTION);
    Assert.assertEquals(10, perfInfo.getTps());
    Assert.assertEquals(1000, perfInfo.calcMsLatency(), 0);
    Assert.assertEquals(100000, perfInfo.getMsMaxLatency(), 0);
  }
}
