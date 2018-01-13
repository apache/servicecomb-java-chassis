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

package org.apache.servicecomb.foundation.vertx.client.tcp;

import java.util.concurrent.TimeoutException;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class TcpRequest {
  private long begin;

  private long msTimeout;

  private Context callContext;

  private long threadId;

  private TcpResponseCallback responseCallback;

  public TcpRequest(long msTimeout, TcpResponseCallback responseCallback) {
    callContext = Vertx.currentContext();
    threadId = Thread.currentThread().getId();
    this.begin = System.currentTimeMillis();
    this.msTimeout = msTimeout;
    this.responseCallback = responseCallback;
  }

  public void onReply(Buffer headerBuffer, Buffer bodyBuffer) {
    TcpData tcpData = new TcpData(headerBuffer, bodyBuffer);

    if (callContext == null || threadId == Thread.currentThread().getId()) {
      responseCallback.success(tcpData);
      return;
    }

    callContext.runOnContext(Void -> {
      responseCallback.success(tcpData);
    });
  }

  public void onSendError(Throwable e) {
    responseCallback.fail(e);
  }

  public boolean isTimeout() {
    return System.currentTimeMillis() - begin >= msTimeout;
  }

  public void onTimeout(TimeoutException e) {
    responseCallback.fail(e);
  }
}
