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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAbstractHttpServletResponse {

  HttpServletResponseEx response = new AbstractHttpServletResponse() {
  };

  private void checkError(Error error) {
    Assertions.assertEquals("not supported method", error.getMessage());
  }

  @Test
  public void testGetCharacterEncoding() {
    Error error = Assertions.assertThrows(Error.class, () -> response.getCharacterEncoding());
    checkError(error);
  }

  @Test
  public void testGetContentType() {
    Error error = Assertions.assertThrows(Error.class, () -> response.getContentType());
    checkError(error);
  }

  @Test
  public void testGetOutputStream() {
    Error error = Assertions.assertThrows(Error.class, () -> response.getOutputStream());
    checkError(error);
  }

  @Test
  public void testGetWriter() {
    Error error = Assertions.assertThrows(Error.class, () -> response.getWriter());
    checkError(error);
  }

  @Test
  public void testSetCharacterEncoding() {
    Error error = Assertions.assertThrows(Error.class, () -> response.setCharacterEncoding(""));
    checkError(error);
  }

  @Test
  public void testSetContentLength() {
    Error error = Assertions.assertThrows(Error.class, () -> response.setContentLength(0));
    checkError(error);
  }

  @Test
  public void testSetContentLengthLong() {
    Error error = Assertions.assertThrows(Error.class, () -> response.setContentLengthLong(0));
    checkError(error);
  }

  @Test
  public void testSetContentType() {
    Error error = Assertions.assertThrows(Error.class, () -> response.setContentType(""));
    checkError(error);
  }

  @Test
  public void testSetBufferSize() {
    Error error = Assertions.assertThrows(Error.class, () -> response.setBufferSize(0));
    checkError(error);
  }

  @Test
  public void testGetBufferSize() {
    Error error = Assertions.assertThrows(Error.class, () -> response.getBufferSize());
    checkError(error);
  }

  @Test
  public void testFlushBuffer() {
    Error error = Assertions.assertThrows(Error.class, () -> response.flushBuffer());
    checkError(error);
  }

  @Test
  public void testResetBuffer() {
    Error error = Assertions.assertThrows(Error.class, () -> response.resetBuffer());
    checkError(error);
  }

  @Test
  public void testIsCommitted() {
    Error error = Assertions.assertThrows(Error.class, () -> response.isCommitted());
    checkError(error);
  }

  @Test
  public void testReset() {
    Error error = Assertions.assertThrows(Error.class, () -> response.reset());
    checkError(error);
  }

  @Test
  public void testSetLocale() {
    Error error = Assertions.assertThrows(Error.class, () -> response.setLocale(null));
    checkError(error);
  }

  @Test
  public void testGetLocale() {
    Error error = Assertions.assertThrows(Error.class, () -> response.getLocale());
    checkError(error);
  }

  @Test
  public void testAddCookie() {
    Error error = Assertions.assertThrows(Error.class, () -> response.addCookie(null));
    checkError(error);
  }

  @Test
  public void testContainsHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> response.containsHeader(null));
    checkError(error);
  }

  @Test
  public void testEncodeURL() {
    Error error = Assertions.assertThrows(Error.class, () -> response.encodeURL(null));
    checkError(error);
  }

  @Test
  public void testEncodeRedirectURL() {
    Error error = Assertions.assertThrows(Error.class, () -> response.encodeRedirectURL(null));
    checkError(error);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testEncodeUrl() {
    Error error = Assertions.assertThrows(Error.class, () -> response.encodeUrl(null));
    checkError(error);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testEncodeRedirectUrl() {
    Error error = Assertions.assertThrows(Error.class, () -> response.encodeRedirectUrl(null));
    checkError(error);
  }

  @Test
  public void testSendErrorScAndMsg() {
    Error error = Assertions.assertThrows(Error.class, () -> response.sendError(0, null));
    checkError(error);
  }

  @Test
  public void testSendErrorSc() {
    Error error = Assertions.assertThrows(Error.class, () -> response.sendError(0));
    checkError(error);
  }

  @Test
  public void testSendRedirect() {
    Error error = Assertions.assertThrows(Error.class, () -> response.sendRedirect(null));
    checkError(error);
  }

  @Test
  public void testSetDateHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> response.setDateHeader(null, 0));
    checkError(error);
  }

  @Test
  public void testAddDateHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> response.addDateHeader(null, 0));
    checkError(error);
  }

  @Test
  public void testSetHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> response.setHeader(null, null));
    checkError(error);
  }

  @Test
  public void testAddHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> response.addHeader(null, null));
    checkError(error);
  }

  @Test
  public void testSetIntHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> response.setIntHeader(null, 0));
    checkError(error);
  }

  @Test
  public void testAddIntHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> response.addIntHeader(null, 0));
    checkError(error);
  }

  @Test
  public void testSetStatusSc() {
    Error error = Assertions.assertThrows(Error.class, () -> response.setStatus(0));
    checkError(error);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testSetStatusScAndSm() {
    Error error = Assertions.assertThrows(Error.class, () -> response.setStatus(0, ""));
    checkError(error);
  }

  @Test
  public void testGetStatus() {
    Error error = Assertions.assertThrows(Error.class, () -> response.getStatus());
    checkError(error);
  }

  @Test
  public void testGetHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> response.getHeader(""));
    checkError(error);
  }

  @Test
  public void testGetHeaders() {
    Error error = Assertions.assertThrows(Error.class, () -> response.getHeaders(""));
    checkError(error);
  }

  @Test
  public void testGetHeaderNames() {
    Error error = Assertions.assertThrows(Error.class, () -> response.getHeaderNames());
    checkError(error);
  }

  @Test
  public void testGetStatusType() {
    Error error = Assertions.assertThrows(Error.class, () -> response.getStatusType());
    checkError(error);
  }

  @Test
  public void attribute() {
    response.setAttribute("k", "v");
    Assertions.assertEquals("v", response.getAttribute("k"));
  }

  @Test
  public void sendPart() {
    Error error = Assertions.assertThrows(Error.class, () -> response.sendPart(null));
    checkError(error);
  }
}
