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

import java.io.IOException;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestAbstractHttpServletResponse {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  HttpServletResponseEx response = new AbstractHttpServletResponse() {
  };

  private void setExceptionExpected() {
    expectedException.expect(Error.class);
    expectedException.expectMessage(Matchers.is("not supported method"));
  }

  @Test
  public void testGetCharacterEncoding() {
    setExceptionExpected();

    response.getCharacterEncoding();
  }

  @Test
  public void testGetContentType() {
    setExceptionExpected();

    response.getContentType();
  }

  @Test
  public void testGetOutputStream() throws IOException {
    setExceptionExpected();

    response.getOutputStream();
  }

  @Test
  public void testGetWriter() throws IOException {
    setExceptionExpected();

    response.getWriter();
  }

  @Test
  public void testSetCharacterEncoding() {
    setExceptionExpected();

    response.setCharacterEncoding("");
  }

  @Test
  public void testSetContentLength() {
    setExceptionExpected();

    response.setContentLength(0);
  }

  @Test
  public void testSetContentLengthLong() {
    setExceptionExpected();

    response.setContentLengthLong(0);
  }

  @Test
  public void testSetContentType() {
    setExceptionExpected();

    response.setContentType("");
  }

  @Test
  public void testSetBufferSize() {
    setExceptionExpected();

    response.setBufferSize(0);
  }

  @Test
  public void testGetBufferSize() {
    setExceptionExpected();

    response.getBufferSize();
  }

  @Test
  public void testFlushBuffer() throws IOException {
    setExceptionExpected();

    response.flushBuffer();
  }

  @Test
  public void testResetBuffer() {
    setExceptionExpected();

    response.resetBuffer();
  }

  @Test
  public void testIsCommitted() {
    setExceptionExpected();

    response.isCommitted();
  }

  @Test
  public void testReset() {
    setExceptionExpected();

    response.reset();
  }

  @Test
  public void testSetLocale() {
    setExceptionExpected();

    response.setLocale(null);
  }

  @Test
  public void testGetLocale() {
    setExceptionExpected();

    response.getLocale();
  }

  @Test
  public void testAddCookie() {
    setExceptionExpected();

    response.addCookie(null);
  }

  @Test
  public void testContainsHeader() {
    setExceptionExpected();

    response.containsHeader(null);
  }

  @Test
  public void testEncodeURL() {
    setExceptionExpected();

    response.encodeURL(null);
  }

  @Test
  public void testEncodeRedirectURL() {
    setExceptionExpected();

    response.encodeRedirectURL(null);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testEncodeUrl() {
    setExceptionExpected();

    response.encodeUrl(null);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testEncodeRedirectUrl() {
    setExceptionExpected();

    response.encodeRedirectUrl(null);
  }

  @Test
  public void testSendErrorScAndMsg() throws IOException {
    setExceptionExpected();

    response.sendError(0, null);
  }

  @Test
  public void testSendErrorSc() throws IOException {
    setExceptionExpected();

    response.sendError(0);
  }

  @Test
  public void testSendRedirect() throws IOException {
    setExceptionExpected();

    response.sendRedirect(null);
  }

  @Test
  public void testSetDateHeader() {
    setExceptionExpected();

    response.setDateHeader(null, 0);
  }

  @Test
  public void testAddDateHeader() {
    setExceptionExpected();

    response.addDateHeader(null, 0);
  }

  @Test
  public void testSetHeader() {
    setExceptionExpected();

    response.setHeader(null, null);
  }

  @Test
  public void testAddHeader() {
    setExceptionExpected();

    response.addHeader(null, null);
  }

  @Test
  public void testSetIntHeader() {
    setExceptionExpected();

    response.setIntHeader(null, 0);
  }

  @Test
  public void testAddIntHeader() {
    setExceptionExpected();

    response.addIntHeader(null, 0);
  }

  @Test
  public void testSetStatusSc() {
    setExceptionExpected();

    response.setStatus(0);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testSetStatusScAndSm() {
    setExceptionExpected();

    response.setStatus(0, "");
  }

  @Test
  public void testGetStatus() {
    setExceptionExpected();

    response.getStatus();
  }

  @Test
  public void testGetHeader() {
    setExceptionExpected();

    response.getHeader("");
  }

  @Test
  public void testGetHeaders() {
    setExceptionExpected();

    response.getHeaders("");
  }

  @Test
  public void testGetHeaderNames() {
    setExceptionExpected();

    response.getHeaderNames();
  }

  @Test
  public void testGetStatusType() {
    setExceptionExpected();

    response.getStatusType();
  }
}
