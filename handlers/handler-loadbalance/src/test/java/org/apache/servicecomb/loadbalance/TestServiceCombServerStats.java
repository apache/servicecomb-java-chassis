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

package org.apache.servicecomb.loadbalance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;

public class TestServiceCombServerStats {
  @Test
  public void testSimpleThread() {
    long time = System.currentTimeMillis();
    ServiceCombServerStats stats = new ServiceCombServerStats();
    stats.markFailure();
    stats.markFailure();
    Assert.assertEquals(stats.getCountinuousFailureCount(), 2);
    stats.markSuccess();
    Assert.assertEquals(stats.getCountinuousFailureCount(), 0);
    stats.markSuccess();
    Assert.assertEquals(stats.getTotalRequests(), 4);
    Assert.assertEquals(stats.getFailedRate(), 50);
    Assert.assertEquals(stats.getSuccessRate(), 50);
    Assert.assertTrue(stats.getLastVisitTime() <= System.currentTimeMillis() && stats.getLastVisitTime() >= time);
    Assert.assertTrue(stats.getLastActiveTime() <= System.currentTimeMillis() && stats.getLastActiveTime() >= time);
  }

  @Test
  public void testMiltiThread() throws Exception {
    long time = System.currentTimeMillis();
    ServiceCombServerStats stats = new ServiceCombServerStats();
    CountDownLatch latch = new CountDownLatch(10);
    for (int i = 0; i < 10; i++) {
      new Thread() {
        public void run() {
          stats.markFailure();
          stats.markFailure();
          stats.markSuccess();
          stats.markSuccess();
          latch.countDown();
        }
      }.start();
    }
    latch.await(30, TimeUnit.SECONDS);
    Assert.assertEquals(stats.getTotalRequests(), 4 * 10);
    Assert.assertEquals(stats.getFailedRate(), 50);
    Assert.assertEquals(stats.getSuccessRate(), 50);
    Assert.assertTrue(stats.getLastVisitTime() <= System.currentTimeMillis() && stats.getLastVisitTime() >= time);
    Assert.assertTrue(stats.getLastActiveTime() <= System.currentTimeMillis() && stats.getLastActiveTime() >= time);
  }

  @Test
  public void testTimeWindow() {
    new MockUp<System>() {
      @Mock
      long currentTimeMillis() {
        return 1000;
      }
    };
    ServiceCombServerStats stats = new ServiceCombServerStats();
    Assert.assertEquals(stats.getLastVisitTime(), 1000);
    stats.markSuccess();
    stats.markFailure();
    Assert.assertEquals(stats.getTotalRequests(), 2);
    Assert.assertEquals(stats.getFailedRate(), 50);
    Assert.assertEquals(stats.getSuccessRate(), 50);
    new MockUp<System>() {
      @Mock
      long currentTimeMillis() {
        return 60000 + 2000;
      }
    };
    stats.markSuccess();
    Assert.assertEquals(stats.getTotalRequests(), 1);
    Assert.assertEquals(stats.getFailedRate(), 0);
    Assert.assertEquals(stats.getSuccessRate(), 100);
  }
}
