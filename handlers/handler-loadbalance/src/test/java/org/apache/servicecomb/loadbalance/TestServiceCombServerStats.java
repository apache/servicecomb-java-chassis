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

import org.apache.servicecomb.foundation.test.scaffolding.model.MockClock;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.ws.Holder;

public class TestServiceCombServerStats {
  @Test
  public void testSimpleThread() {
    long time = System.currentTimeMillis();
    ServiceCombServerStats stats = new ServiceCombServerStats();
    stats.markFailure();
    stats.markFailure();
    Assert.assertEquals(2, stats.getCountinuousFailureCount());
    stats.markSuccess();
    Assert.assertEquals(0, stats.getCountinuousFailureCount());
    stats.markSuccess();
    Assert.assertEquals(4, stats.getTotalRequests());
    Assert.assertEquals(50, stats.getFailedRate());
    Assert.assertEquals(50, stats.getSuccessRate());
    Assert.assertTrue(stats.getLastVisitTime() <= System.currentTimeMillis() && stats.getLastVisitTime() >= time);
    Assert.assertTrue(stats.getLastActiveTime() <= System.currentTimeMillis() && stats.getLastActiveTime() >= time);
  }

  @Test
  public void testMiltiThread() throws Exception {
    long time = System.currentTimeMillis();
    ServiceCombServerStats stats = new ServiceCombServerStats();
    CountDownLatch latch = new CountDownLatch(10);
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        stats.markFailure();
        stats.markFailure();
        stats.markSuccess();
        stats.markSuccess();
        latch.countDown();
      }).start();
    }
    latch.await(30, TimeUnit.SECONDS);
    Assert.assertEquals(4 * 10, stats.getTotalRequests());
    Assert.assertEquals(50, stats.getFailedRate());
    Assert.assertEquals(50, stats.getSuccessRate());
    Assert.assertTrue(stats.getLastVisitTime() <= System.currentTimeMillis() && stats.getLastVisitTime() >= time);
    Assert.assertTrue(stats.getLastActiveTime() <= System.currentTimeMillis() && stats.getLastActiveTime() >= time);
  }

  @Test
  public void testTimeWindow() {
    ServiceCombServerStats stats = new ServiceCombServerStats(new MockClock(new Holder<>(1000L)));
    Assert.assertEquals(1000, stats.getLastVisitTime());
    stats.markSuccess();
    stats.markFailure();
    Assert.assertEquals(2, stats.getTotalRequests());
    Assert.assertEquals(50, stats.getFailedRate());
    Assert.assertEquals(50, stats.getSuccessRate());
    stats.clock = new MockClock(new Holder<>(60000L + 2000L));
    stats.markSuccess();
    Assert.assertEquals(1, stats.getTotalRequests());
    Assert.assertEquals(0, stats.getFailedRate());
    Assert.assertEquals(100, stats.getSuccessRate());
  }
}
