/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.common.base;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryableRunnable implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(RetryableRunnable.class);

  private final DescriptiveRunnable runnable;

  private final int sleepInMs;

  public RetryableRunnable(DescriptiveRunnable runnable, int sleepInMs) {
    this.runnable = runnable;
    this.sleepInMs = sleepInMs;
  }

  @Override
  public void run() {
    boolean success;
    do {
      try {
        LOGGER.info("Running [{}] task", runnable.description());
        runnable.run();
        success = true;
      } catch (Throwable e) {
        success = false;
        LOGGER.error("Failed to run [{}] task", runnable.description(), e);
        sleep(sleepInMs);
      }
    } while (!success && !Thread.currentThread().isInterrupted());

    LOGGER.info("Task [{}] completed", runnable.description());
  }

  private void sleep(int timeout) {
    try {
      TimeUnit.MILLISECONDS.sleep(timeout);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
  }
}
