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

import mockit.Mock;
import mockit.MockUp;

public class TestPerfStatContext {

  PerfStatContext oPerfStatContext = null;

  @Before
  public void setUp() throws Exception {
    oPerfStatContext = new PerfStatContext();
  }

  @After
  public void tearDown() throws Exception {
    oPerfStatContext = null;
  }

  @Test
  public void testDefaultValues() {
    Assert.assertEquals(0, oPerfStatContext.getMsgCount());
    Assert.assertTrue(oPerfStatContext.getLatency() >= 0);
  }

  @Test
  public void testIntializedValues() throws InterruptedException {
    new MockUp<System>() {
      int count = 0;

      @Mock
      public long currentTimeMillis() {
        if (count == 0) {
          count++;
          return 10;
        } else {
          return 20;
        }
      }
    };
    PerfStatContext oPerfStatContext = new PerfStatContext();
    oPerfStatContext.setMsgCount(10);
    Assert.assertEquals(10, oPerfStatContext.getMsgCount());
    Assert.assertEquals(10, oPerfStatContext.getLatency());
  }
}
