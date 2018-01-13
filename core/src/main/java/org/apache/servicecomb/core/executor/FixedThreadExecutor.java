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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

public class FixedThreadExecutor implements Executor {
  private static final Logger LOGGER = LoggerFactory.getLogger(FixedThreadExecutor.class);

  public static final String KEY_GROUP = "servicecomb.executor.default.group";

  public static final String KEY_THREAD = "servicecomb.executor.default.thread-per-group";

  // to avoid multiple network thread conflicted when put tasks to executor queue
  private List<Executor> executorList = new ArrayList<>();

  // for bind network thread to one executor
  // it's impossible that has too many network thread, so index will not too big that less than 0
  private AtomicInteger index = new AtomicInteger();

  private Map<Long, Executor> threadExecutorMap = new ConcurrentHashMapEx<>();

  public FixedThreadExecutor() {
    int groupCount = DynamicPropertyFactory.getInstance().getIntProperty(KEY_GROUP, 2).get();
    int threadPerGroup = DynamicPropertyFactory.getInstance()
        .getIntProperty(KEY_THREAD, Runtime.getRuntime().availableProcessors())
        .get();
    LOGGER.info("executor group {}, thread per group {}.", groupCount, threadPerGroup);

    for (int groupIdx = 0; groupIdx < groupCount; groupIdx++) {
      executorList.add(Executors.newFixedThreadPool(threadPerGroup));
    }
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
}
