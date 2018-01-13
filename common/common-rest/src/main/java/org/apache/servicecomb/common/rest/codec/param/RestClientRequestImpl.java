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

package org.apache.servicecomb.common.rest.codec.param;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapper;
import org.apache.servicecomb.foundation.vertx.stream.BufferOutputStream;
import org.apache.servicecomb.foundation.vertx.stream.InputStreamToReadStream;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.streams.Pump;

public class RestClientRequestImpl implements RestClientRequest {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestClientRequestImpl.class);

  protected Vertx vertx;

  protected AsyncResponse asyncResp;

  private final Map<String, Part> uploads = new HashMap<>();

  protected HttpClientRequest request;

  protected Map<String, String> cookieMap;

  protected Map<String, Object> formMap;

  protected Buffer bodyBuffer;

  public RestClientRequestImpl(HttpClientRequest request, Vertx vertx, AsyncResponse asyncResp) {
    this.vertx = vertx;
    this.asyncResp = asyncResp;
    this.request = request;
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
  public void attach(String name, Part part) {
    uploads.put(name, part);
  }

  @Override
  public void end() {
    writeCookies();

    if (!uploads.isEmpty()) {
      doEndWithUpload();
      return;
    }

    doEndNormal();
  }

  protected void doEndWithUpload() {
    request.setChunked(true);

    String boundary = "boundary" + UUID.randomUUID().toString();
    putHeader(CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + boundary);

    genBodyForm(boundary);

    attachFiles(boundary);
  }

  private void genBodyForm(String boundary) {
    if (formMap == null) {
      return;
    }

    try {
      try (BufferOutputStream output = new BufferOutputStream()) {
        for (Entry<String, Object> entry : formMap.entrySet()) {
          output.write(bytesOf("\r\n"));
          output.write(bytesOf("--" + boundary + "\r\n"));
          output.write(bytesOf("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n"));
          if (entry.getValue() != null) {
            String value = RestObjectMapper.INSTANCE.convertToString(entry.getValue());
            output.write(value.getBytes(StandardCharsets.UTF_8));
          }
        }
        request.write(output.getBuffer());
      }
    } catch (Exception e) {
      asyncResp.consumerFail(e);
    }
  }

  private byte[] bytesOf(String string) {
    return string.getBytes(StandardCharsets.UTF_8);
  }

  protected void doEndNormal() {
    try {
      genBodyBuffer();
    } catch (Exception e) {
      asyncResp.consumerFail(e);
      return;
    }

    if (bodyBuffer == null) {
      request.end();
      return;
    }

    request.end(bodyBuffer);
  }

  private void attachFiles(String boundary) {
    Iterator<Entry<String, Part>> uploadsIterator = uploads.entrySet().iterator();
    attachFile(boundary, uploadsIterator);
  }

  private void attachFile(String boundary, Iterator<Entry<String, Part>> uploadsIterator) {
    if (!uploadsIterator.hasNext()) {
      request.write(boundaryEndInfo(boundary));
      request.end();
      return;
    }

    Entry<String, Part> entry = uploadsIterator.next();
    // do not use part.getName() to get parameter name
    // because pojo consumer not easy to set name to part
    String name = entry.getKey();
    Part part = entry.getValue();
    String filename = part.getSubmittedFileName();

    InputStreamToReadStream fileStream = null;
    try {
      fileStream = new InputStreamToReadStream(vertx, part.getInputStream());
    } catch (IOException e) {
      asyncResp.consumerFail(e);
      return;
    }

    InputStreamToReadStream finalFileStream = fileStream;
    fileStream.exceptionHandler(e -> {
      LOGGER.debug("Failed to sending file [{}:{}].", name, filename, e);
      IOUtils.closeQuietly(finalFileStream.getInputStream());
      asyncResp.consumerFail(e);
    });
    fileStream.endHandler(V -> {
      LOGGER.debug("finish sending file [{}:{}].", name, filename);
      IOUtils.closeQuietly(finalFileStream.getInputStream());

      attachFile(boundary, uploadsIterator);
    });

    Buffer fileHeader = fileBoundaryInfo(boundary, name, part);
    request.write(fileHeader);
    Pump.pump(fileStream, request).start();
  }

  private Buffer boundaryEndInfo(String boundary) {
    return Buffer.buffer()
        .appendString("\r\n")
        .appendString("--" + boundary + "--\r\n");
  }

  protected Buffer fileBoundaryInfo(String boundary, String name, Part part) {
    Buffer buffer = Buffer.buffer();
    buffer.appendString("\r\n");
    buffer.appendString("--" + boundary + "\r\n");
    buffer.appendString("Content-Disposition: form-data; name=\"")
        .appendString(name)
        .appendString("\"; filename=\"")
        .appendString(part.getSubmittedFileName() != null ? part.getSubmittedFileName() : "null")
        .appendString("\"\r\n");
    buffer.appendString("Content-Type: ").appendString(part.getContentType()).appendString("\r\n");
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
