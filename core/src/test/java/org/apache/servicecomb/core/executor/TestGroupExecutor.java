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

package org.apache.servicecomb.core.executor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Deencapsulation;

public class TestGroupExecutor {
  String strThreadTest = "default";

  @Before
  public void setup() {
    ArchaiusUtils.resetConfig();
  }

  @AfterClass
  public static void teardown() {
    ArchaiusUtils.resetConfig();
  }

  GroupExecutor groupExecutor = new GroupExecutor();

  @Test
  public void groupCount() {
    groupExecutor.initConfig();
    Assert.assertEquals(2, groupExecutor.groupCount);

    ArchaiusUtils.setProperty(GroupExecutor.KEY_GROUP, 4);
    groupExecutor.initConfig();
    Assert.assertEquals(4, groupExecutor.groupCount);
  }

  @Test
  public void coreThreads() {
    groupExecutor.initConfig();
    Assert.assertEquals(25, groupExecutor.coreThreads);

    ArchaiusUtils.setProperty(GroupExecutor.KEY_CORE_THREADS, 100);
    groupExecutor.initConfig();
    Assert.assertEquals(100, groupExecutor.coreThreads);
  }

  @Test
  public void maxIdleInSecond() {
    groupExecutor.initConfig();
    Assert.assertEquals(60, groupExecutor.maxIdleInSecond);

    ArchaiusUtils.setProperty(GroupExecutor.KEY_MAX_IDLE_SECOND, 100);
    groupExecutor.initConfig();
    Assert.assertEquals(100, groupExecutor.maxIdleInSecond);
  }

  @Test
  public void maxQueueSize() {
    groupExecutor.initConfig();
    Assert.assertEquals(Integer.MAX_VALUE, groupExecutor.maxQueueSize);

    ArchaiusUtils.setProperty(GroupExecutor.KEY_MAX_QUEUE_SIZE, 100);
    groupExecutor.initConfig();
    Assert.assertEquals(100, groupExecutor.maxQueueSize);
  }

  @Test
  public void maxThreads() {
    groupExecutor.initConfig();
    Assert.assertEquals(100, groupExecutor.maxThreads);

    LogCollector collector = new LogCollector();
    ArchaiusUtils.setProperty(GroupExecutor.KEY_OLD_MAX_THREAD, 200);
    groupExecutor.initConfig();
    Assert.assertEquals(200, groupExecutor.maxThreads);
    Assert.assertEquals(
        "servicecomb.executor.default.thread-per-group is deprecated, recommended to use servicecomb.executor.default.maxThreads-per-group.",
        collector.getEvents().get(collector.getEvents().size() - 2).getMessage());
    collector.teardown();

    ArchaiusUtils.setProperty(GroupExecutor.KEY_MAX_THREADS, 300);
    groupExecutor.initConfig();
    Assert.assertEquals(300, groupExecutor.maxThreads);
  }

  @Test
  public void adjustCoreThreads() {
    ArchaiusUtils.setProperty(GroupExecutor.KEY_MAX_THREADS, 10);

    LogCollector collector = new LogCollector();
    groupExecutor.initConfig();
    Assert.assertEquals(10, groupExecutor.maxThreads);
    Assert.assertEquals(
        "coreThreads is bigger than maxThreads, change from 25 to 10.",
        collector.getEvents().get(collector.getEvents().size() - 2).getMessage());
    collector.teardown();
  }

  @Test
  public void testGroupExecutor() {
    groupExecutor.init();
    groupExecutor.execute(() -> {
    });
    Map<Long, Executor> threadExecutorMap = Deencapsulation.getField(groupExecutor, "threadExecutorMap");
    Assert.assertEquals(true, (threadExecutorMap.size() > 0));

    List<Executor> executorList = Deencapsulation.getField(groupExecutor, "executorList");
    Assert.assertEquals(true, (executorList.size() > 1));

    ReactiveExecutor oReactiveExecutor = new ReactiveExecutor();
    oReactiveExecutor.execute(() -> strThreadTest = "thread Ran");
    oReactiveExecutor.close();
    Assert.assertEquals("thread Ran", strThreadTest);
  }
}
