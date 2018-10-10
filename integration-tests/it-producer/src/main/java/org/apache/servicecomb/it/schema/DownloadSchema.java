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
package org.apache.servicecomb.it.schema;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.foundation.common.part.FilePart;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestSchema(schemaId = "download")
@RequestMapping(path = "/base/v1//download")
public class DownloadSchema implements BootListener {
  File tempDir = new File("target/downloadTemp");

  HttpServer server;

  public DownloadSchema() throws IOException {
    FileUtils.deleteQuietly(tempDir);
    FileUtils.forceMkdir(tempDir);

    // for download from net stream case
    server = ServerBootstrap
        .bootstrap()
        .setListenerPort(0)
        .registerHandler("/download/netInputStream", (req, resp, context) -> {
          String uri = req.getRequestLine().getUri();
          String query = URI.create(uri).getQuery();
          int idx = query.indexOf('=');
          String content = query.substring(idx + 1);
          content = URLDecoder.decode(content, StandardCharsets.UTF_8.name());
          resp.setEntity(new StringEntity(content, StandardCharsets.UTF_8.name()));
        }).create();
    server.start();
  }

  @Override
  public void onBootEvent(BootEvent event) {
    if (EventType.AFTER_CLOSE.equals(event.getEventType())) {
      server.stop();
    }
  }

  protected File createTempFile(String content) throws IOException {
    return createTempFile(null, content);
  }

  protected File createTempFile(String name, String content) throws IOException {
    if (name == null) {
      name = "download-" + UUID.randomUUID().toString() + ".txt";
    }
    File file = new File(tempDir, name);
    FileUtils.write(file, content);
    return file;
  }

  // customize HttpHeaders.CONTENT_DISPOSITION to be "attachment;filename=tempFileEntity.txt"
  @GetMapping(path = "/tempFileEntity")
  public ResponseEntity<Part> tempFileEntity(String content) throws IOException {
    File file = createTempFile(content);

    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=tempFileEntity.txt")
        .body(new FilePart(null, file)
            .setDeleteAfterFinished(true));
  }

  // generate HttpHeaders.CONTENT_DISPOSITION to be "attachment;filename=tempFilePart.txt" automatically
  @GetMapping(path = "/tempFilePart")
  public Part tempFilePart(String content) throws IOException {
    File file = createTempFile(content);

    return new FilePart(null, file)
        .setDeleteAfterFinished(true)
        .setSubmittedFileName("tempFilePart.txt");
  }

  @GetMapping(path = "/file")
  public File file(String content) throws IOException {
    return createTempFile("file.txt", content);
  }

  @GetMapping(path = "/chineseAndSpaceFile")
  public Part chineseAndSpaceFile(String content) throws IOException {
    File file = createTempFile(content);
    return new FilePart(null, file)
        .setDeleteAfterFinished(true)
        .setSubmittedFileName("测 试.test.txt");
  }

  @GetMapping(path = "/resource")
  @ApiResponses({
      @ApiResponse(code = 200, response = File.class, message = ""),
  })
  public Resource resource(String content) {
    return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
      @Override
      public String getFilename() {
        return "resource.txt";
      }
    };
  }

  @GetMapping(path = "/entityResource")
  @ApiResponses({
      @ApiResponse(code = 200, response = File.class, message = ""),
  })
  public ResponseEntity<Resource> entityResource(String content) {
    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=entityResource.txt")
        .body(new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)));
  }

  @GetMapping(path = "/entityInputStream")
  @ApiResponses({
      @ApiResponse(code = 200, response = File.class, message = ""),
  })
  public ResponseEntity<InputStream> entityInputStream(String content) {
    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=entityInputStream.txt")
        .body(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
  }

  @GetMapping(path = "/bytes")
  @ApiResponses({
      @ApiResponse(code = 200, response = File.class, message = ""),
  })
  public ResponseEntity<byte[]> bytes(String content) {
    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=bytes.txt")
        .body(content.getBytes(StandardCharsets.UTF_8));
  }

  @GetMapping(path = "/netInputStream")
  @ApiResponses({
      @ApiResponse(code = 200, response = File.class, message = ""),
  })
  public ResponseEntity<InputStream> netInputStream(String content) throws IOException {
    URL url = new URL("http://localhost:" + server.getLocalPort() + "/download/netInputStream?content="
        + URLEncoder.encode(content, StandardCharsets.UTF_8.name()));
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    ResponseEntity<InputStream> responseEntity = ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=netInputStream.txt")
        .body(conn.getInputStream());
    conn.disconnect();
    return responseEntity;
  }
}
