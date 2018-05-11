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

package org.apache.servicecomb.core.handler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现调用链的优雅停止： 当调用链没有返回的时候，等待返回(由于Consumer存在超时，所以必定能够返回)
 */
public final class ShutdownHookHandler implements Handler, Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ShutdownHookHandler.class);

  public static final ShutdownHookHandler INSTANCE = new ShutdownHookHandler();

  private final AtomicLong requestCounter = new AtomicLong(0);

  private final AtomicLong responseCounter = new AtomicLong(0);

  private volatile boolean shuttingDown = false;

  public final Semaphore ALL_INVOCATION_FINISHED = new Semaphore(1);

  private ShutdownHookHandler() {
    try {
      ALL_INVOCATION_FINISHED.acquire();
    } catch (InterruptedException e) {
      throw new ServiceCombException("init invocation finished semaphore failed", e);
    }
    Runtime.getRuntime().addShutdownHook(new Thread(this));
  }

  public long getActiveCount() {
    return requestCounter.get() - responseCounter.get();
  }

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    if (shuttingDown) {
      asyncResp.handle(Response.createFail(invocation.getInvocationType(),
          "shutting down in progress"));
      return;
    }

    // TODO:统计功能应该独立出来，在链中统计，会有各种bug
    // 下面的两次catch，可能会导致一次请求，对应2次应答
    requestCounter.incrementAndGet();
    try {
      invocation.next(resp -> {
        try {
          asyncResp.handle(resp);
        } finally {
          responseCounter.incrementAndGet();
          validAllInvocationFinished();
        }
      });
    } catch (Throwable e) {
      responseCounter.incrementAndGet();
      validAllInvocationFinished();
      throw e;
    }
  }

  private synchronized void validAllInvocationFinished() {
    if (shuttingDown && getActiveCount() <= 0) {
      ALL_INVOCATION_FINISHED.release();
    }
  }

  @Override
  public void run() {
    shuttingDown = true;
    LOG.warn("handler chain is shutting down...");
    validAllInvocationFinished();
  }
}
