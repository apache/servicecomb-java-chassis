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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestSchema(schemaId = "UploadSchema")
@RequestMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
public class UploadSchema {
  @PostMapping(path = "/fileUpload", produces = MediaType.TEXT_PLAIN_VALUE,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String fileUpload(@RequestPart(name = "files") List<MultipartFile> files) {
    try {
      String fileName = UUID.randomUUID().toString();
      List<File> savedFiles = new ArrayList<>();
      int index = 0;
      for (MultipartFile file : files) {
        File tempFile = new File("random-server-" + fileName + index);
        savedFiles.add(tempFile);
        file.transferTo(tempFile);
        index++;
      }
      savedFiles.forEach(File::delete);
      return "success";
    } catch (IOException e) {
      return "failed";
    }
  }

  @GetMapping(path = "/isServerStartUpSuccess")
  public boolean isServerStartUpSuccess() {
    return TestMgr.isSuccess();
  }

  @PostMapping(path = "/fileUploadMultiRpc", produces = MediaType.TEXT_PLAIN_VALUE,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String fileUploadMultiRpc(@RequestPart(name = "files") MultipartFile[] files) {
    return "fileUploadMulti success, and fileNum is " + files.length;
  }

  @PostMapping(path = "/uploadFileRequestPartAttribute", produces = MediaType.TEXT_PLAIN_VALUE)
  public String uploadFileRequestPartAttribute(@RequestPart(name = "file") MultipartFile file,
      @RequestPart(name = "attribute") String attribute) throws IOException {
    try (InputStream is = file.getInputStream()) {
      return attribute + " " + IOUtils.toString(is, StandardCharsets.UTF_8);
    }
  }

  @PostMapping(path = "/uploadFileAndAttribute", produces = MediaType.TEXT_PLAIN_VALUE)
  public String uploadFileAndAttribute(@RequestPart(name = "file") MultipartFile file,
      @RequestAttribute(name = "attribute") String attribute) throws IOException {
    try (InputStream is = file.getInputStream()) {
      return attribute + " " + IOUtils.toString(is, StandardCharsets.UTF_8);
    }
  }
}
