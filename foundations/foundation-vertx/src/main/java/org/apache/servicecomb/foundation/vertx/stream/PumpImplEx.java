/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

/*
 *  Froked from https://github.com/eclipse-vertx/vert.x/blob/master/src/main/java/io/vertx/core/streams/impl/PumpImpl.java
 *
 */
package org.apache.servicecomb.foundation.vertx.stream;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

public class PumpImplEx<T> implements Pump {

  private final ReadStream<T> readStream;

  private final WriteStream<T> writeStream;

  private final Handler<T> dataHandler;

  private final Handler<Void> drainHandler;

  private int pumped;

  public PumpImplEx(ReadStream<T> readStream, WriteStream<T> writeStream, int maxWriteQueueSize) {
    this(readStream, writeStream);
    this.writeStream.setWriteQueueMaxSize(maxWriteQueueSize);
  }

  public PumpImplEx(ReadStream<T> readStream, WriteStream<T> writeStream) {
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


  @Override
  public PumpImplEx<T> setWriteQueueMaxSize(int maxSize) {
    writeStream.setWriteQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public PumpImplEx<T> start() {
    readStream.handler(dataHandler);
    return this;
  }

  @Override
  public PumpImplEx<T> stop() {
    writeStream.drainHandler(null);
    readStream.handler(null);
    return this;
  }


  @Override
  public synchronized int numberPumped() {
    return pumped;
  }


  private synchronized void incPumped() {
    pumped++;
  }

  public Handler<T> getDataHandler() {
    return dataHandler;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static PumpImplEx getPumpImplEx(ReadStream rs, WriteStream ws) {
    return new PumpImplEx(rs, ws);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static PumpImplEx getPumpImplEx(ReadStream rs, WriteStream ws, int writeQueueMaxSize) {
    return new PumpImplEx(rs, ws, writeQueueMaxSize);
  }
}
