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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现调用链的优雅停止： 当调用链没有返回的时候，等待返回或者超时
 */
public final class ShutdownHookHandler implements Handler, Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ShutdownHookHandler.class);

  public static final ShutdownHookHandler INSTANCE = new ShutdownHookHandler();

  private final AtomicLong requestCounter = new AtomicLong(0);

  private final AtomicLong responseCounter = new AtomicLong(0);

  private final int timeout = 600;

  private final int period = 10;

  private volatile boolean shuttingDown = false;

  private ShutdownHookHandler() {
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
    //      下面的两次catch，可能会导致一次请求，对应2次应答
    requestCounter.incrementAndGet();
    try {
      invocation.next(resp -> {
        try {
          asyncResp.handle(resp);
        } finally {
          responseCounter.incrementAndGet();
        }
      });
    } catch (Throwable e) {
      responseCounter.incrementAndGet();
      throw e;
    }
  }

  @Override
  public void run() {
    shuttingDown = true;
    LOG.warn("handler chain is shutting down");
    int time = 0;
    while (getActiveCount() != 0 && time <= timeout) {
      try {
        TimeUnit.SECONDS.sleep(period);
      } catch (InterruptedException e) {
        LOG.warn(e.getMessage());
      }
      time = time + period;
      LOG.warn("waiting invocation to finish in seconds " + time);
    }
    LOG.warn("handler chain is shut down");
  }
}
