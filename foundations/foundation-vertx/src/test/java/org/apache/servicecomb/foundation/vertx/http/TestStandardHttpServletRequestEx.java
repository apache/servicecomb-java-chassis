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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.foundation.vertx.stream.BufferInputStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.buffer.Buffer;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

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
    Assertions.assertSame(bodyBuffer, requestEx.getBodyBuffer());
    Assertions.assertArrayEquals("abc".getBytes(), Arrays.copyOf(requestEx.getBodyBytes(), requestEx.getBodyBytesLength()));
  }

  @Test
  public void getInputStreamNotCache() throws IOException {
    ServletInputStream inputStream = request.getInputStream();

    Assertions.assertSame(inputStream, requestEx.getInputStream());
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
    Assertions.assertEquals("abc", IOUtils.toString(cachedInputStream, StandardCharsets.UTF_8));
    Assertions.assertEquals("abc", requestEx.getBodyBuffer().toString());
    // do not create another one
    Assertions.assertSame(cachedInputStream, requestEx.getInputStream());
  }

  @Test
  public void parameterMap_inherited() {
    Map<String, String[]> inherited = new HashMap<>();
    String[] v1 = new String[] {"v1-1", "v1-2"};
    inherited.put("p1", v1);
    new Expectations() {
      {
        request.getParameterMap();
        result = inherited;
        request.getMethod();
        result = HttpMethod.POST;
      }
    };

    Assertions.assertSame(inherited, requestEx.getParameterMap());
    MatcherAssert.assertThat(Collections.list(requestEx.getParameterNames()), Matchers.contains("p1"));
    Assertions.assertSame(v1, requestEx.getParameterValues("p1"));
    Assertions.assertEquals("v1-1", requestEx.getParameter("p1"));
  }

  @Test
  public void parameterMap_merge() throws IOException {
    Map<String, String[]> inherited = new HashMap<>();
    String[] v1 = new String[] {"v1-1", "v1-2"};
    inherited.put("p1", v1);

    Buffer buffer = Buffer.buffer("p1=v1-3;p2=v2");
    BufferInputStream inputStream = new BufferInputStream(buffer.getByteBuf());
    new Expectations() {
      {
        request.getParameterMap();
        result = inherited;
        request.getMethod();
        result = HttpMethod.PUT;
        request.getContentType();
        result = MediaType.APPLICATION_FORM_URLENCODED.toUpperCase(Locale.US) + ";abc";
        request.getInputStream();
        result = inputStream;
      }
    };

    MatcherAssert.assertThat(Collections.list(requestEx.getParameterNames()), Matchers.containsInAnyOrder("p1", "p2"));
    MatcherAssert.assertThat(requestEx.getParameterValues("p1"), Matchers.arrayContaining("v1-1", "v1-2", "v1-3"));
    Assertions.assertEquals("v1-1", requestEx.getParameter("p1"));
  }

  @Test
  public void setParameter() {
    Map<String, String[]> parameterMap = new HashMap<>();
    Deencapsulation.setField(requestEx, "parameterMap", parameterMap);

    requestEx.setParameter("k1", "v1");
    requestEx.setParameter("k2", "v2");

    Assertions.assertEquals("v1", requestEx.getParameter("k1"));
    Assertions.assertEquals("v2", requestEx.getParameter("k2"));

    Assertions.assertSame(parameterMap, requestEx.getParameterMap());

    MatcherAssert.assertThat(Collections.list(requestEx.getParameterNames()), Matchers.containsInAnyOrder("k1", "k2"));
  }
}
