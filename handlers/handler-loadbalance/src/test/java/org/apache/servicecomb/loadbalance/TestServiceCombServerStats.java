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

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.test.scaffolding.time.MockClock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestServiceCombServerStats {
  @Before
  public void before() {
    releaseTryingChance();
  }

  @After
  public void after() {
    releaseTryingChance();
  }

  @Test
  public void testSimpleThread() {
    long time = System.currentTimeMillis();
    ServiceCombServerStats stats = new ServiceCombServerStats(null);
    stats.markFailure();
    stats.markFailure();
    Assert.assertEquals(2, stats.getContinuousFailureCount());
    stats.markSuccess();
    Assert.assertEquals(0, stats.getContinuousFailureCount());
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
    ServiceCombServerStats stats = new ServiceCombServerStats(null);
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
    ServiceCombServerStats stats = new ServiceCombServerStats(null, new MockClock(1000L));
    Assert.assertEquals(1000, stats.getLastVisitTime());
    stats.markSuccess();
    stats.markFailure();
    Assert.assertEquals(2, stats.getTotalRequests());
    Assert.assertEquals(50, stats.getFailedRate());
    Assert.assertEquals(50, stats.getSuccessRate());
    stats.clock = new MockClock(60000L + 2000L);
    stats.markSuccess();
    Assert.assertEquals(1, stats.getTotalRequests());
    Assert.assertEquals(0, stats.getFailedRate());
    Assert.assertEquals(100, stats.getSuccessRate());
  }

  @Test
  public void testGlobalAllowIsolatedServerTryingFlag_apply_with_null_precondition() {
    Invocation invocation = new Invocation();
    Assert.assertTrue(ServiceCombServerStats.applyForTryingChance(invocation));
    Assert.assertSame(invocation, ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().getInvocation());
  }

  @Test
  public void testGlobalAllowIsolatedServerTryingFlag_apply_with_chance_occupied() {
    Invocation invocation = new Invocation();
    Assert.assertTrue(ServiceCombServerStats.applyForTryingChance(invocation));
    Assert.assertSame(invocation, ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().getInvocation());

    Invocation otherInvocation = new Invocation();
    Assert.assertFalse(ServiceCombServerStats.applyForTryingChance(otherInvocation));
    Assert.assertSame(invocation, ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().getInvocation());
  }

  @Test
  public void testGlobalAllowIsolatedServerTryingFlag_apply_with_flag_outdated() {
    Invocation invocation = new Invocation();
    Assert.assertTrue(ServiceCombServerStats.applyForTryingChance(invocation));
    Assert.assertSame(invocation, ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().getInvocation());
    ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().clock = new MockClock(
        ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().startTryingTimestamp + 60000
    );

    Invocation otherInvocation = new Invocation();
    Assert.assertTrue(ServiceCombServerStats.applyForTryingChance(otherInvocation));
    Assert
        .assertSame(otherInvocation, ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().getInvocation());
  }

  public static void releaseTryingChance() {
    ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.set(null);
  }

  public static Invocation getTryingIsolatedServerInvocation() {
    return Optional.ofNullable(ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get())
        .map(TryingIsolatedServerMarker::getInvocation)
        .orElse(null);
  }
}
