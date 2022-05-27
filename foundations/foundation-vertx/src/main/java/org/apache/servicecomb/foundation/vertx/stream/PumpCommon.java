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

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.foundation.common.io.AsyncCloseable;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

public class PumpCommon {
  /**
   *
   * @param context
   * @param readStream
   * @param writeStream
   * @return future of save action<br>
   * <p>important:
   * <p>  if writeStream is AsyncCloseable, future means write complete
   * <p>  if writeStream is not AsyncCloseable, future only means read complete
   */
  @SuppressWarnings("unchecked")
  public CompletableFuture<Void> pump(Context context, ReadStream<Buffer> readStream, WriteStream<Buffer> writeStream,
      Handler<Throwable> throwableHandler) {
    CompletableFuture<Void> readFuture = new CompletableFuture<>();

    writeStream.exceptionHandler(e -> {
      // consumer -> producer
      // 3rd consumer -> edge -> producer
      // when download not finished, consumer stop download
      // producer should stop download logic at once
      if (readStream instanceof InputStreamToReadStream) {
        ((InputStreamToReadStream) readStream).handleException(e);
      } else if (readStream instanceof HttpClientResponse) {
        // can not find a way to cancel/terminate request
        // so can only close the connection.
        ((HttpClientResponse) readStream).request().connection().close();
      }
      if (throwableHandler != null) {
        throwableHandler.handle(e);
      }
      readFuture.completeExceptionally(e);
    });
    readStream.exceptionHandler(readFuture::completeExceptionally);
    // just means read finished, not means write finished
    readStream.endHandler(readFuture::complete);

    // if readStream(HttpClientResponse) and writeStream(HttpServerResponse)
    // belongs to difference eventloop
    // maybe will cause deadlock
    // if happened, vertx will print deadlock stacks
    Pump.pump(readStream, writeStream).start();
    context.runOnContext(v -> readStream.resume());

    if (!AsyncCloseable.class.isInstance(writeStream)) {
      return readFuture;
    }

    return closeWriteStream((AsyncCloseable<Void>) writeStream, readFuture);
  }

  protected CompletableFuture<Void> closeWriteStream(AsyncCloseable<Void> writeStream,
      CompletableFuture<Void> readFuture) {
    CompletableFuture<Void> writeFuture = new CompletableFuture<>();
    readFuture.whenComplete((v, e) ->
        writeStream.close().whenComplete((wv, we) -> {
          if (we != null) {
            writeFuture.completeExceptionally(we);
            return;
          }

          writeFuture.complete(null);
        })
    );

    return CompletableFuture.allOf(readFuture, writeFuture);
  }
}
