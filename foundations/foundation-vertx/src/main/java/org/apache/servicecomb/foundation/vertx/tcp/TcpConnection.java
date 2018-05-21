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
package org.apache.servicecomb.foundation.vertx.tcp;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.vertx.core.Context;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.NetSocketImpl;
import io.vertx.core.net.impl.VertxHelper;

public class TcpConnection {
  protected String protocol;

  // 压缩算法名字
  protected String zipName;

  protected NetSocket netSocket;

  // context of netSocket
  protected Context context;

  // vertx's wrap for netty is ugly: write always lock first
  // so we save buf in a CAS queue, and notify eventloop thread to do the real write
  //
  // netty:
  // if write caller is not in network IO thread
  // always wrap the write as a task, and put it to IO thread queue(MPSC queue)
  //
  // so this optimization:
  // 1.avoid vertx's lock
  // 2.reduce netty's task schedule
  private Queue<ByteBuf> writeQueue = new ConcurrentLinkedQueue<>();

  private AtomicLong writeQueueSize = new AtomicLong();

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getZipName() {
    return zipName;
  }

  public void setZipName(String zipName) {
    this.zipName = zipName;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public NetSocket getNetSocket() {
    return netSocket;
  }

  public void initNetSocket(NetSocketImpl netSocket) {
    this.netSocket = netSocket;

    this.context = VertxHelper.getConnectionContext(netSocket);
  }

  public void write(ByteBuf buf) {
    writeQueue.add(buf);
    long oldSize = writeQueueSize.getAndIncrement();
    if (oldSize == 0) {
      scheduleWrite();
    }
  }

  // notify context thread to write
  protected void scheduleWrite() {
    context.runOnContext(v -> {
      writeInContext();
    });
  }

  protected void writeInContext() {
    CompositeByteBuf cbb = ByteBufAllocator.DEFAULT.compositeBuffer();
    for (; ; ) {
      ByteBuf buf = writeQueue.poll();
      if (buf == null) {
        break;
      }

      writeQueueSize.decrementAndGet();
      cbb.addComponent(true, buf);

      if (cbb.numComponents() == cbb.maxNumComponents()) {
        netSocket.write(Buffer.buffer(cbb));
        cbb = ByteBufAllocator.DEFAULT.compositeBuffer();
      }
    }
    if (cbb.isReadable()) {
      netSocket.write(Buffer.buffer(cbb));
    }
  }
}
