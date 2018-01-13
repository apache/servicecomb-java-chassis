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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPerfStatMonitor {

  PerfStatMonitor oPerfStatMonitor = null;

  PerfStatSuccFail oPerfStatSuccFail = null;

  @Before
  public void setUp() throws Exception {
    oPerfStatMonitor = new PerfStatMonitor("testMonitor", 0);
    oPerfStatSuccFail = new PerfStatSuccFail("testMergeFrom");
  }

  @After
  public void tearDown() throws Exception {
    oPerfStatMonitor = null;
  }

  @Test
  public void testCalcCycle() {
    Assert.assertEquals("testMonitor", oPerfStatMonitor.getName());
    Assert.assertEquals(0, oPerfStatMonitor.getIndex());
    oPerfStatMonitor.addThreadStat(oPerfStatSuccFail);
    oPerfStatMonitor.calcCycle(System.currentTimeMillis(), 20);
    Assert.assertEquals("testMergeFrom", oPerfStatMonitor.getPerfStat().getName());
    Assert.assertEquals(2, oPerfStatMonitor.getPerfResultList().size());

    //Test Format
    StringBuilder oBuilder = new StringBuilder();
    oPerfStatMonitor.format(oBuilder, "Test");
    Assert.assertEquals("  all succ  :Test  all fail  :Test", oBuilder.toString());
  }
}
