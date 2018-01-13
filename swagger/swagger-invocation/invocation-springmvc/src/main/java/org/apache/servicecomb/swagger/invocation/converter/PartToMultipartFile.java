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

package org.apache.servicecomb.swagger.invocation.converter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

public class PartToMultipartFile implements MultipartFile {
  private Part part;

  public PartToMultipartFile(Part part) {
    this.part = part;
  }

  @Override
  public String getName() {
    return part.getName();
  }

  @Override
  public String getOriginalFilename() {
    return part.getSubmittedFileName();
  }

  @Override
  public String getContentType() {
    return part.getContentType();
  }

  @Override
  public boolean isEmpty() {
    return part.getSize() == 0;
  }

  @Override
  public long getSize() {
    return part.getSize();
  }

  @Override
  public byte[] getBytes() throws IOException {
    try (InputStream is = getInputStream()) {
      return IOUtils.toByteArray(is);
    }
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return part.getInputStream();
  }

  @Override
  public void transferTo(File dest) throws IOException, IllegalStateException {
    part.write(dest.getPath());
  }
}
