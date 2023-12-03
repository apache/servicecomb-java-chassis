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
package org.apache.servicecomb.metrics.core.meter.pool;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.servicecomb.core.executor.ThreadPoolExecutorEx;
import org.apache.servicecomb.foundation.metrics.meter.PeriodMeter;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

public class ThreadPoolMeter implements PeriodMeter {
  public static final String THREAD_POOL_METER = "servicecomb.threadpool";

  public static final String ID = "id";

  public static final String STAGE = "stage";

  public static final String REJECTED_COUNT = "rejectedCount";

  public static final String TASK_COUNT = "taskCount";

  public static final String COMPLETED_TASK_COUNT = "completedTaskCount";

  public static final String CURRENT_THREADS_BUSY = "currentThreadsBusy";

  public static final String MAX_THREADS = "maxThreads";

  public static final String POOL_SIZE = "poolSize";

  public static final String CORE_POOL_SIZE = "corePoolSize";

  public static final String QUEUE_SIZE = "queueSize";

  private final ThreadPoolExecutor threadPoolExecutor;

  private long currentTask;

  private long lastTask;

  private long currentCompletedTask;

  private long lastCompletedTask;

  public ThreadPoolMeter(MeterRegistry meterRegistry, String threadPoolName, ThreadPoolExecutor threadPoolExecutor) {
    this.threadPoolExecutor = threadPoolExecutor;

    Gauge.builder(THREAD_POOL_METER, () -> currentTask)
        .tags(ID, threadPoolName, STAGE, TASK_COUNT)
        .register(meterRegistry);
    Gauge.builder(THREAD_POOL_METER, () -> currentCompletedTask)
        .tags(ID, threadPoolName, STAGE, COMPLETED_TASK_COUNT)
        .register(meterRegistry);
    Gauge.builder(THREAD_POOL_METER, threadPoolExecutor::getActiveCount)
        .tags(ID, threadPoolName, STAGE, CURRENT_THREADS_BUSY)
        .register(meterRegistry);
    Gauge.builder(THREAD_POOL_METER, threadPoolExecutor::getMaximumPoolSize)
        .tags(ID, threadPoolName, STAGE, MAX_THREADS)
        .register(meterRegistry);
    Gauge.builder(THREAD_POOL_METER, threadPoolExecutor::getPoolSize)
        .tags(ID, threadPoolName, STAGE, POOL_SIZE)
        .register(meterRegistry);
    Gauge.builder(THREAD_POOL_METER, threadPoolExecutor::getCorePoolSize)
        .tags(ID, threadPoolName, STAGE, CORE_POOL_SIZE)
        .register(meterRegistry);
    Gauge.builder(THREAD_POOL_METER, () -> threadPoolExecutor.getQueue().size())
        .tags(ID, threadPoolName, STAGE, QUEUE_SIZE)
        .register(meterRegistry);

    if (threadPoolExecutor instanceof ThreadPoolExecutorEx) {
      Gauge.builder(THREAD_POOL_METER, () -> ((ThreadPoolExecutorEx) (threadPoolExecutor)).getRejectedCount())
          .tags(ID, threadPoolName, STAGE, REJECTED_COUNT)
          .register(meterRegistry);
    }
  }

  @Override
  public void poll(long msNow, long secondInterval) {
    long temp = threadPoolExecutor.getTaskCount();
    currentTask = temp - lastTask;
    lastTask = temp;

    temp = threadPoolExecutor.getCompletedTaskCount();
    currentCompletedTask = temp - lastCompletedTask;
    lastCompletedTask = temp;
  }
}
