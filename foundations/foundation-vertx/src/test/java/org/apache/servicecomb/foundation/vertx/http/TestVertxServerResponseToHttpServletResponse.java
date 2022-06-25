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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.Part;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.foundation.common.http.HttpStatus;
import org.apache.servicecomb.foundation.common.part.FilePart;
import org.apache.servicecomb.foundation.vertx.stream.PumpFromPart;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.SyncContext;
import io.vertx.core.streams.WriteStream;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestVertxServerResponseToHttpServletResponse {
  MultiMap headers = MultiMap.caseInsensitiveMultiMap();

  HttpStatus httpStatus = new HttpStatus(123, "default");

  HttpServerResponse serverResponse;

  VertxServerResponseToHttpServletResponse response;

  boolean flushWithBody;

  boolean runOnContextInvoked;

  @Mocked
  Vertx vertx;

  @Mocked
  Context context;

  boolean chunked;

  @Before
  public void setup() {
    serverResponse = new MockUp<HttpServerResponse>() {
      @Mock
      HttpServerResponse setStatusCode(int statusCode) {
        Deencapsulation.setField(httpStatus, "statusCode", statusCode);
        return serverResponse;
      }

      @Mock
      HttpServerResponse setStatusMessage(String statusMessage) {
        Deencapsulation.setField(httpStatus, "reason", statusMessage);
        return serverResponse;
      }

      @Mock
      int getStatusCode() {
        return httpStatus.getStatusCode();
      }

      @Mock
      String getStatusMessage() {
        return httpStatus.getReasonPhrase();
      }

      @Mock
      MultiMap headers() {
        return headers;
      }

      @Mock
      HttpServerResponse putHeader(String name, String value) {
        headers.set(name, value);
        return serverResponse;
      }

      @Mock
      void end() {
        flushWithBody = false;
      }

      @Mock
      void end(Buffer chunk) {
        flushWithBody = true;
      }

      @Mock
      HttpServerResponse setChunked(boolean chunked) {
        TestVertxServerResponseToHttpServletResponse.this.chunked = chunked;
        return serverResponse;
      }

      @Mock
      boolean isChunked() {
        return chunked;
      }
    }.getMockInstance();

    new Expectations() {
      {
        Vertx.currentContext();
        result = context;
      }
    };

    new MockUp<Context>(context) {
      @Mock
      void runOnContext(Handler<Void> action) {
        runOnContextInvoked = true;
        action.handle(null);
      }

      @Mock
      <T> void executeBlocking(Handler<Promise<T>> blockingCodeHandler, boolean ordered,
          Handler<AsyncResult<T>> resultHandler) {
        SyncContext.syncExecuteBlocking(blockingCodeHandler, resultHandler);
      }

      @Mock
      Vertx owner() {
        return vertx;
      }
    };

    response = new VertxServerResponseToHttpServletResponse(serverResponse);
  }

  @Test
  public void construct_invalid() throws IOException {
    new Expectations() {
      {
        Vertx.currentContext();
        result = null;
      }
    };

    NullPointerException exception = Assertions.assertThrows(NullPointerException.class, () -> new VertxServerResponseToHttpServletResponse(serverResponse));
    Assertions.assertEquals("must run in vertx context.", exception.getMessage());
  }

  @Test
  public void setContentType() {
    response.setContentType("json");
    Assertions.assertEquals("json", headers.get(HttpHeaders.CONTENT_TYPE));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void setStatus() {
    response.setStatus(222, "test");
    Assertions.assertEquals(222, httpStatus.getStatusCode());
    Assertions.assertEquals("test", httpStatus.getReasonPhrase());
  }

  @Test
  public void getStatusType() {
    StatusType status = response.getStatusType();

    Assertions.assertSame(status, response.getStatusType());
    Assertions.assertEquals(123, httpStatus.getStatusCode());
    Assertions.assertEquals("default", httpStatus.getReasonPhrase());
  }

  @Test
  public void addHeader() {
    response.addHeader("n1", "v1_1");
    response.addHeader("n1", "v1_2");
    response.addHeader("n2", "v2");

    Assertions.assertEquals(2, headers.size());
    MatcherAssert.assertThat(headers.getAll("n1"), Matchers.contains("v1_1", "v1_2"));
    MatcherAssert.assertThat(headers.getAll("n2"), Matchers.contains("v2"));
  }

  @Test
  public void setHeader() {
    response.setHeader("n1", "v1_1");
    response.setHeader("n1", "v1_2");
    response.setHeader("n2", "v2");

    Assertions.assertEquals(2, headers.size());
    MatcherAssert.assertThat(headers.getAll("n1"), Matchers.contains("v1_2"));
    MatcherAssert.assertThat(headers.getAll("n2"), Matchers.contains("v2"));
  }

  @Test
  public void getStatus() {
    Assertions.assertEquals(123, response.getStatus());
  }

  @Test
  public void getContentType() {
    headers.set(HttpHeaders.CONTENT_TYPE, "json");
    Assertions.assertEquals("json", response.getContentType());
  }

  @Test
  public void getHeader() {
    headers.set(HttpHeaders.CONTENT_TYPE, "json");
    Assertions.assertEquals("json", response.getHeader(HttpHeaders.CONTENT_TYPE));
  }

  @Test
  public void getHeaders() {
    headers.add("h1", "h1_1");
    headers.add("h1", "h1_2");

    MatcherAssert.assertThat(response.getHeaders("h1"), Matchers.contains("h1_1", "h1_2"));
  }

  @Test
  public void getHeaderNames() {
    headers.add("h1", "h1");
    headers.add("h2", "h2");

    MatcherAssert.assertThat(response.getHeaderNames(), Matchers.contains("h1", "h2"));
  }

  @Test
  public void flushBuffer_sameContext() throws IOException {
    response.flushBuffer();

    Assertions.assertFalse(runOnContextInvoked);
  }

  @Test
  public void flushBuffer_diffContext() throws IOException {
    new Expectations() {
      {
        Vertx.currentContext();
        result = null;
      }
    };
    response.flushBuffer();

    Assertions.assertTrue(runOnContextInvoked);
  }

  @Test
  public void internalFlushBufferNoBody() throws IOException {
    response.internalFlushBuffer();

    Assertions.assertFalse(flushWithBody);
  }

  @Test
  public void internalFlushBufferWithBody() throws IOException {
    response.setBodyBuffer(Buffer.buffer());
    response.internalFlushBuffer();

    Assertions.assertTrue(flushWithBody);
  }

  @Test
  public void prepareSendPartHeader_update(@Mocked Part part) {
    new Expectations() {
      {
        part.getContentType();
        result = "type";
        part.getSubmittedFileName();
        result = "测     试";
      }
    };
    DownloadUtils.prepareDownloadHeader(response, part);

    Assertions.assertTrue(serverResponse.isChunked());
    Assertions.assertEquals("type", response.getHeader(HttpHeaders.CONTENT_TYPE));
    Assertions.assertEquals(
        "attachment;filename=%E6%B5%8B%20%20%20%20%20%E8%AF%95;filename*=utf-8''%E6%B5%8B%20%20%20%20%20%E8%AF%95",
        response.getHeader(HttpHeaders.CONTENT_DISPOSITION));
  }

  @Test
  public void prepareSendPartHeader_notUpdate(@Mocked Part part) {
    headers.add(HttpHeaders.CONTENT_LENGTH, "10");
    headers.add(HttpHeaders.CONTENT_TYPE, "type");
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "disposition");

    DownloadUtils.prepareDownloadHeader(response, part);

    Assertions.assertFalse(serverResponse.isChunked());
    Assertions.assertEquals("type", response.getHeader(HttpHeaders.CONTENT_TYPE));
    Assertions.assertEquals("disposition", response.getHeader(HttpHeaders.CONTENT_DISPOSITION));
  }

  @Test
  public void sendPart_openInputStreamFailed(@Mocked Part part)
      throws IOException {
    IOException ioException = new IOException("forbid open stream");
    new Expectations() {
      {
        part.getInputStream();
        result = ioException;
      }
    };

    CompletableFuture<Void> future = response.sendPart(part);

    ExecutionException exception = Assertions.assertThrows(ExecutionException.class, future::get);
    Assertions.assertTrue(exception.getCause() instanceof IOException);
  }

  @Test
  public void sendPart_testPartIsNull(@Mocked Part part) throws InterruptedException, ExecutionException {
    CompletableFuture<Void> future1 = response.sendPart(null);
    Assertions.assertNull(future1.get());
  }

  @Test
  public void sendPart_inputStreamBreak(@Mocked Part part, @Mocked InputStream inputStream)
      throws IOException {
    IOException ioException = new IOException("forbid read");
    new Expectations() {
      {
        part.getInputStream();
        result = inputStream;
        inputStream.read((byte[]) any);
        result = ioException;
      }
    };

    CompletableFuture<Void> future = response.sendPart(part);

    ExecutionException exception = Assertions.assertThrows(ExecutionException.class, future::get);
    Assertions.assertTrue(exception.getCause() instanceof IOException);
  }

  @Test
  public void sendPart_ReadStreamPart(@Mocked ReadStreamPart part) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    new MockUp<PumpFromPart>() {
      @Mock
      CompletableFuture<Void> toWriteStream(WriteStream<Buffer> writeStream, Handler<Throwable> throwableHandler) {
        return future;
      }
    };

    Assertions.assertSame(future, response.sendPart(part));
  }

  @Test
  public void sendPart_succ(@Mocked Part part, @Mocked InputStream inputStream)
      throws IOException, InterruptedException, ExecutionException {
    new Expectations() {
      {
        part.getInputStream();
        result = inputStream;
        inputStream.read((byte[]) any);
        result = -1;
      }
    };

    CompletableFuture<Void> future = response.sendPart(part);

    Assertions.assertNull(future.get());
  }

  @Test
  public void clearPartResource_deleteFile() throws IOException {
    File file = new File("target", UUID.randomUUID() + ".txt");
    FileUtils.write(file, "content", StandardCharsets.UTF_8);
    FilePart part = new FilePart(null, file).setDeleteAfterFinished(true);

    Assertions.assertTrue(file.exists());
    DownloadUtils.clearPartResource(part);
    Assertions.assertFalse(file.exists());
  }

  @Test
  public void clearPartResource_notDeleteFile() throws IOException {
    File file = new File("target", UUID.randomUUID() + ".txt");
    FileUtils.write(file, "content", StandardCharsets.UTF_8);
    FilePart part = new FilePart(null, file);

    Assertions.assertTrue(file.exists());
    DownloadUtils.clearPartResource(part);
    Assertions.assertTrue(file.exists());

    file.delete();
  }
}
