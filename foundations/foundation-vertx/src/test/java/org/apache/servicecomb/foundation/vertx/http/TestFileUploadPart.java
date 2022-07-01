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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import io.vertx.ext.web.FileUpload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestFileUploadPart {

  FileUpload fileUpload;

  FileUploadPart part;

  static File file;

  static String content = "fileContent";

  @BeforeAll
  public static void classSetup() throws IOException {
    file = File.createTempFile("upload", ".txt");
    file.deleteOnExit();
    FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8, false);
  }

  @BeforeEach
  public void setup() {
    fileUpload = Mockito.mock(FileUpload.class);
    part = new FileUploadPart(fileUpload);
  }

  @AfterEach
  public void after() {
    Mockito.reset(fileUpload);
  }

  @Test
  public void getInputStream() throws IOException {
    Mockito.when(fileUpload.uploadedFileName()).thenReturn(file.getAbsolutePath());
    try (InputStream is = part.getInputStream()) {
      Assertions.assertEquals(content, IOUtils.toString(is, StandardCharsets.UTF_8));
    }
  }

  @Test
  public void getContentType() {
    String contentType = "type";
    Mockito.when(fileUpload.contentType()).thenReturn(contentType);

    Assertions.assertEquals(contentType, part.getContentType());
  }

  @Test
  public void getName() {
    String name = "pName";
    Mockito.when(fileUpload.name()).thenReturn(name);

    Assertions.assertEquals(name, part.getName());
  }

  @Test
  public void getSubmittedFileName() {
    String clientName = "clientName";
    Mockito.when(fileUpload.fileName()).thenReturn(clientName);

    Assertions.assertEquals(clientName, part.getSubmittedFileName());
  }

  @Test
  public void getSize() {
    long fileSize = 10;
    Mockito.when(fileUpload.size()).thenReturn(fileSize);

    Assertions.assertEquals(fileSize, part.getSize());
  }

  @Test
  public void write() throws IOException {
    Mockito.when(fileUpload.uploadedFileName()).thenReturn(file.getAbsolutePath());

    File targetFile = new File(UUID.randomUUID().toString());
    targetFile.deleteOnExit();
    part.write(targetFile.getAbsolutePath());
    Assertions.assertEquals(content, FileUtils.readFileToString(targetFile, StandardCharsets.UTF_8));
  }
}
