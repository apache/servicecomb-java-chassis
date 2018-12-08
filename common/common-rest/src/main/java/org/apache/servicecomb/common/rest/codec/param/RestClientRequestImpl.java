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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.foundation.vertx.stream.BufferOutputStream;
import org.apache.servicecomb.foundation.vertx.stream.PumpFromPart;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Context;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;

public class RestClientRequestImpl implements RestClientRequest {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestClientRequestImpl.class);

  protected Context context;

  protected AsyncResponse asyncResp;

  // maybe part or list of parts
  private final Map<String, Object> uploads = new HashMap<>();

  protected HttpClientRequest request;

  protected Map<String, String> cookieMap;

  protected Map<String, Object> formMap;

  protected Buffer bodyBuffer;

  public RestClientRequestImpl(HttpClientRequest request, Context context, AsyncResponse asyncResp) {
    this.context = context;
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
  public void attach(String name, Object part) {
    if (null == part) {
      LOGGER.debug("null file is ignored, file name = [{}]", name);
      return;
    }
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
    putHeader(CONTENT_TYPE, MULTIPART_FORM_DATA + "; charset=UTF-8; boundary=" + boundary);

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
            String value = RestObjectMapperFactory.getRestObjectMapper().convertToString(entry.getValue());
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
    Iterator<Entry<String, Object>> uploadsIterator = uploads.entrySet().iterator();
    List<NamePartMap> namePartMaps = new ArrayList<>();
    while (uploadsIterator.hasNext()) {
      Entry<String, Object> entry = uploadsIterator.next();
      String name = entry.getKey();
      Object tmpPart = entry.getValue();
      if (tmpPart instanceof Part) {
        namePartMaps.add(new NamePartMap(name, (Part) tmpPart));
      } else {
        //it's a list
        @SuppressWarnings("unchecked")
        List<Part> partList = (List<Part>) tmpPart;
        partList.forEach(part -> namePartMaps.add(new NamePartMap(name, part)));
      }
    }
    attachFile(boundary, namePartMaps.iterator());
  }

  private void attachFile(String boundary, Iterator<NamePartMap> uploadsIterator) {
    if (!uploadsIterator.hasNext()) {
      request.write(boundaryEndInfo(boundary));
      request.end();
      return;
    }

    NamePartMap next = uploadsIterator.next();
    // do not use part.getName() to get parameter name
    // because pojo consumer not easy to set name to part

    String filename = next.getPart().getSubmittedFileName();

    Buffer fileHeader = fileBoundaryInfo(boundary, next.getName(), next.getPart());
    request.write(fileHeader);

    new PumpFromPart(context, next.getPart()).toWriteStream(request).whenComplete((v, e) -> {
      if (e != null) {
        LOGGER.debug("Failed to sending file [{}:{}].", next.getName(), filename, e);
        asyncResp.consumerFail(e);
        return;
      }

      LOGGER.debug("finish sending file [{}:{}].", next.getName(), filename);
      attachFile(boundary, uploadsIterator);
    });
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
          String value = RestObjectMapperFactory.getRestObjectMapper().convertToString(entry.getValue());
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

  @Override
  public MultiMap getHeaders() {
    return request.headers();
  }

  public static class NamePartMap {
    String name;

    Part part;

    public NamePartMap(String name, Part part) {
      this.name = name;
      this.part = part;
    }

    public String getName() {
      return name;
    }

    public Part getPart() {
      return part;
    }
  }
}
