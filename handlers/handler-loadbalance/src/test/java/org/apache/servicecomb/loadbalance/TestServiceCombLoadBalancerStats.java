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

import static org.awaitility.Awaitility.await;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceInstancePing;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestServiceCombLoadBalancerStats {
  @BeforeClass
  public static void beforeClass() {
    // avoid mock
    ServiceCombLoadBalancerStats.INSTANCE.init();
  }

  @Test
  public void testServiceExpire(@Injectable Transport transport, @Mocked SPIServiceUtils utils, @Injectable
      MicroserviceInstancePing ping) throws Exception {
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setInstanceId("instance1");

    new Expectations() {
      {
        SPIServiceUtils.getPriorityHighestService(MicroserviceInstancePing.class);
        result = ping;
        ping.ping(instance);
        result = false;
      }
    };

    ServiceCombLoadBalancerStats serviceCombLoadBalancerStats = new ServiceCombLoadBalancerStats();
    serviceCombLoadBalancerStats.setServerExpireInSeconds(2);
    serviceCombLoadBalancerStats.setTimerIntervalInMilis(500);
    serviceCombLoadBalancerStats.init();

    ServiceCombServer serviceCombServer = new ServiceCombServer(transport,
        new CacheEndpoint("rest://localhost:8080", instance));
    serviceCombLoadBalancerStats.markSuccess(serviceCombServer);
    ServiceCombServerStats stats = serviceCombLoadBalancerStats.getServiceCombServerStats(serviceCombServer);
    Assert.assertEquals(serviceCombLoadBalancerStats.getPingView().size(), 1);
    await().atMost(5, TimeUnit.SECONDS)
        .until(() -> {
          return serviceCombLoadBalancerStats.getPingView().size() <= 0;
        });
    Assert.assertEquals(serviceCombLoadBalancerStats.getPingView().size(), 0);
    System.out.print(stats.getFailedRequests());
    Assert.assertTrue(stats.getFailedRequests() >= 1);
  }

  @Test
  public void testSimpleThread(@Injectable Transport transport) {
    long time = System.currentTimeMillis();
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setInstanceId("instance1");
    ServiceCombServer serviceCombServer = new ServiceCombServer(transport,
        new CacheEndpoint("rest://localhost:8080", instance));
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(serviceCombServer);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(serviceCombServer);
    Assert.assertEquals(
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getCountinuousFailureCount(),
        2);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(serviceCombServer);
    Assert.assertEquals(
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getCountinuousFailureCount(),
        0);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(serviceCombServer);
    Assert
        .assertEquals(
            ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getTotalRequests(), 4);
    Assert.assertEquals(
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getFailedRate(), 50);
    Assert.assertEquals(
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getSuccessRate(), 50);
    Assert.assertTrue(
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getLastVisitTime() <= System
            .currentTimeMillis()
            && ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getLastVisitTime()
            >= time);
  }

  @Test
  public void testMiltiThread(@Injectable Transport transport) throws Exception {
    long time = System.currentTimeMillis();
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setInstanceId("instance2");
    ServiceCombServer serviceCombServer = new ServiceCombServer(transport,
        new CacheEndpoint("rest://localhost:8080", instance));

    CountDownLatch latch = new CountDownLatch(10);
    for (int i = 0; i < 10; i++) {
      new Thread() {
        public void run() {
          ServiceCombLoadBalancerStats.INSTANCE.markFailure(serviceCombServer);
          ServiceCombLoadBalancerStats.INSTANCE.markFailure(serviceCombServer);
          ServiceCombLoadBalancerStats.INSTANCE.markSuccess(serviceCombServer);
          ServiceCombLoadBalancerStats.INSTANCE.markSuccess(serviceCombServer);
          latch.countDown();
        }
      }.start();
    }
    latch.await(30, TimeUnit.SECONDS);
    Assert.assertEquals(
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getTotalRequests(),
        4 * 10);
    Assert.assertEquals(
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getFailedRate(), 50);
    Assert.assertEquals(
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getSuccessRate(), 50);
    Assert.assertEquals(
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getSuccessRequests(), 20);
    Assert.assertTrue(
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getLastVisitTime() <= System
            .currentTimeMillis()
            && ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getLastVisitTime()
            >= time);

    // time consuming test for timers, taking about 20 seconds. ping timer will update instance status to failure
    Assert.assertTrue(
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getFailedRate() <= 50);
    long beginTime = System.currentTimeMillis();
    long rate = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getFailedRequests();
    while (rate <= 20 &&
        System.currentTimeMillis() - beginTime <= 30000) {
      Thread.sleep(2000);
      rate = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getFailedRequests();
      System.out.println("XXXTTTTT1" + rate);
    }

    Assert.assertTrue(System.currentTimeMillis() - beginTime < 30000);
    Assert
        .assertTrue(
            ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer).getFailedRequests()
                > 20);
  }
}
