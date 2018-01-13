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

package org.apache.servicecomb.foundation.metrics.performance;

import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPerfStatMonitorMgr {

  PerfStatMonitorMgr oPerfStatMonitorMgr = null;

  PerfStatSuccFail oPerfStatSuccFail = null;

  @Before
  public void setUp() throws Exception {
    oPerfStatMonitorMgr = new PerfStatMonitorMgr();
    oPerfStatSuccFail = new PerfStatSuccFail("testMergeFrom");
  }

  @After
  public void tearDown() throws Exception {
    oPerfStatMonitorMgr = null;
    oPerfStatSuccFail = null;
  }

  @Test
  public void testRegisterPerfStat() {
    oPerfStatMonitorMgr.registerPerfStat(oPerfStatSuccFail, 0);
    Assert.assertEquals(1, oPerfStatMonitorMgr.getMonitorList().size());
  }

  @Test
  public void testOnCycle() {
    oPerfStatMonitorMgr.registerPerfStat(oPerfStatSuccFail, 0);
    oPerfStatMonitorMgr.onCycle(System.currentTimeMillis(), 10);
    Assert.assertEquals(1, oPerfStatMonitorMgr.getMonitorPerfStat().size());
  }

  @Test
  public void testSort() {
    oPerfStatMonitorMgr.registerPerfStat(new PerfStatSuccFail("a"), -1);
    oPerfStatMonitorMgr.registerPerfStat(new PerfStatSuccFail("b"), Integer.MAX_VALUE);

    Assert.assertThat(
        oPerfStatMonitorMgr.getMonitorList().stream().map(PerfStatMonitor::getName).collect(Collectors.toList()),
        Matchers.contains("a", "b"));
  }
}
