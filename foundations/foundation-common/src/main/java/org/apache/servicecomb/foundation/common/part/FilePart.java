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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

public class FilePart extends AbstractPart implements FilePartForSend {
  private final File file;

  private boolean deleteAfterFinished;

  public FilePart(String name, String file) {
    this(name, new File(file));
  }

  public FilePart(String name, File file) {
    this.name = name;
    this.file = file;
    this.setSubmittedFileName(this.file.getName());
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(file);
  }

  @Override
  public long getSize() {
    return file.length();
  }

  @Override
  public void write(String fileName) throws IOException {
    FileUtils.copyFile(file, new File(fileName));
  }

  @Override
  public void delete() throws IOException {
    file.delete();
  }

  @Override
  public boolean isDeleteAfterFinished() {
    return deleteAfterFinished;
  }

  public FilePart setDeleteAfterFinished(boolean deleteAfterFinished) {
    this.deleteAfterFinished = deleteAfterFinished;
    return this;
  }

  @Override
  public String getAbsolutePath() {
    return file.getAbsolutePath();
  }
}
