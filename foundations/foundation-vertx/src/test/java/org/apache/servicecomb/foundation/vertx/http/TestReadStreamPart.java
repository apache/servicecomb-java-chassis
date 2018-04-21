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
package org.apache.servicecomb.foundation.vertx.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.foundation.vertx.stream.InputStreamToReadStream;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestReadStreamPart {
  @Mocked
  Vertx vertx;

  String src = "src";

  InputStreamToReadStream readStream;

  ReadStreamPart part;

  InputStream inputStream = new ByteArrayInputStream(src.getBytes());

  @Rule
  public ExpectedException expectedException = ExpectedException.none();


  @Before
  public void setup() {
    readStream = new InputStreamToReadStream(vertx, inputStream);
    part = new ReadStreamPart(readStream);

    new MockUp<Vertx>(vertx) {
      @Mock
      <T> void executeBlocking(Handler<Future<T>> blockingCodeHandler, boolean ordered,
          Handler<AsyncResult<T>> resultHandler) {
        Future<T> future = Future.future();
        blockingCodeHandler.handle(future);
        future.setHandler(resultHandler);
      }
    };
  }

  @Test
  public void saveToWriteStream() throws InterruptedException, ExecutionException {
    Buffer buf = Buffer.buffer();
    WriteStream<Buffer> writeStream = new MockUp<WriteStream<Buffer>>() {
      @Mock
      WriteStream<Buffer> write(Buffer data) {
        buf.appendBuffer(data);
        return null;
      }
    }.getMockInstance();

    part.saveToWriteStream(writeStream).get();

    Assert.assertEquals(src, buf.toString());
  }

  @Test
  public void saveToWriteStream_writeException() throws InterruptedException, ExecutionException {
    Error error = new Error();
    WriteStream<Buffer> writeStream = new MockUp<WriteStream<Buffer>>() {
      Handler<Throwable> exceptionHandler;

      @Mock
      WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        return null;
      }

      @Mock
      WriteStream<Buffer> write(Buffer data) {
        exceptionHandler.handle(error);
        return null;
      }
    }.getMockInstance();

    expectedException.expect(ExecutionException.class);
    expectedException.expectCause(Matchers.sameInstance(error));

    part.saveToWriteStream(writeStream).get();
  }

  @Test
  public void saveToWrite_readException(@Mocked WriteStream<Buffer> writeStream)
      throws InterruptedException, ExecutionException {
    Error error = new Error();
    new MockUp<InputStream>(inputStream) {
      @Mock
      int read(byte b[]) throws IOException {
        throw error;
      }
    };

    expectedException.expect(ExecutionException.class);
    expectedException.expectCause(Matchers.sameInstance(error));

    part.saveToWriteStream(writeStream).get();
  }
}
