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
package org.apache.servicecomb.demo.springmvc.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
@RequestMapping(path = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
public class DownloadSchema {
  @GetMapping(path = "/file")
  public File downloadFile() throws IOException {
    return new File(this.getClass().getClassLoader().getResource("microservice.yaml").getFile());
  }

  @GetMapping(path = "/resource")
  @ApiResponses({
      @ApiResponse(code = 200, response = File.class, message = ""),
  })
  public Resource downloadResource() throws IOException {
    return new ByteArrayResource("abc".getBytes(StandardCharsets.UTF_8)) {
      @Override
      public String getFilename() {
        return "abc.txt";
      }
    };
  }

  @GetMapping(path = "/entityResource")
  @ApiResponses({
      @ApiResponse(code = 200, response = File.class, message = ""),
  })
  public ResponseEntity<Resource> downloadEntityResource() throws IOException {
    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=entityResource.txt")
        .body(new ByteArrayResource("entityResource".getBytes(StandardCharsets.UTF_8)));
  }

  @GetMapping(path = "/entityInputStream")
  @ApiResponses({
      @ApiResponse(code = 200, response = File.class, message = ""),
  })
  public ResponseEntity<InputStream> downloadEntityInputStream() throws IOException {
    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=entityInputStream.txt")
        .body(new ByteArrayInputStream("entityInputStream".getBytes(StandardCharsets.UTF_8)));
  }
}
