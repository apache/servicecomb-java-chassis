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
package org.apache.servicecomb.foundation.vertx.stream;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

public class PumpImpl<T> implements Pump {

  private final ReadStream<T> readStream;

  private final WriteStream<T> writeStream;

  private final Handler<T> dataHandler;

  private final Handler<Void> drainHandler;

  private int pumped;

  public PumpImpl(ReadStream<T> readStream, WriteStream<T> writeStream, int maxWriteQueueSize) {
    this(readStream, writeStream);
    this.writeStream.setWriteQueueMaxSize(maxWriteQueueSize);
  }

  public PumpImpl(ReadStream<T> readStream, WriteStream<T> writeStream) {
    this.readStream = readStream;
    this.writeStream = writeStream;
    drainHandler = v -> readStream.resume();
    dataHandler = data -> {
      if (data instanceof Buffer) {
        if (((Buffer) data).length() == 0) {
          return;
        }
      }
      writeStream.write(data);
      incPumped();
      if (writeStream.writeQueueFull()) {
        readStream.pause();
        writeStream.drainHandler(drainHandler);
      }
    };
  }


  /**
   * Set the write queue max size to {@code maxSize}
   */
  @Override
  public PumpImpl<T> setWriteQueueMaxSize(int maxSize) {
    writeStream.setWriteQueueMaxSize(maxSize);
    return this;
  }

  /**
   * Start the Pump. The Pump can be started and stopped multiple times.
   */
  @Override
  public PumpImpl<T> start() {
    readStream.handler(dataHandler);
    return this;
  }

  /**
   * Stop the Pump. The Pump can be started and stopped multiple times.
   */
  @Override
  public PumpImpl<T> stop() {
    writeStream.drainHandler(null);
    readStream.handler(null);
    return this;
  }

  /**
   * Return the total number of elements pumped by this pump.
   */
  @Override
  public synchronized int numberPumped() {
    return pumped;
  }

  // Note we synchronize as numberPumped can be called from a different thread however incPumped will always
  // be called from the same thread so we benefit from bias locked optimisation which should give a very low
  // overhead
  private synchronized void incPumped() {
    pumped++;
  }

  public Handler<T> getDataHandler() {
    return dataHandler;
  }
}
