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

import static org.apache.servicecomb.core.executor.GroupExecutor.KEY_CORE_THREADS;
import static org.apache.servicecomb.core.executor.GroupExecutor.KEY_GROUP;
import static org.apache.servicecomb.core.executor.GroupExecutor.KEY_MAX_IDLE_SECOND;
import static org.apache.servicecomb.core.executor.GroupExecutor.KEY_MAX_QUEUE_SIZE;
import static org.apache.servicecomb.core.executor.GroupExecutor.KEY_MAX_THREADS;
import static org.apache.servicecomb.core.executor.GroupExecutor.KEY_OLD_MAX_THREAD;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import mockit.Deencapsulation;

public class TestGroupExecutor {
  String strThreadTest = "default";

  @BeforeEach
  public void setup() {
    Mockito.when(environment.getProperty(KEY_GROUP, int.class, 2)).thenReturn(2);
    Mockito.when(environment.getProperty(KEY_CORE_THREADS, int.class, 25)).thenReturn(25);
    Mockito.when(environment.getProperty(KEY_MAX_IDLE_SECOND, int.class, 60)).thenReturn(60);
    Mockito.when(environment.getProperty(KEY_MAX_THREADS, int.class, -1)).thenReturn(-1);
    Mockito.when(environment.getProperty(KEY_OLD_MAX_THREAD, int.class, -1)).thenReturn(-1);
    Mockito.when(environment.getProperty(KEY_MAX_QUEUE_SIZE, int.class, Integer.MAX_VALUE))
        .thenReturn(Integer.MAX_VALUE);
  }

  @AfterAll
  public static void teardown() {
  }

  Environment environment = Mockito.mock(Environment.class);

  GroupExecutor groupExecutor = new GroupExecutor(environment);

  @Test
  public void groupCount() {
    groupExecutor.initConfig();
    Assertions.assertEquals(2, groupExecutor.groupCount);

    Mockito.when(environment.getProperty(KEY_GROUP, int.class, 2)).thenReturn(4);
    groupExecutor.initConfig();
    Assertions.assertEquals(4, groupExecutor.groupCount);
  }

  @Test
  public void coreThreads() {
    groupExecutor.initConfig();
    Assertions.assertEquals(25, groupExecutor.coreThreads);

    Mockito.when(environment.getProperty(KEY_CORE_THREADS, int.class, 25)).thenReturn(100);
    groupExecutor.initConfig();
    Assertions.assertEquals(100, groupExecutor.coreThreads);
  }

  @Test
  public void maxIdleInSecond() {
    groupExecutor.initConfig();
    Assertions.assertEquals(60, groupExecutor.maxIdleInSecond);

    Mockito.when(environment.getProperty(KEY_MAX_IDLE_SECOND, int.class, 60)).thenReturn(100);
    groupExecutor.initConfig();
    Assertions.assertEquals(100, groupExecutor.maxIdleInSecond);
  }

  @Test
  public void maxQueueSize() {
    groupExecutor.initConfig();
    Assertions.assertEquals(Integer.MAX_VALUE, groupExecutor.maxQueueSize);
    Mockito.when(environment.getProperty(KEY_MAX_QUEUE_SIZE, int.class, Integer.MAX_VALUE)).thenReturn(100);
    groupExecutor.initConfig();
    Assertions.assertEquals(100, groupExecutor.maxQueueSize);
  }

  @Test
  public void maxThreads() {
    groupExecutor.initConfig();
    Assertions.assertEquals(100, groupExecutor.maxThreads);

    LogCollector collector = new LogCollector();
    Mockito.when(environment.getProperty(KEY_OLD_MAX_THREAD, int.class, -1)).thenReturn(200);
    groupExecutor.initConfig();
    Assertions.assertEquals(200, groupExecutor.maxThreads);
    Assertions.assertEquals(
        "servicecomb.executor.default.thread-per-group is deprecated, recommended to use servicecomb.executor.default.maxThreads-per-group.",
        collector.getEvents().get(collector.getEvents().size() - 2).getMessage().getFormattedMessage());
    collector.teardown();

    Mockito.when(environment.getProperty(KEY_MAX_THREADS, int.class, -1)).thenReturn(300);
    groupExecutor.initConfig();
    Assertions.assertEquals(300, groupExecutor.maxThreads);
  }

  @Test
  public void adjustCoreThreads() {
    Mockito.when(environment.getProperty(KEY_MAX_THREADS, int.class, -1)).thenReturn(10);
    LogCollector collector = new LogCollector();
    groupExecutor.initConfig();
    Assertions.assertEquals(10, groupExecutor.maxThreads);
    Assertions.assertEquals(
        "coreThreads is bigger than maxThreads, change from 25 to 10.",
        collector.getEvents().get(collector.getEvents().size() - 2).getMessage().getFormattedMessage());
    collector.teardown();
  }

  @Test
  public void testGroupExecutor() {
    groupExecutor.init();
    groupExecutor.execute(() -> {
    });
    Map<Long, Executor> threadExecutorMap = Deencapsulation.getField(groupExecutor, "threadExecutorMap");
    Assertions.assertTrue((threadExecutorMap.size() > 0));

    List<Executor> executorList = Deencapsulation.getField(groupExecutor, "executorList");
    Assertions.assertTrue((executorList.size() > 1));

    ReactiveExecutor oReactiveExecutor = new ReactiveExecutor();
    oReactiveExecutor.execute(() -> strThreadTest = "thread Ran");
    oReactiveExecutor.close();
    Assertions.assertEquals("thread Ran", strThreadTest);
  }
}
