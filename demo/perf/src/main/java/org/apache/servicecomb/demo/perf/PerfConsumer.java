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
package org.apache.servicecomb.demo.perf;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.servicecomb.provider.pojo.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PerfConsumer {
  private static final Logger LOGGER = LoggerFactory.getLogger(PerfConsumer.class);

  private Intf intf;

  public void runConsumer() throws InterruptedException, ExecutionException {
    intf = Invoker.createProxy(
        PerfConfiguration.producer,
        "impl",
        Intf.class);

    if (PerfConfiguration.sync) {
      runSyncConsumers();
      return;
    }

    runAsyncConsumers();
  }

  private void runAsyncConsumers() throws InterruptedException, ExecutionException {
    CompletableFuture<String> future = intf.asyncQuery(PerfConfiguration.id,
        PerfConfiguration.step,
        PerfConfiguration.all,
        PerfConfiguration.fromDB);
    LOGGER.info("runAsyncConsumer: {}", future.get());

    for (int idx = 0; idx < PerfConfiguration.asyncCount; idx++) {
      runAsyncConsumer();
    }
  }

  private void runAsyncConsumer() {
    CompletableFuture<String> future = intf.asyncQuery(PerfConfiguration.id,
        PerfConfiguration.step,
        PerfConfiguration.all,
        PerfConfiguration.fromDB);
    future.whenComplete((r, e) -> {
      if (e == null) {
        runAsyncConsumer();
        return;
      }

      throw new IllegalStateException("invoke failed.", e);
    });
  }

  private void runSyncConsumers() {
    LOGGER.info("runSyncConsumer: {}",
        intf.syncQuery(PerfConfiguration.id,
            PerfConfiguration.step,
            PerfConfiguration.all,
            PerfConfiguration.fromDB));

    Executor executor = Executors.newFixedThreadPool(PerfConfiguration.syncCount);
    for (int idx = 0; idx < PerfConfiguration.syncCount; idx++) {
      executor.execute(this::runSyncConsumer);
    }
  }

  private void runSyncConsumer() {
    try {
      for (; ; ) {
        intf.syncQuery(PerfConfiguration.id,
            PerfConfiguration.step,
            PerfConfiguration.all,
            PerfConfiguration.fromDB);
      }
    } catch (Throwable e) {
      throw new IllegalStateException("invoke failed.", e);
    }
  }
}
