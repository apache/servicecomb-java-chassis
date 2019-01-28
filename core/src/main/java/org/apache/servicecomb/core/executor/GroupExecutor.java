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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

public class GroupExecutor implements Executor, Closeable {
  private static final Logger LOGGER = LoggerFactory.getLogger(GroupExecutor.class);

  public static final String KEY_GROUP = "servicecomb.executor.default.group";

  // Deprecated
  public static final String KEY_THREAD = "servicecomb.executor.default.thread-per-group";

  public static final String KEY_CORE_THREADS = "servicecomb.executor.default.coreThreads-per-group";

  public static final String KEY_MAX_THREADS = "servicecomb.executor.default.maxThreads-per-group";

  public static final String KEY_MAX_IDLE_SECOND = "servicecomb.executor.default.maxIdleSecond-per-group";

  public static final String KEY_MAX_QUEUE_SIZE = "servicecomb.executor.default.maxQueueSize-per-group";

  protected int groupCount;

  protected int coreThreads;

  protected int maxThreads;

  protected int maxIdleInSecond;

  protected int maxQueueSize;

  // to avoid multiple network thread conflicted when put tasks to executor queue
  private List<ExecutorService> executorList = new ArrayList<>();

  // for bind network thread to one executor
  // it's impossible that has too many network thread, so index will not too big that less than 0
  private AtomicInteger index = new AtomicInteger();

  private Map<Long, Executor> threadExecutorMap = new ConcurrentHashMapEx<>();

  public void init() {
    initConfig();

    for (int groupIdx = 0; groupIdx < groupCount; groupIdx++) {
      ThreadPoolExecutor executor = new ThreadPoolExecutor(coreThreads,
          maxThreads,
          maxIdleInSecond,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(maxQueueSize));
      executorList.add(executor);
    }
  }

  public void initConfig() {
    groupCount = DynamicPropertyFactory.getInstance().getIntProperty(KEY_GROUP, 2).get();
    coreThreads = DynamicPropertyFactory.getInstance().getIntProperty(KEY_CORE_THREADS, 25).get();

    maxThreads = DynamicPropertyFactory.getInstance().getIntProperty(KEY_MAX_THREADS, -1).get();
    if (maxThreads <= 0) {
      maxThreads = DynamicPropertyFactory.getInstance().getIntProperty(KEY_THREAD, -1).get();
      if (maxThreads > 0) {
        LOGGER.warn("{} is deprecated, recommended to use {}.", KEY_THREAD, KEY_MAX_THREADS);
      } else {
        maxThreads = 100;
      }
    }
    if (coreThreads > maxThreads) {
      LOGGER.warn("coreThreads is bigger than maxThreads, change from {} to {}.", coreThreads, maxThreads);
      coreThreads = maxThreads;
    }

    maxIdleInSecond = DynamicPropertyFactory.getInstance().getIntProperty(KEY_MAX_IDLE_SECOND, 60).get();
    maxQueueSize = DynamicPropertyFactory.getInstance().getIntProperty(KEY_MAX_QUEUE_SIZE, Integer.MAX_VALUE).get();

    LOGGER.info(
        "executor group={}. per group settings, coreThreads={}, maxThreads={}, maxIdleInSecond={}, maxQueueSize={}.",
        groupCount, coreThreads, maxThreads, maxIdleInSecond, maxQueueSize);
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
