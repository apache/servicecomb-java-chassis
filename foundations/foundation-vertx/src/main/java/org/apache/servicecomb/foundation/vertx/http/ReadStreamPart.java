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

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.servlet.http.Part;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.foundation.common.http.HttpUtils;
import org.apache.servicecomb.foundation.common.part.AbstractPart;
import org.apache.servicecomb.foundation.vertx.stream.PumpCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientResponse;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(ReadStreamPart.class);

  private Context context;

  private ReadStream<Buffer> readStream;

  public ReadStreamPart(Context context, HttpClientResponse httpClientResponse) {
    this(context, (ReadStream<Buffer>) httpClientResponse);

    setSubmittedFileName(
        HttpUtils.parseFileNameFromHeaderValue(httpClientResponse.getHeader(HttpHeaders.CONTENT_DISPOSITION)));

    String contentType = httpClientResponse.getHeader(HttpHeaders.CONTENT_TYPE);
    if (StringUtils.isNotEmpty(contentType)) {
      this.contentType(contentType);
    }
  }

  public ReadStreamPart(Context context, ReadStream<Buffer> readStream) {
    this.context = context;
    this.readStream = readStream;

    readStream.pause();
  }

  public Context getContext() {
    return context;
  }

  public ReadStream<Buffer> getReadStream() {
    return readStream;
  }

  /**
   *
   * @param writeStream
   * @return future of save action<br>
   *
   * important: WriteStream did not provide endHandler, so we can not know when will really finished write.
   * so the return future only means finished read from readStream.
   */
  public CompletableFuture<Void> saveToWriteStream(WriteStream<Buffer> writeStream) {
    return new PumpCommon().pump(context, readStream, writeStream);
  }

  public CompletableFuture<byte[]> saveAsBytes() {
    return saveAs(buf -> buf.getBytes());
  }

  public CompletableFuture<String> saveAsString() {
    return saveAs(buf -> buf.toString());
  }

  public <T> CompletableFuture<T> saveAs(Function<Buffer, T> converter) {
    CompletableFuture<T> future = new CompletableFuture<>();
    Buffer buffer = Buffer.buffer();

    // if readStream.resume() not run on correct eventloop, will:
    //  1.create a context task to save last chunk data to buffer
    //  2.activate connection to read new data
    //  but maybe 2 will run before 1, that will cause lost data or get incorrect data
    context.runOnContext(V -> {
      readStream.exceptionHandler(future::completeExceptionally);
      readStream.handler(buffer::appendBuffer);
      readStream.endHandler(v -> future.complete(converter.apply(buffer)));
      readStream.resume();
    });

    return future;
  }

  /**
   *
   * @param fileName
   * @return future of save to file, future complete means write to file finished
   */
  public CompletableFuture<File> saveToFile(String fileName) {
    File file = new File(fileName);
    file.getParentFile().mkdirs();
    OpenOptions openOptions = new OpenOptions().setCreateNew(true);
    return saveToFile(file, openOptions);
  }

  /**
   *
   * @param file
   * @param openOptions
   * @return future of save to file, future complete means write to file finished
   */
  public CompletableFuture<File> saveToFile(File file, OpenOptions openOptions) {
    CompletableFuture<File> future = new CompletableFuture<>();

    context.runOnContext((v) -> {
      Vertx vertx = context.owner();
      vertx.fileSystem().open(file.getAbsolutePath(), openOptions, ar -> {
        onFileOpened(file, ar, future);
      });
    });

    return future;
  }

  protected void onFileOpened(File file, AsyncResult<AsyncFile> ar, CompletableFuture<File> future) {
    if (ar.failed()) {
      future.completeExceptionally(ar.cause());
      return;
    }

    AsyncFile asyncFile = ar.result();
    CompletableFuture<Void> saveFuture = saveToWriteStream(asyncFile);
    saveFuture.whenComplete((v, saveException) -> {
      asyncFile.close(closeAr -> {
        if (closeAr.failed()) {
          LOGGER.error("Failed to close file {}.", file);
        }

        // whatever close success or failed
        // will not affect to result
        // result just only related to write
        if (saveException == null) {
          future.complete(file);
          return;
        }

        future.completeExceptionally(saveException);
      });
    });
  }
}
