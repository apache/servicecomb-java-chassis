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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.vertx.stream.InputStreamToReadStream;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.impl.SyncContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.streams.WriteStream;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestReadStreamPart {
  static Vertx vertx = Vertx.vertx();

  static SyncContext context = new SyncContext();

  static String src = "src";

  static InputStream inputStream = new ByteArrayInputStream(src.getBytes());

  InputStreamToReadStream readStream = new InputStreamToReadStream(context, inputStream, true);

  ReadStreamPart part = new ReadStreamPart(context, readStream);

  @Before
  public void setup() throws IOException {
    context.setOwner((VertxInternal) vertx);
    inputStream.reset();
  }

  @AfterClass
  public static void teardown() {
    vertx.close();
  }

  @Test
  public void constructFromHttpClientResponse_noContentType(@Mocked HttpClientResponse httpClientResponse) {
    new Expectations() {
      {
        httpClientResponse.getHeader(HttpHeaders.CONTENT_DISPOSITION);
        result = "xx;filename=name.txt";
        httpClientResponse.getHeader(HttpHeaders.CONTENT_TYPE);
        result = null;
      }
    };

    part = new ReadStreamPart(context, httpClientResponse);

    Assertions.assertEquals("name.txt", part.getSubmittedFileName());
    Assertions.assertEquals("text/plain", part.getContentType());
  }

  @Test
  public void constructFromHttpClientResponse_hasContentType(@Mocked HttpClientResponse httpClientResponse) {
    new Expectations() {
      {
        httpClientResponse.getHeader(HttpHeaders.CONTENT_DISPOSITION);
        result = "xx;filename=name.txt";
        httpClientResponse.getHeader(HttpHeaders.CONTENT_TYPE);
        result = "type";
      }
    };

    part = new ReadStreamPart(context, httpClientResponse);

    Assertions.assertEquals("name.txt", part.getSubmittedFileName());
    Assertions.assertEquals("type", part.getContentType());
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

    Assertions.assertEquals(src, buf.toString());
  }

  @Test
  public void saveToWriteStream_writeException() throws InterruptedException, ExecutionException {
    RuntimeException error = new RuntimeExceptionWithoutStackTrace();
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

    ExecutionException exception = Assertions.assertThrows(ExecutionException.class, () -> part.saveToWriteStream(writeStream).get());
    Assertions.assertTrue(exception.getCause() instanceof RuntimeException);
  }

  @Test
  public void saveToWrite_readException(@Mocked WriteStream<Buffer> writeStream) {
    RuntimeException error = new RuntimeExceptionWithoutStackTrace();
    new MockUp<InputStream>(inputStream) {
      @Mock
      int read(byte[] b) throws IOException {
        throw error;
      }
    };

    ExecutionException exception = Assertions.assertThrows(ExecutionException.class, () -> part.saveToWriteStream(writeStream).get());
    Assertions.assertTrue(exception.getCause() instanceof RuntimeException);
  }

  @Test
  public void saveAsBytes() throws InterruptedException, ExecutionException {
    Assertions.assertArrayEquals(src.getBytes(), part.saveAsBytes().get());
  }

  @Test
  public void saveAsString() throws InterruptedException, ExecutionException {
    Assertions.assertEquals(src, part.saveAsString().get());
  }

  @Test
  public void saveToFile() throws InterruptedException, ExecutionException, IOException {
    File dir = new File("target/notExist-" + UUID.randomUUID());
    File file = new File(dir, "a.txt");

    Assertions.assertFalse(dir.exists());

    part.saveToFile(file.getAbsolutePath()).get();

    Assertions.assertEquals(src, FileUtils.readFileToString(file, StandardCharsets.UTF_8));

    FileUtils.forceDelete(dir);
    Assertions.assertFalse(dir.exists());
  }

  @Test
  public void saveToFile_notExist_notCreate() throws InterruptedException, ExecutionException, IOException {
    File dir = new File("target/notExist-" + UUID.randomUUID());
    File file = new File(dir, "a.txt");

    Assertions.assertFalse(dir.exists());

    ExecutionException exception = Assertions.assertThrows(ExecutionException.class, () -> {
      OpenOptions openOptions = new OpenOptions().setCreateNew(false);
      part.saveToFile(file, openOptions).get();
    });
    Assertions.assertTrue(exception.getCause() instanceof FileSystemException);
  }
}
