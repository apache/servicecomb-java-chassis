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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFilePart {
  static File file = new File("testFilePart.txt");

  static String content = "testFilePart content";

  String name = "paramName";

  FilePart part = new FilePart(name, file.getAbsolutePath());

  @BeforeClass
  public static void setup() throws IOException {
    file.delete();
    FileUtils.write(file, content);
  }

  @AfterClass
  public static void teardown() {
    file.delete();
  }

  @Test
  public void getName() {
    Assert.assertEquals(name, part.getName());
  }

  @Test
  public void getInputStream() throws IOException {
    try (InputStream is = part.getInputStream()) {
      Assert.assertEquals(content, IOUtils.toString(is));
    }
  }

  @Test
  public void getSize() {
    Assert.assertEquals(content.length(), part.getSize());
  }

  @Test
  public void write() throws IOException {
    File destFile = new File("testFilePartCopy.txt");

    part.write(destFile.getPath());
    Assert.assertEquals(content, FileUtils.readFileToString(destFile));

    FilePart destPart = new FilePart(null, destFile);
    destPart.delete();
    Assert.assertFalse(destFile.exists());
  }

  @Test
  public void deleteAfterFinished() {
    Assert.assertFalse(part.isDeleteAfterFinished());

    Assert.assertTrue(part.setDeleteAfterFinished(true).isDeleteAfterFinished());
  }

  @Test
  public void getAbsolutePath() {
    Assert.assertEquals(file.getAbsolutePath(), part.getAbsolutePath());
  }
}
