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

package org.apache.servicecomb.service.center.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractTask {
  interface Task {
    void execute();
  }

  class BackOffSleepTask implements Task {
    final long base = 3000;

    final long max = 60000;

    long waitTime;

    Task nextTask;

    BackOffSleepTask(int failedCount, Task nextTask) {
      this.waitTime = failedCount * base;
      this.nextTask = nextTask;
    }

    BackOffSleepTask(long waitTime, Task nextTask) {
      this.waitTime = waitTime;
      this.nextTask = nextTask;
    }

    @Override
    public void execute() {
      long time = Math.min(max, waitTime);
      try {
        Thread.sleep(time);
      } catch (InterruptedException e) {
        LOGGER.error("unexpected interrupt during sleep", e);
      }
      startTask(nextTask);
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterRegistration.class);

  private final ExecutorService taskPool;

  protected AbstractTask(String taskName) {
    this.taskPool = Executors.newSingleThreadExecutor((task) ->
        new Thread(task, taskName));
  }

  protected void startTask(Task task) {
    try {
      this.taskPool.execute(() -> {
        try {
          task.execute();
        } catch (Throwable e) {
          LOGGER.error("unexpected error execute task {}", task.getClass().getName(), e);
        }
      });
    } catch (RejectedExecutionException e) {
      LOGGER.error("execute task rejected {}", task.getClass().getName(), e);
    }
  }

  public void stop() {
    this.taskPool.shutdownNow();
  }
}
