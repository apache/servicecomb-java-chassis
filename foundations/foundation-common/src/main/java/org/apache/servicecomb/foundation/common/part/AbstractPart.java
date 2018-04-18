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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;

public class AbstractPart implements Part {
  private static MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();

  protected String name;

  private String submittedFileName;

  protected String contentType;

  @Override
  public InputStream getInputStream() throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public String getContentType() {
    return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM;
  }

  @SuppressWarnings("unchecked")
  public <T> T contentType(String contentType) {
    this.contentType = contentType;
    return (T) this;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getSubmittedFileName() {
    return submittedFileName;
  }

  public AbstractPart setSubmittedFileName(String submittedFileName) {
    this.submittedFileName = submittedFileName;
    updateContentType();
    return this;
  }

  private void updateContentType() {
    if (contentType != null || submittedFileName == null) {
      return;
    }

    contentType = mimetypesFileTypeMap.getContentType(submittedFileName);
  }

  @Override
  public long getSize() {
    throw new Error("not supported method");
  }

  @Override
  public void write(String fileName) throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public void delete() throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public String getHeader(String name) {
    throw new Error("not supported method");
  }

  @Override
  public Collection<String> getHeaders(String name) {
    throw new Error("not supported method");
  }

  @Override
  public Collection<String> getHeaderNames() {
    throw new Error("not supported method");
  }
}
