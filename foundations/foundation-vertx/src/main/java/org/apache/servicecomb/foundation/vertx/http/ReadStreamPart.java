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

import java.util.concurrent.CompletableFuture;

import javax.servlet.http.Part;

import org.apache.servicecomb.foundation.common.part.AbstractPart;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

/**
 * this is not a really part type, all method extend from AbstractPart is undefined except:<br>
 * 1.getContentType<br>
 * 2.getSubmittedFileName<br>
 * extend from AbstractPart just because want to make it be Part type,
 * so that can be sent by 
 * {@link org.apache.servicecomb.foundation.vertx.http.VertxServerResponseToHttpServletResponse#sendPart(Part) VertxServerResponseToHttpServletResponse.sendPart}
 */
public class ReadStreamPart extends AbstractPart {
  private ReadStream<Buffer> readStream;

  public ReadStreamPart(ReadStream<Buffer> readStream) {
    this.readStream = readStream;

    readStream.pause();
  }

  public CompletableFuture<Void> saveToWriteStream(WriteStream<Buffer> writeStream) {
    CompletableFuture<Void> future = new CompletableFuture<>();

    writeStream.exceptionHandler(future::completeExceptionally);
    readStream.exceptionHandler(future::completeExceptionally);
    readStream.endHandler(future::complete);

    // if readStream(HttpClientResponse) and writeStream(HttpServerResponse)
    // belongs to difference eventloop
    // maybe will cause deadlock
    // if happened, vertx will print deadlock stacks
    Pump.pump(readStream, writeStream).start();
    readStream.resume();

    return future;
  }
}
