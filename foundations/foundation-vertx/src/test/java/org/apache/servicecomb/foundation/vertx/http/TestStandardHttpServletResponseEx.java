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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.servicecomb.foundation.common.part.InputStreamPart;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import io.vertx.core.buffer.Buffer;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;


public class TestStandardHttpServletResponseEx {
  HttpServletResponse response;

  StandardHttpServletResponseEx responseEx;

  @Before
  public void setup() {
    response = Mockito.mock(HttpServletResponse.class);
    responseEx = new StandardHttpServletResponseEx(response);
  }

  @Test
  public void setBodyBuffer() {
    Assertions.assertNull(responseEx.getBodyBuffer());

    Buffer bodyBuffer = Buffer.buffer();
    bodyBuffer.appendString("abc");
    responseEx.setBodyBuffer(bodyBuffer);
    Assertions.assertEquals("abc", responseEx.getBodyBuffer().toString());
  }

  @Test
  public void getBodyBytes() {
    Buffer bodyBuffer = Buffer.buffer();
    bodyBuffer.appendString("abc");
    responseEx.setBodyBuffer(bodyBuffer);
    Assertions.assertEquals("abc", new String(responseEx.getBodyBytes(), 0, responseEx.getBodyBytesLength()));
  }

  @Test
  public void getBodyBytesLength() {
    Buffer bodyBuffer = Buffer.buffer();
    bodyBuffer.appendString("abc");
    responseEx.setBodyBuffer(bodyBuffer);
    Assertions.assertEquals(3, responseEx.getBodyBytesLength());
  }

  @Test
  public void flushBuffer() throws IOException {
    Buffer buffer = Buffer.buffer();
    ServletOutputStream output = new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {

      }

      @Override
      public void write(int b) {
        buffer.appendByte((byte) b);
      }
    };

    Mockito.when(response.getOutputStream()).thenReturn(output);
    responseEx = new StandardHttpServletResponseEx(response);

    // no body
    responseEx.flushBuffer();
    Assertions.assertEquals(0, buffer.length());

    Buffer body = Buffer.buffer().appendString("body");
    responseEx.setBodyBuffer(body);
    responseEx.flushBuffer();
    Assertions.assertEquals("body", buffer.toString());
  }

  @Test
  public void attribute() {
    responseEx.setAttribute("k", "v");
    Assertions.assertEquals("v", responseEx.getAttribute("k"));
  }

  @Test
  public void sendPart_succ() throws Throwable {
    String src = RandomStringUtils.random(100, true, true);
    InputStream inputStream = new ByteArrayInputStream(src.getBytes());
    Part part = new InputStreamPart("name", inputStream);
    Buffer buffer = Buffer.buffer();

    ServletOutputStream outputStream = new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {

      }

      @Override
      public void write(int b) {
        buffer.appendByte((byte) b);
      }
    };
    Mockito.when(response.getOutputStream()).thenReturn(outputStream);

    responseEx.sendPart(part).get();

    Assertions.assertEquals(src, buffer.toString());
  }

  @Test
  public void sendPart_failed() throws Throwable {
    Part part = Mockito.mock(Part.class);
    RuntimeException error = new RuntimeExceptionWithoutStackTrace();
    Mockito.when(response.getOutputStream()).thenThrow(error);

    Assertions.assertThrows(RuntimeException.class, () -> responseEx.sendPart(part).get());
  }
}
