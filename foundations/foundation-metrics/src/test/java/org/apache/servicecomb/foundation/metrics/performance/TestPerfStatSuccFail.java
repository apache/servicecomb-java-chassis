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

public class TestPerfStatSuccFail {

  PerfStatSuccFail oPerfStatSuccFail = null;

  @Before
  public void setUp() throws Exception {
    oPerfStatSuccFail = new PerfStatSuccFail("testSuccFail");
  }

  @After
  public void tearDown() throws Exception {
    oPerfStatSuccFail = null;
  }

  @Test
  public void testAdd() {
    oPerfStatSuccFail.add(true, new PerfStatContext());
    Assert.assertEquals(2, oPerfStatSuccFail.getPerfStatDataList().size());

    //Test org.apache.servicecomb.foundation.metrics.performance.PerfStatSuccFail.add(boolean, int, long)
    oPerfStatSuccFail.add(false, 10, 100);
    Assert.assertEquals(2, oPerfStatSuccFail.getPerfStatDataList().size());
  }
}
