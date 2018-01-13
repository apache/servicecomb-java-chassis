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

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestAbstractPart {
  AbstractPart part = new AbstractPart();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private void initExpectedException() {
    expectedException.expect(Error.class);
    expectedException.expectMessage(Matchers.is("not supported method"));
  }

  @Test
  public void getInputStream() throws IOException {
    initExpectedException();

    part.getInputStream();
  }

  @Test
  public void getContentType() throws IOException {
    Assert.assertEquals(MediaType.APPLICATION_OCTET_STREAM, part.getContentType());

    String contentType = "abc";
    part.contentType(contentType);
    Assert.assertEquals(contentType, part.getContentType());
  }

  @Test
  public void getName() throws IOException {
    Assert.assertNull(part.getName());

    String name = "abc";
    part.name = name;
    Assert.assertEquals(name, part.getName());
  }

  @Test
  public void getSubmittedFileName() throws IOException {
    Assert.assertNull(part.getSubmittedFileName());

    String submittedFileName = "abc";
    part.setSubmittedFileName(submittedFileName);
    Assert.assertEquals(submittedFileName, part.getSubmittedFileName());
  }

  @Test
  public void setSubmittedFileName_contentTypeNotNull() {
    part.contentType(MediaType.TEXT_PLAIN);
    part.setSubmittedFileName("a.zip");

    Assert.assertEquals(MediaType.TEXT_PLAIN, part.getContentType());
  }

  @Test
  public void setSubmittedFileName_setNull() {
    part.setSubmittedFileName(null);

    Assert.assertEquals(MediaType.APPLICATION_OCTET_STREAM, part.getContentType());
  }

  @Test
  public void setSubmittedFileName_setNormal() {
    part.setSubmittedFileName("a.zip");

    Assert.assertEquals("application/zip", part.getContentType());
  }

  @Test
  public void getSize() {
    initExpectedException();

    part.getSize();
  }

  @Test
  public void write() throws IOException {
    initExpectedException();

    part.write("file");
  }

  @Test
  public void delete() throws IOException {
    initExpectedException();

    part.delete();
  }

  @Test
  public void getHeader() {
    initExpectedException();

    part.getHeader("header");
  }

  @Test
  public void getHeaders() {
    initExpectedException();

    part.getHeaders("header");
  }

  @Test
  public void getHeaderNames() {
    initExpectedException();

    part.getHeaderNames();
  }
}
