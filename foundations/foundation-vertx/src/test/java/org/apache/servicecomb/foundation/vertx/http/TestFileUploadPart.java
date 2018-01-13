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
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.ext.web.FileUpload;
import mockit.Expectations;
import mockit.Mocked;

public class TestFileUploadPart {
  @Mocked
  FileUpload fileUpload;

  FileUploadPart part;

  static File file;

  static String content = "fileContent";

  @BeforeClass
  public static void classSetup() throws IOException {
    file = File.createTempFile("upload", ".txt");
    file.deleteOnExit();
    FileUtils.writeStringToFile(file, content);
  }

  @Before
  public void setup() {
    part = new FileUploadPart(fileUpload);


  }

  @Test
  public void getInputStream() throws IOException {
    new Expectations() {
      {
        fileUpload.uploadedFileName();
        result = file.getAbsolutePath();
      }
    };
    try (InputStream is = part.getInputStream()) {
      Assert.assertEquals(content, IOUtils.toString(is));
    }
  }

  @Test
  public void getContentType() {
    String contentType = "type";
    new Expectations() {
      {
        fileUpload.contentType();
        result = contentType;
      }
    };

    Assert.assertEquals(contentType, part.getContentType());
  }

  @Test
  public void getName() {
    String name = "pName";
    new Expectations() {
      {
        fileUpload.name();
        result = name;
      }
    };

    Assert.assertEquals(name, part.getName());
  }

  @Test
  public void getSubmittedFileName() {
    String clientName = "clientName";
    new Expectations() {
      {
        fileUpload.fileName();
        result = clientName;
      }
    };

    Assert.assertEquals(clientName, part.getSubmittedFileName());
  }

  @Test
  public void getSize() {
    long fileSize = 10;
    new Expectations() {
      {
        fileUpload.size();
        result = fileSize;
      }
    };

    Assert.assertEquals(fileSize, part.getSize());
  }

  @Test
  public void write() throws IOException {
    new Expectations() {
      {
        fileUpload.uploadedFileName();
        result = file.getAbsolutePath();
      }
    };

    File targetFile = new File(UUID.randomUUID().toString());
    targetFile.deleteOnExit();
    part.write(targetFile.getAbsolutePath());
    Assert.assertEquals(content, FileUtils.readFileToString(targetFile));
  }
}
