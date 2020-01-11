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

package org.apache.servicecomb.foundation.common.concurrency;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for {@link Runnable}.
 * All the exceptions thrown from {@link Runnable#run()} are caught and handled.
 */
public class SuppressedRunnableWrapper implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SuppressedRunnableWrapper.class);

  private final Runnable target;

  private final Consumer<Throwable> errorHandler;

  public SuppressedRunnableWrapper(Runnable runnable) {
    this(runnable, null);
  }

  public SuppressedRunnableWrapper(Runnable runnable, Consumer<Throwable> errorHandler) {
    this.target = runnable;
    this.errorHandler = errorHandler;
  }

  @Override
  public void run() {
    try {
      target.run();
    } catch (Throwable e) {
      handleError(e);
    }
  }

  private void handleError(Throwable e) {
    if (null == errorHandler) {
      LOGGER.error("task {} execute error!", target, e);
      return;
    }
    errorHandler.accept(e);
  }
}
