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

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.servicecomb.foundation.common.part.InputStreamPart;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.vertx.core.buffer.Buffer;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestStandardHttpServletResponseEx {
  @Mocked
  HttpServletResponse response;

  StandardHttpServletResponseEx responseEx;

  @Before
  public void setup() {
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
  public void setStatus() {
    responseEx.setStatus(200, "ok");
    Assertions.assertEquals(200, responseEx.getStatus());
    Assertions.assertEquals(200, responseEx.getStatusType().getStatusCode());
    Assertions.assertEquals("ok", responseEx.getStatusType().getReasonPhrase());
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
      public void write(int b) throws IOException {
        buffer.appendByte((byte) b);
      }
    };
    response = new MockUp<HttpServletResponse>() {
      @Mock
      ServletOutputStream getOutputStream() {
        return output;
      }
    }.getMockInstance();
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
    String src = RandomStringUtils.random(100);
    InputStream inputStream = new ByteArrayInputStream(src.getBytes());
    Part part = new InputStreamPart("name", inputStream);
    Buffer buffer = Buffer.buffer();
    ServletOutputStream outputStream = new MockUp<ServletOutputStream>() {
      @Mock
      void write(int b) {
        buffer.appendByte((byte) b);
      }
    }.getMockInstance();

    new Expectations() {
      {
        response.getOutputStream();
        result = outputStream;
      }
    };

    responseEx.sendPart(part).get();

    Assertions.assertEquals(src, buffer.toString());
  }

  @Test
  public void sendPart_failed(@Mocked Part part) throws Throwable {
    RuntimeException error = new RuntimeExceptionWithoutStackTrace();
    new Expectations() {
      {
        response.getOutputStream();
        result = error;
      }
    };

    Assertions.assertThrows(RuntimeException.class, () -> responseEx.sendPart(part).get());
  }
}
