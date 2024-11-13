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
package org.apache.servicecomb.registry.etcd;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ConditionWaiter<T> {
  private final AtomicReference<T> dataReference;

  private final AtomicBoolean isComplete;

  private final long sleepDuration;

  private final TimeUnit timeUnit;

  private final ExecutorService executorService;

  public ConditionWaiter(T initialData, long sleepDuration, TimeUnit timeUnit) {
    this.dataReference = new AtomicReference<>(initialData);
    this.isComplete = new AtomicBoolean(false);
    this.sleepDuration = sleepDuration;
    this.timeUnit = timeUnit;
    this.executorService = Executors.newSingleThreadExecutor();
  }

  public T waitForCompletion() {
    while (!isComplete.get()) {
      SleepUtil.sleep(sleepDuration, timeUnit);
    }
    return dataReference.get();
  }

  public void setData(T newData) {
    dataReference.set(newData);
  }

  public void executeTaskAsync(Callable<T> task) {
    CompletableFuture.supplyAsync(() -> {
      try {
        return task.call();
      } catch (Exception e) {
        throw new RuntimeException("Task execution failed", e);
      }
    }, executorService).thenAccept(result -> {
      setData(result);
      isComplete.set(true);
    });
  }

  public static class SleepUtil {
    public static void sleep(long duration, TimeUnit timeUnit) {
      try {
        timeUnit.sleep(duration);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.out.println("Thread was interrupted during sleep!");
      }
    }
  }
}
