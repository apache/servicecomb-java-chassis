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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.Part;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.foundation.common.http.HttpStatus;
import org.apache.servicecomb.foundation.common.http.HttpUtils;
import org.apache.servicecomb.foundation.common.part.FilePartForSend;
import org.apache.servicecomb.foundation.vertx.stream.InputStreamToReadStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pump;

public class VertxServerResponseToHttpServletResponse extends AbstractHttpServletResponse {
  private static final Logger LOGGER = LoggerFactory.getLogger(VertxServerResponseToHttpServletResponse.class);

  private Context context;

  private HttpServerResponse serverResponse;

  private StatusType statusType;

  public VertxServerResponseToHttpServletResponse(HttpServerResponse serverResponse) {
    this.context = Vertx.currentContext();
    this.serverResponse = serverResponse;

    Objects.requireNonNull(context, "must run in vertx context.");
  }

  @Override
  public void setContentType(String type) {
    serverResponse.headers().set(HttpHeaders.CONTENT_TYPE, type);
  }

  @Override
  public void setStatus(int sc, String sm) {
    serverResponse.setStatusCode(sc);
    serverResponse.setStatusMessage(sm);
  }

  @Override
  public StatusType getStatusType() {
    if (statusType == null) {
      statusType = new HttpStatus(serverResponse.getStatusCode(), serverResponse.getStatusMessage());
    }
    return statusType;
  }

  @Override
  public void addHeader(String name, String value) {
    serverResponse.headers().add(name, value);
  }

  @Override
  public void setHeader(String name, String value) {
    serverResponse.headers().set(name, value);
  }

  @Override
  public int getStatus() {
    return serverResponse.getStatusCode();
  }

  @Override
  public String getContentType() {
    return serverResponse.headers().get(HttpHeaders.CONTENT_TYPE);
  }

  @Override
  public String getHeader(String name) {
    return serverResponse.headers().get(name);
  }

  @Override
  public Collection<String> getHeaders(String name) {
    return serverResponse.headers().getAll(name);
  }

  @Override
  public Collection<String> getHeaderNames() {
    return serverResponse.headers().names();
  }

  @Override
  public void flushBuffer() throws IOException {
    if (context == Vertx.currentContext()) {
      internalFlushBuffer();
      return;
    }

    context.runOnContext(V -> {
      internalFlushBuffer();
    });
  }

  public void internalFlushBuffer() {
    if (bodyBuffer == null) {
      serverResponse.end();
      return;
    }

    serverResponse.end(bodyBuffer);
  }

  @Override
  public CompletableFuture<Void> sendPart(Part part) {
    prepareSendPartHeader(part);

    if (ReadStreamPart.class.isInstance(part)) {
      return ((ReadStreamPart) part).saveToWriteStream(this.serverResponse);
    }

    CompletableFuture<Void> future = new CompletableFuture<Void>();
    try {
      InputStream is = part.getInputStream();
      context.runOnContext(v -> {
        InputStreamToReadStream inputStreamToReadStream = new InputStreamToReadStream(context.owner(), is);
        inputStreamToReadStream.exceptionHandler(t -> {
          clearPartResource(part, is);
          future.completeExceptionally(t);
        });
        inputStreamToReadStream.endHandler(V -> {
          clearPartResource(part, is);
          future.complete(null);
        });
        Pump.pump(inputStreamToReadStream, serverResponse).start();
      });
    } catch (IOException e) {
      future.completeExceptionally(e);
    }

    return future;
  }

  protected void prepareSendPartHeader(Part part) {
    if (!serverResponse.headers().contains(HttpHeaders.CONTENT_LENGTH)) {
      serverResponse.setChunked(true);
    }

    if (!serverResponse.headers().contains(HttpHeaders.CONTENT_TYPE)) {
      serverResponse.putHeader(HttpHeaders.CONTENT_TYPE, part.getContentType());
    }

    if (!serverResponse.headers().contains(HttpHeaders.CONTENT_DISPOSITION)) {
      // to support chinese and space filename in firefox
      // must use "filename*", (https://tools.ietf.org/html/rtf6266)
      String encodedFileName = HttpUtils.uriEncodePath(part.getSubmittedFileName());
      serverResponse.putHeader(HttpHeaders.CONTENT_DISPOSITION,
          "attachment;filename=" + encodedFileName + ";filename*=utf-8''" + encodedFileName);
    }
  }

  protected void clearPartResource(Part part, InputStream is) {
    IOUtils.closeQuietly(is);
    if (FilePartForSend.class.isInstance(part) && ((FilePartForSend) part).isDeleteAfterFinished()) {
      try {
        part.delete();
      } catch (IOException e) {
        LOGGER.error("Failed to delete temp file: {}.", ((FilePartForSend) part).getAbsolutePath(), e);
      }
    }
  }
}
