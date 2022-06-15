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

package org.apache.servicecomb.foundation.common.part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestFilePart {
  static File file = new File("testFilePart.txt");

  static String content = "testFilePart content";

  String name = "paramName";

  FilePart part = new FilePart(name, file.getAbsolutePath());

  @BeforeAll
  public static void setup() throws IOException {
    file.delete();
    FileUtils.write(file, content, StandardCharsets.UTF_8);
  }

  @AfterAll
  public static void teardown() {
    file.delete();
  }

  @Test
  public void getName() {
    Assertions.assertEquals(name, part.getName());
  }

  @Test
  public void getInputStream() throws IOException {
    try (InputStream is = part.getInputStream()) {
      Assertions.assertEquals(content, IOUtils.toString(is, StandardCharsets.UTF_8));
    }
  }

  @Test
  public void getSize() {
    Assertions.assertEquals(content.length(), part.getSize());
  }

  @Test
  public void write() throws IOException {
    File destFile = new File("testFilePartCopy.txt");

    part.write(destFile.getPath());
    Assertions.assertEquals(content, FileUtils.readFileToString(destFile, StandardCharsets.UTF_8));

    FilePart destPart = new FilePart(null, destFile);
    destPart.delete();
    Assertions.assertFalse(destFile.exists());
  }

  @Test
  public void deleteAfterFinished() {
    Assertions.assertFalse(part.isDeleteAfterFinished());

    Assertions.assertTrue(part.setDeleteAfterFinished(true).isDeleteAfterFinished());
  }

  @Test
  public void getAbsolutePath() {
    Assertions.assertEquals(file.getAbsolutePath(), part.getAbsolutePath());
  }
}
