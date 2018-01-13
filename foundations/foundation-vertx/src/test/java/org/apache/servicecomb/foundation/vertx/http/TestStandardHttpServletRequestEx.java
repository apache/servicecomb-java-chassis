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
import java.util.Arrays;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.buffer.Buffer;
import mockit.Expectations;
import mockit.Mocked;

public class TestStandardHttpServletRequestEx {
  @Mocked
  HttpServletRequest request;

  StandardHttpServletRequestEx requestEx;

  @Before
  public void setup() {
    requestEx = new StandardHttpServletRequestEx(request);
  }

  @Test
  public void setBodyBuffer() {
    Buffer bodyBuffer = Buffer.buffer();
    bodyBuffer.appendString("abc");

    requestEx.setBodyBuffer(bodyBuffer);
    Assert.assertSame(bodyBuffer, requestEx.getBodyBuffer());
    Assert.assertArrayEquals("abc".getBytes(), Arrays.copyOf(requestEx.getBodyBytes(), requestEx.getBodyBytesLength()));
  }

  @Test
  public void getInputStreamNotCache() throws IOException {
    ServletInputStream inputStream = request.getInputStream();

    Assert.assertSame(inputStream, requestEx.getInputStream());
  }

  @Test
  public void getInputStreamCache() throws IOException {
    requestEx.setCacheRequest(true);

    ServletInputStream inputStream = request.getInputStream();
    new Expectations(IOUtils.class) {
      {
        IOUtils.toByteArray(inputStream);
        result = "abc".getBytes();
      }
    };

    ServletInputStream cachedInputStream = requestEx.getInputStream();
    Assert.assertEquals("abc", IOUtils.toString(cachedInputStream));
    Assert.assertEquals("abc", requestEx.getBodyBuffer().toString());
    // do not create another one
    Assert.assertSame(cachedInputStream, requestEx.getInputStream());
  }
}
