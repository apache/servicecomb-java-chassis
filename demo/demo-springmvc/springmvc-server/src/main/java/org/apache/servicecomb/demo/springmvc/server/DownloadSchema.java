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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.part.FilePart;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "DownloadSchema")
@RequestMapping(path = "/download", produces = MediaType.APPLICATION_JSON_VALUE)
public class DownloadSchema {
  private File tempDir = new File("target/downloadTemp");

  private File lastFile;

  private File createTempFile(String content) throws IOException {
    return createTempFile(null, content);
  }

  private File createTempFile(String name, String content) throws IOException {
    if (name == null) {
      name = "download-" + UUID.randomUUID() + ".txt";
    }
    File file = new File(tempDir, name);
    FileUtils.write(file, content, StandardCharsets.UTF_8, false);
    lastFile = file;
    return file;
  }

  @GetMapping(path = "/deleteAfterFinished")
  public ResponseEntity<Part> deleteAfterFinished(@RequestParam("content") String content) throws IOException {
    File file = createTempFile(content);

    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=tempFileEntity.txt")
        .body(new FilePart(null, file)
            .setDeleteAfterFinished(true));
  }

  @GetMapping(path = "/partIsNull")
  public ResponseEntity<Part> partIsNull(@RequestParam("content") String content) throws IOException {
    if (StringUtils.isEmpty(content)) {
      return ResponseEntity
          .ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=tempFileEntity.txt")
          .body(null);
    }
    File file = createTempFile(content);
    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=tempFileEntity.txt")
        .body(new FilePart(null, file));
  }

  @GetMapping(path = "/notDeleteAfterFinished")
  public ResponseEntity<Part> notDeleteAfterFinished(@RequestParam("content") String content) throws IOException {
    File file = createTempFile(content);

    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=tempFileEntity.txt")
        .body(new FilePart(null, file));
  }

  @GetMapping(path = "/assertLastFileDeleted")
  public boolean assertLastFileDeleted() {
    return lastFile.exists();
  }
}
