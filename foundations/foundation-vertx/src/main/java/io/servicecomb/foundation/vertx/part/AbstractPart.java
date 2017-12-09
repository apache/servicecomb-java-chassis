/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.vertx.part;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;

public class AbstractPart implements Part {
  protected String name;

  protected String submittedFileName;

  protected String contentType = MediaType.MULTIPART_FORM_DATA;

  @Override
  public InputStream getInputStream() throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public String getContentType() {
    return contentType;
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
