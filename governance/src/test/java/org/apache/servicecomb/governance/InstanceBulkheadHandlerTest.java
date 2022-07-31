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

package org.apache.servicecomb.governance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class})
public class InstanceBulkheadHandlerTest {
  private InstanceBulkheadHandler instanceBulkheadHandler;

  @Autowired
  public void setInstanceBulkheadHandler(InstanceBulkheadHandler instanceBulkheadHandler) {
    this.instanceBulkheadHandler = instanceBulkheadHandler;
  }

  @Test
  public void test_instance_bulkhead_work() throws Throwable {

    // instance1
    DecorateCheckedSupplier<String> dsInstance1 = Decorators.ofCheckedSupplier(() -> "wake");

    GovernanceRequest requestInstance1 = new GovernanceRequest();
    requestInstance1.setInstanceId("instance01");
    requestInstance1.setServiceName("service01");
    requestInstance1.setUri("/test");

    Bulkhead bulkheadInstance1 = instanceBulkheadHandler.getActuator(requestInstance1);
    dsInstance1.withBulkhead(bulkheadInstance1);

    // instance2
    DecorateCheckedSupplier<String> dsInstance2 = Decorators.ofCheckedSupplier(() -> {
      Thread.sleep(1000);
      return "sleep";
    });

    GovernanceRequest requestInstance2 = new GovernanceRequest();
    requestInstance2.setInstanceId("instance02");
    requestInstance2.setServiceName("service01");
    requestInstance2.setUri("/test");

    Bulkhead bulkheadInstance2 = instanceBulkheadHandler.getActuator(requestInstance2);
    dsInstance2.withBulkhead(bulkheadInstance2);

    Executor executor = Executors.newFixedThreadPool(4);
    AtomicInteger wakeCount = new AtomicInteger(0);
    AtomicInteger sleepCount = new AtomicInteger(0);
    AtomicInteger errorCount = new AtomicInteger(0);
    AtomicInteger rejectCount = new AtomicInteger(0);
    CountDownLatch countDownLatch = new CountDownLatch(100);
    for (int i = 0; i < 100; i++) {
      final int num = i;
      executor.execute(() -> {
        // 50% for each server
        if (num % 2 == 0) {
          runCommand(dsInstance1, wakeCount, sleepCount, errorCount, rejectCount, countDownLatch);
        } else {
          runCommand(dsInstance2, wakeCount, sleepCount, errorCount, rejectCount, countDownLatch);
        }
      });
    }
    countDownLatch.await(100, TimeUnit.SECONDS);
    Assertions.assertEquals(50, wakeCount.get());
    Assertions.assertEquals(2, sleepCount.get());
    Assertions.assertEquals(0, errorCount.get());
    Assertions.assertEquals(48, rejectCount.get());
  }

  private void runCommand(DecorateCheckedSupplier<String> ds, AtomicInteger wakeCount, AtomicInteger sleepCount,
      AtomicInteger errorCount, AtomicInteger rejectCount, CountDownLatch countDownLatch) {
    try {
      String result = ds.get();
      if ("wake".equals(result)) {
        wakeCount.incrementAndGet();
      } else if ("sleep".equals(result)) {
        sleepCount.incrementAndGet();
      } else {
        errorCount.incrementAndGet();
      }
    } catch (BulkheadFullException e) {
      rejectCount.incrementAndGet();
    } catch (Throwable e) {
      errorCount.incrementAndGet();
    }
    countDownLatch.countDown();
  }
}
