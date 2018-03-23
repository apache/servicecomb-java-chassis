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

import org.junit.Assert;
import org.junit.Test;

public class TestPerfInfo {
  @Test
  public void construct() {
    PerfInfo perf = new PerfInfo();

    Assert.assertEquals(0, perf.getTps());
    Assert.assertEquals(0, perf.getMsTotalTime(), 0);
    Assert.assertEquals(0, perf.getMsMaxLatency(), 0);
    Assert.assertEquals(0, perf.calcMsLatency(), 0);
  }

  @Test
  public void add_changeMax() {
    PerfInfo sum = new PerfInfo();

    PerfInfo other = new PerfInfo();
    other.setTps(10);
    other.setMsTotalTime(10);
    other.setMsMaxLatency(100);
    sum.add(other);

    other = new PerfInfo();
    other.setTps(20);
    other.setMsTotalTime(20);
    other.setMsMaxLatency(200);
    sum.add(other);

    Assert.assertEquals(30, sum.getTps());
    Assert.assertEquals(30, sum.getMsTotalTime(), 0);
    Assert.assertEquals(200, sum.getMsMaxLatency(), 0);
    Assert.assertEquals(1.0, sum.calcMsLatency(), 0);
  }

  @Test
  public void add_notChangeMax() {
    PerfInfo sum = new PerfInfo();

    PerfInfo other = new PerfInfo();
    other.setTps(10);
    other.setMsTotalTime(10);
    other.setMsMaxLatency(100);
    sum.add(other);

    other = new PerfInfo();
    other.setTps(20);
    other.setMsTotalTime(20);
    other.setMsMaxLatency(50);
    sum.add(other);

    Assert.assertEquals(30, sum.getTps());
    Assert.assertEquals(1.0, sum.calcMsLatency(), 0);
    Assert.assertEquals(100, sum.getMsMaxLatency(), 0);
  }

  @Test
  public void testToString() {
    PerfInfo perf = new PerfInfo();
    perf.setTps(10);
    perf.setMsTotalTime(10);
    perf.setMsMaxLatency(100);

    Assert.assertEquals("PerfInfo [tps=10, msTotalTime=10.0, msLatency=1.0, msMaxLatency=100.0]", perf.toString());
  }
}
