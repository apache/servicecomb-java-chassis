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
package org.apache.servicecomb.serviceregistry.diagnosis.instance;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Holder;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.vertx.core.json.Json;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestInstanceCacheCheckTask {
  @Mocked
  AppManager appManager;

  ScheduledThreadPoolExecutor taskPool = new ScheduledThreadPoolExecutor(2,
      task -> new Thread(task, "Service Center Task test thread"),
      (task, executor) -> System.out.println("Too many pending tasks, reject " + task.getClass().getName()));

  EventBus eventBus = new EventBus();

  InstanceCacheCheckTask task = new InstanceCacheCheckTask();

  InstanceCacheSummary result;

  @Before
  public void setUp() {
    task.setAppManager(appManager);
    task.setTaskPool(taskPool);
    task.setEventBus(eventBus);
    task.setTimeUnit(TimeUnit.MILLISECONDS);

    new MockUp<InstanceCacheChecker>() {
      @Mock
      InstanceCacheSummary check() {
        return new InstanceCacheSummary();
      }
    };
  }

  @After
  public void tearDown() throws Exception {
    ArchaiusUtils.resetConfig();
    taskPool.shutdownNow();
  }

  @Test
  public void manualTask() throws InterruptedException {

    ArchaiusUtils.setProperty(InstanceCacheCheckTask.AUTO_INTERVAL, 0);
    CountDownLatch latch = new CountDownLatch(1);
    eventBus.register(new Object() {
      @Subscribe
      public void onChecked(InstanceCacheSummary instanceCacheSummary) {
        result = instanceCacheSummary;
        latch.countDown();
      }
    });
    task.init();

    ArchaiusUtils.setProperty(InstanceCacheCheckTask.MANUAL, UUID.randomUUID().toString());
    latch.await();

    Assert.assertEquals("{\"status\":null,\"producers\":[],\"timestamp\":0}", Json.encode(result));
  }

  @Test
  public void autoTask_normal() throws InterruptedException {
    ArchaiusUtils.setProperty(InstanceCacheCheckTask.AUTO_INTERVAL, 1);
    CountDownLatch latch = new CountDownLatch(1);
    eventBus.register(new Object() {
      @Subscribe
      public void onChecked(InstanceCacheSummary instanceCacheSummary) {
        result = instanceCacheSummary;
        ((ScheduledFuture<?>) Deencapsulation.getField(task, "scheduledFuture")).cancel(false);
        latch.countDown();
      }
    });
    task.init();

    latch.await();
    Assert.assertNotNull(Deencapsulation.getField(task, "scheduledFuture"));
    Assert.assertEquals("{\"status\":null,\"producers\":[],\"timestamp\":0}", Json.encode(result));
  }

  @Test
  public void autoTask_clearOldTask() {
    Holder<Boolean> cancelResult = new Holder<>();
    ScheduledFuture<?> scheduledFuture = new MockUp<ScheduledFuture<?>>() {
      @Mock
      boolean cancel(boolean mayInterruptIfRunning) {
        cancelResult.value = true;
        return true;
      }
    }.getMockInstance();

    ArchaiusUtils.setProperty(InstanceCacheCheckTask.AUTO_INTERVAL, 0);
    Deencapsulation.setField(task, "scheduledFuture", scheduledFuture);
    task.init();

    Assert.assertNull(Deencapsulation.getField(task, "scheduledFuture"));
    Assert.assertTrue(cancelResult.value);
  }

  @Test
  public void autoTask_invalidIntervalZero() {
    ArchaiusUtils.setProperty(InstanceCacheCheckTask.AUTO_INTERVAL, 0);
    task.init();

    Assert.assertNull(Deencapsulation.getField(task, "scheduledFuture"));
  }

  @Test
  public void autoTask_invalidIntervalLessThanZero() {
    ArchaiusUtils.setProperty(InstanceCacheCheckTask.AUTO_INTERVAL, -1);
    task.init();

    Assert.assertNull(Deencapsulation.getField(task, "scheduledFuture"));
  }
}
