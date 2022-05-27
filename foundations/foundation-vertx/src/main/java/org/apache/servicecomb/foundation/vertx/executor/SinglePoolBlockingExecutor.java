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

package org.apache.servicecomb.foundation.vertx.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SinglePoolBlockingExecutor implements Executor {
  private static final Logger LOGGER = LoggerFactory.getLogger(SinglePoolBlockingExecutor.class);

  private static final Executor SINGLE_POOL = Executors.newSingleThreadExecutor((r) -> {
    Thread thread = new Thread(r);
    thread.setName("single-pool-blocking-executor");
    return thread;
  });

  public static SinglePoolBlockingExecutor create() {
    return new SinglePoolBlockingExecutor();
  }

  private SinglePoolBlockingExecutor() {

  }

  @Override
  public void execute(Runnable command) {
    SINGLE_POOL.execute(() -> {
      try {
        command.run();
      } catch (Throwable e) {
        LOGGER.error("Logic should not throw exception, please fix it", e);
      }
    });
  }
}
