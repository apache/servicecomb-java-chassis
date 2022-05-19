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

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAbstractPart {
  AbstractPart part = new AbstractPart();

  private void checkError(Error error) {
    Assertions.assertEquals("not supported method", error.getMessage());
  }

  @Test
  public void getInputStream() throws IOException {
    Error error = Assertions.assertThrows(Error.class, () -> part.getInputStream());
    checkError(error);
  }

  @Test
  public void getContentType() throws IOException {
    Assertions.assertEquals(MediaType.APPLICATION_OCTET_STREAM, part.getContentType());

    String contentType = "abc";
    part.contentType(contentType);
    Assertions.assertEquals(contentType, part.getContentType());
  }

  @Test
  public void getName() throws IOException {
    Assertions.assertNull(part.getName());

    String name = "abc";
    part.name = name;
    Assertions.assertEquals(name, part.getName());
  }

  @Test
  public void getSubmittedFileName() throws IOException {
    Assertions.assertNull(part.getSubmittedFileName());

    String submittedFileName = "abc";
    part.setSubmittedFileName(submittedFileName);
    Assertions.assertEquals(submittedFileName, part.getSubmittedFileName());
  }

  @Test
  public void setSubmittedFileName_contentTypeNotNull() {
    part.contentType(MediaType.TEXT_PLAIN);
    part.setSubmittedFileName("a.zip");

    Assertions.assertEquals(MediaType.TEXT_PLAIN, part.getContentType());
  }

  @Test
  public void setSubmittedFileName_setNull() {
    part.setSubmittedFileName(null);

    Assertions.assertEquals(MediaType.APPLICATION_OCTET_STREAM, part.getContentType());
  }

  @Test
  public void setSubmittedFileName_setNormal() {
    part.setSubmittedFileName("a.zip");

    Assertions.assertEquals("application/zip", part.getContentType());
  }

  @Test
  public void getSize() {
    Error error = Assertions.assertThrows(Error.class, () -> part.getSize());
    checkError(error);
  }

  @Test
  public void write() throws IOException {
    Error error = Assertions.assertThrows(Error.class, () -> part.write("file"));
    checkError(error);
  }

  @Test
  public void delete() throws IOException {
    Error error = Assertions.assertThrows(Error.class, () -> part.delete());
    checkError(error);
  }

  @Test
  public void getHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> part.getHeader("header"));
    checkError(error);
  }

  @Test
  public void getHeaders() {
    Error error = Assertions.assertThrows(Error.class, () -> part.getHeaders("header"));
    checkError(error);
  }

  @Test
  public void getHeaderNames() {
    Error error = Assertions.assertThrows(Error.class, () -> part.getHeaderNames());
    checkError(error);
  }
}
