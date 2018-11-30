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

import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.foundation.vertx.tcp.TcpOutputStream;

public abstract class AbstractTcpClientPackage {
  private static AtomicLong reqId = new AtomicLong();

  public static long getAndIncRequestId() {
    return reqId.getAndIncrement();
  }

  private long finishWriteToBuffer;

  protected long msgId = getAndIncRequestId();

  public long getMsgId() {
    return msgId;
  }

  public long getFinishWriteToBuffer() {
    return finishWriteToBuffer;
  }

  public void finishWriteToBuffer() {
    this.finishWriteToBuffer = System.nanoTime();
  }

  public abstract TcpOutputStream createStream();
}
