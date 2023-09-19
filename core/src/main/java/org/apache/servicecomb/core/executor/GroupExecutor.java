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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class GroupExecutor implements Executor, Closeable {
  private static final Logger LOGGER = LoggerFactory.getLogger(GroupExecutor.class);

  public static final String KEY_GROUP = "servicecomb.executor.default.group";

  // Deprecated
  public static final String KEY_OLD_MAX_THREAD = "servicecomb.executor.default.thread-per-group";

  public static final String KEY_CORE_THREADS = "servicecomb.executor.default.coreThreads-per-group";

  public static final String KEY_MAX_THREADS = "servicecomb.executor.default.maxThreads-per-group";

  public static final String KEY_MAX_IDLE_SECOND = "servicecomb.executor.default.maxIdleSecond-per-group";

  public static final String KEY_MAX_QUEUE_SIZE = "servicecomb.executor.default.maxQueueSize-per-group";

  private static final AtomicBoolean LOG_PRINTED = new AtomicBoolean();

  private final Environment environment;

  protected String groupName;

  protected int groupCount;

  protected int coreThreads;

  protected int maxThreads;

  protected int maxIdleInSecond;

  protected int maxQueueSize;

  // to avoid multiple network thread conflicted when put tasks to executor queue
  private final List<ExecutorService> executorList = new ArrayList<>();

  // for bind network thread to one executor
  // it's impossible that has too many network thread, so index will not too big that less than 0
  private final AtomicInteger index = new AtomicInteger();

  private final Map<Long, Executor> threadExecutorMap = new ConcurrentHashMapEx<>();

  public GroupExecutor(Environment environment) {
    this.environment = environment;
  }

  public GroupExecutor init() {
    return init("group");
  }

  public GroupExecutor init(String groupName) {
    this.groupName = groupName;
    initConfig();

    for (int groupIdx = 0; groupIdx < groupCount; groupIdx++) {
      GroupThreadFactory factory = new GroupThreadFactory(groupName + groupIdx);

      ThreadPoolExecutorEx executor = new ThreadPoolExecutorEx(coreThreads,
          maxThreads,
          maxIdleInSecond,
          TimeUnit.SECONDS,
          new LinkedBlockingQueueEx(maxQueueSize),
          factory);
      executorList.add(executor);
    }

    return this;
  }

  public void initConfig() {
    if (LOG_PRINTED.compareAndSet(false, true)) {
      LOGGER.info("thread pool rules:\n"
          + "1.use core threads.\n"
          + "2.if all core threads are busy, then create new thread.\n"
          + "3.if thread count reach the max limitation, then queue the request.\n"
          + "4.if queue is full, and threads count is max, then reject the request.");
    }

    groupCount = environment.getProperty(KEY_GROUP, int.class, 2);
    coreThreads = environment.getProperty(KEY_CORE_THREADS, int.class, 25);

    maxThreads = environment.getProperty(KEY_MAX_THREADS, int.class, -1);
    if (maxThreads <= 0) {
      maxThreads = environment.getProperty(KEY_OLD_MAX_THREAD, int.class, -1);
      if (maxThreads > 0) {
        LOGGER.warn("{} is deprecated, recommended to use {}.", KEY_OLD_MAX_THREAD, KEY_MAX_THREADS);
      } else {
        maxThreads = 100;
      }
    }
    if (coreThreads > maxThreads) {
      LOGGER.warn("coreThreads is bigger than maxThreads, change from {} to {}.", coreThreads, maxThreads);
      coreThreads = maxThreads;
    }

    maxIdleInSecond = environment.getProperty(KEY_MAX_IDLE_SECOND, int.class, 60);
    maxQueueSize = environment.getProperty(KEY_MAX_QUEUE_SIZE, int.class, Integer.MAX_VALUE);

    LOGGER.info(
        "executor name={}, group={}. per group settings, coreThreads={}, maxThreads={}, maxIdleInSecond={}, maxQueueSize={}.",
        groupName, groupCount, coreThreads, maxThreads, maxIdleInSecond, maxQueueSize);
  }

  public List<ExecutorService> getExecutorList() {
    return executorList;
  }

  @Override
  public void execute(Runnable command) {
    long threadId = Thread.currentThread().getId();
    Executor executor = threadExecutorMap.computeIfAbsent(threadId, this::chooseExecutor);

    executor.execute(command);
  }

  private Executor chooseExecutor(long threadId) {
    int idx = index.getAndIncrement() % executorList.size();
    return executorList.get(idx);
  }

  @Override
  public void close() {
    for (ExecutorService executorService : executorList) {
      executorService.shutdown();
    }
    executorList.clear();
  }
}
