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

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPerfStatImpl {

  PerfStatImpl oPerfStatImpl = null;

  PerfStatSuccFail oPerfStatSuccFail = null;

  @Before
  public void setUp() throws Exception {
    oPerfStatImpl = new PerfStatImpl("testData");
    oPerfStatSuccFail = new PerfStatSuccFail("testMergeFrom");
  }

  @After
  public void tearDown() throws Exception {
    oPerfStatImpl = null;
  }

  @Test
  public void testMergeFrom() {
    oPerfStatImpl.mergeFrom(oPerfStatSuccFail);
    Assert.assertEquals("testMergeFrom", oPerfStatImpl.getName());
    Assert.assertEquals(2, oPerfStatImpl.getPerfStatDataList().size());
  }

  @Test
  public void testCalc() {
    oPerfStatImpl = new PerfStatImpl("testConstructor", new PerfStatData("test"));
    PerfResult oPerfResult = new PerfResult();
    oPerfResult.setName("test");
    List<PerfResult> oPerfResultList = new ArrayList<>();
    oPerfResultList.add(oPerfResult);
    oPerfStatImpl.calc(System.currentTimeMillis(), oPerfResultList);
    Assert.assertEquals(2, oPerfResultList.size());

    //Testing Calc with null PerfStat
    oPerfStatImpl.calc(null, 20, oPerfResultList);
    Assert.assertEquals(2, oPerfResultList.size()); //The list size does not increase

    //Testing Calc with PerfStat
    oPerfStatImpl.calc(oPerfStatSuccFail, 20, oPerfResultList);
    Assert.assertEquals(3, oPerfResultList.size());
  }
}
