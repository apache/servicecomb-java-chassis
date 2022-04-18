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

package org.apache.servicecomb.core.provider.consumer;

import static javax.ws.rs.core.Response.Status.REQUEST_TIMEOUT;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionCodes;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

/**
 * 业务线程在阻塞等待着，不必另起线程
 * 将应答流程包装为Runnable，先唤醒业务线程，再在业务线程中执行runnable
 */
public class SyncResponseExecutor implements Executor {
  private final CountDownLatch latch;

  private Runnable cmd;

  private Response response;

  public SyncResponseExecutor() {
    latch = new CountDownLatch(1);
  }

  @Override
  public void execute(Runnable cmd) {
    this.cmd = cmd;

    // one network thread, many connections, then this notify will be performance bottlenecks
    // if save to a queue, and other thread(s) to invoke countDown, will get good performance
    // but if have multiple network thread, this "optimization" will reduce performance
    // now not change this.
    latch.countDown();
  }

  public Response waitResponse(Invocation invocation) throws InvocationException {
    guardedWait(invocation);

    // cmd为null，是没走execute，直接返回的场景
    if (cmd != null) {
      cmd.run();
    }

    return response;
  }

  public void setResponse(Response response) {
    this.response = response;
    if (cmd == null) {
      // 1. 走到这里，没有cmd，说明没走到网络线程，直接就返回了。
      // 2. 或者在网络线程中没使用execute的方式返回，这会导致返回流程在网络线程中执行，虽然不合适，但是也不应该导致业务线程无法唤醒
      latch.countDown();
    }
  }

  private void guardedWait(Invocation invocation) throws InvocationException {
    long wait = getWaitTime(invocation);
    try {
      if (wait <= 0) {
        latch.await();
        return;
      }
      if (latch.await(wait, TimeUnit.MILLISECONDS)) {
        return;
      }
    } catch (InterruptedException e) {
      //ignore
    }
    throw new InvocationException(REQUEST_TIMEOUT, ExceptionCodes.INVOCATION_TIMEOUT, "Invocation Timeout.");
  }

  private long getWaitTime(Invocation invocation) {
    if (invocation.getOperationMeta().getConfig().getMsInvocationTimeout() > 0) {
      // if invocation timeout configured, use it.
      return invocation.getOperationMeta().getConfig().getMsInvocationTimeout();
    }

    // In invocation handlers, may call other microservices, invocation
    // timeout may be much longer than request timeout.
    // But this is quite rare, for simplicity, default two times of request timeout.
    // If users need longer timeout, can configure invocation timeout.
    return invocation.getOperationMeta().getConfig().getMsRequestTimeout() * 2;
  }
}
