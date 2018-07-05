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
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.foundation.vertx.stream.InputStreamToReadStream;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.SyncVertx;
import io.vertx.core.streams.WriteStream;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestReadStreamPart {
  static SyncVertx vertx = new SyncVertx();

  static ContextImpl context = vertx.getContext();

  static String src = "src";

  static InputStream inputStream = new ByteArrayInputStream(src.getBytes());

  InputStreamToReadStream readStream = new InputStreamToReadStream(context, inputStream, true);

  ReadStreamPart part = new ReadStreamPart(context, readStream);

  @Rule
  public ExpectedException expectedException = ExpectedException.none();


  @Before
  public void setup() throws IOException {
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

    Assert.assertEquals("name.txt", part.getSubmittedFileName());
    Assert.assertEquals("text/plain", part.getContentType());
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

    Assert.assertEquals("name.txt", part.getSubmittedFileName());
    Assert.assertEquals("type", part.getContentType());
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

  @Test
  public void saveAsBytes() throws InterruptedException, ExecutionException {
    Assert.assertArrayEquals(src.getBytes(), part.saveAsBytes().get());
  }

  @Test
  public void saveAsString() throws InterruptedException, ExecutionException {
    Assert.assertEquals(src, part.saveAsString().get());
  }

  @Test
  public void saveToFile() throws InterruptedException, ExecutionException, IOException {
    File dir = new File("target/notExist-" + UUID.randomUUID().toString());
    File file = new File(dir, "a.txt");

    Assert.assertFalse(dir.exists());

    part.saveToFile(file.getAbsolutePath()).get();

    Assert.assertEquals(src, FileUtils.readFileToString(file));

    FileUtils.forceDelete(dir);
    Assert.assertFalse(dir.exists());
  }

  @Test
  public void saveToFile_notExist_notCreate() throws InterruptedException, ExecutionException, IOException {
    File dir = new File("target/notExist-" + UUID.randomUUID().toString());
    File file = new File(dir, "a.txt");

    Assert.assertFalse(dir.exists());

    expectedException.expect(ExecutionException.class);
    expectedException.expectCause(Matchers.instanceOf(FileSystemException.class));

    OpenOptions openOptions = new OpenOptions().setCreateNew(false);
    part.saveToFile(file, openOptions).get();
  }
}
