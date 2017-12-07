/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.common.rest.codec.param;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.MediaType;

import io.servicecomb.common.rest.codec.RestClientRequest;
import io.servicecomb.common.rest.codec.RestObjectMapper;
import io.servicecomb.foundation.vertx.stream.BufferOutputStream;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VoidHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;

public class RestClientRequestImpl implements RestClientRequest {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestClientRequestImpl.class);

  private final Map<String, String> uploads = new HashMap<>();
  private final Vertx vertx;
  protected HttpClientRequest request;

  protected Map<String, String> cookieMap;

  protected Map<String, Object> formMap;

  protected Buffer bodyBuffer;

  public RestClientRequestImpl(HttpClientRequest request, Vertx vertx) {
    this.request = request;
    this.vertx = vertx;
  }

  @Override
  public void write(Buffer bodyBuffer) {
    this.bodyBuffer = bodyBuffer;
  }

  @Override
  public Buffer getBodyBuffer() throws Exception {
    genBodyBuffer();
    return bodyBuffer;
  }

  @Override
  public void attach(String name, String filename) {
    uploads.put(name, filename);
  }

  @Override
  public void end() throws Exception {
    writeCookies();

    if (!uploads.isEmpty()) {
      attachFiles();
      return;
    }

    genBodyBuffer();

    if (bodyBuffer == null) {
      request.end();
      return;
    }

    request.end(bodyBuffer);
  }

  private void attachFiles() {
    if (!uploads.isEmpty()) {
      String boundary = "fileUploadBoundary" + UUID.randomUUID().toString();
      putHeader(CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + boundary);

      List<CompletableFuture<Void>> fileCloseFutures = new ArrayList<>(uploads.size());

      final long[] totalSize = {0L};
      final CompletableFuture<?>[] fileOpenFuture = {CompletableFuture.completedFuture(null)};

      uploads.forEach((name, filename) -> {
        CompletableFuture<Void> fileCloseFuture = new CompletableFuture<>();
        fileCloseFutures.add(fileCloseFuture);

        Buffer buffer = fileBoundaryInfo(boundary, name, filename);
        totalSize[0] += combinedFileSize(filename, buffer);

        vertx.fileSystem()
            .open(filename, readOnlyOption(), result -> {
              AsyncFile file = result.result();
              fileOpenFuture[0] = appendFileAsync(fileOpenFuture[0], fileCloseFuture, buffer, file);
              file.endHandler(closeFileHandler(name, filename, file, fileCloseFuture));
            });
      });

      request.headers()
          .set("Content-Length", String.valueOf(totalSize[0] + ("--" + boundary + "--\r\n").getBytes().length));

      CompletableFuture.allOf(fileCloseFutures.toArray(new CompletableFuture<?>[fileCloseFutures.size()]))
          .thenRunAsync(() -> {
            Pump.pump(embeddedReadStream(boundaryEndInfo(boundary)), request).start();
            request.end();
          });
    }
  }

  private VoidHandler closeFileHandler(String name,
      String filename,
      AsyncFile file,
      CompletableFuture<Void> fileCloseFuture) {
    return new VoidHandler() {
      public void handle() {
        file.close(ar -> {
          if (ar.succeeded()) {
            fileCloseFuture.complete(null);
            LOGGER.debug("Sent file [{}:{}] successfully", name, filename);
          } else {
            fileCloseFuture.completeExceptionally(ar.cause());
            LOGGER.error("Failed to send file [{}:{}]", name, filename, ar.cause());
          }
        });
      }
    };
  }

  private CompletableFuture<Void> appendFileAsync(
      CompletableFuture<?> fileOpenFuture,
      CompletableFuture<Void> fileCloseFuture,
      Buffer fileHeader,
      AsyncFile file) {

    return fileOpenFuture.thenRunAsync(() -> {
      Pump.pump(embeddedReadStream(fileHeader), request).start();
      Pump.pump(file, request).start();

      // ensure file sent completely, before proceeding to the next file
      fileCloseFuture.join();
    });
  }

  private long combinedFileSize(String filename, Buffer buffer) {
    long size;
    try {
      size = Files.size(Paths.get(filename)) + buffer.length();
    } catch (IOException e) {
      throw new IllegalStateException("No such file: " + filename, e);
    }
    return size;
  }

  private OpenOptions readOnlyOption() {
    return new OpenOptions()
        .setCreate(false)
        .setWrite(false);
  }

  private Buffer boundaryEndInfo(String boundary) {
    return Buffer.buffer()
        .appendString("\r\n")
        .appendString("--" + boundary + "--\r\n");
  }

  private static ReadStream<Buffer> embeddedReadStream(final Buffer buffer) {
    return new ReadStream<Buffer>() {
      @Override
      public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        return this;
      }

      @Override
      public ReadStream<Buffer> handler(Handler<Buffer> handler) {
        handler.handle(buffer);
        return this;
      }

      @Override
      public ReadStream<Buffer> pause() {
        return this;
      }

      @Override
      public ReadStream<Buffer> resume() {
        return this;
      }

      @Override
      public ReadStream<Buffer> endHandler(Handler<Void> handler) {
        return this;
      }
    };
  }

  private Buffer fileBoundaryInfo(String boundary, String name, String filename) {
    Buffer buffer = Buffer.buffer();
    buffer.appendString("\r\n");
    buffer.appendString("--" + boundary + "\r\n");
    buffer.appendString("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n");
    buffer.appendString("Content-Type: multipart/form-data\r\n");
    buffer.appendString("Content-Transfer-Encoding: binary\r\n");
    buffer.appendString("\r\n");
    return buffer;
  }

  private void genBodyBuffer() throws Exception {
    if (bodyBuffer != null) {
      return;
    }

    if (formMap == null) {
      return;
    }

    request.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
    try (BufferOutputStream output = new BufferOutputStream()) {
      for (Entry<String, Object> entry : formMap.entrySet()) {
        output.write(entry.getKey().getBytes(StandardCharsets.UTF_8));
        output.write('=');
        if (entry.getValue() != null) {
          String value = RestObjectMapper.INSTANCE.convertToString(entry.getValue());
          value = URLEncoder.encode(value, StandardCharsets.UTF_8.name());
          output.write(value.getBytes(StandardCharsets.UTF_8));
        }
        output.write('&');
      }
      bodyBuffer = output.getBuffer();
    }
  }

  private void writeCookies() {
    if (cookieMap == null) {
      return;
    }

    StringBuilder builder = new StringBuilder();
    for (Entry<String, String> entry : cookieMap.entrySet()) {
      builder.append(entry.getKey())
          .append('=')
          .append(entry.getValue())
          .append("; ");
    }
    request.putHeader(HttpHeaders.COOKIE, builder.toString());
  }

  @Override
  public void addCookie(String name, String value) {
    if (cookieMap == null) {
      cookieMap = new HashMap<>();
    }

    cookieMap.put(name, value);
  }

  @Override
  public void addForm(String name, Object value) {
    if (formMap == null) {
      formMap = new HashMap<>();
    }

    if (value != null) {
      formMap.put(name, value);
    }
  }

  @Override
  public void putHeader(String name, String value) {
    request.putHeader(name, value);
  }
}
