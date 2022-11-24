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
package org.apache.servicecomb.common.rest.codec.param;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Part;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.common.collect.Multimap;

import io.vertx.core.Context;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import static org.assertj.core.api.Assertions.assertThat;

public class TestRestClientRequestImpl {
  private HttpClientRequest request;

  private final Context context = Mockito.mock(Context.class);

  @BeforeEach
  public void before() {
    request = Mockito.mock(HttpClientRequest.class);
  }

  @AfterEach
  public void after() {
    Mockito.reset(request);
  }

  @Test
  public void testForm() throws Exception {
    RestClientRequestImpl restClientRequest = new RestClientRequestImpl(request, null, null);
    restClientRequest.addForm("abc", "Hello");
    restClientRequest.addForm("def", "world");
    restClientRequest.addForm("ghi", null);
    Buffer buffer = restClientRequest.getBodyBuffer();
    assertThat(buffer.toString()).isIn("def=world&abc=Hello&", "abc=Hello&def=world&");
  }

  @Test
  public void testCookie() throws Exception {
    final MultiMap map = MultiMap.caseInsensitiveMultiMap();
    Mockito.doAnswer(invocation -> {
      map.add(io.vertx.core.http.HttpHeaders.COOKIE, "sessionid=abcdefghijklmnopqrstuvwxyz; region=china-north; ");
      return null;
    }).when(request).putHeader(io.vertx.core.http.HttpHeaders.COOKIE, "sessionid=abcdefghijklmnopqrstuvwxyz; region=china-north; ");
    Mockito.doAnswer(invocation -> {
      map.add(io.vertx.core.http.HttpHeaders.COOKIE, "sessionid=abcdefghijklmnopqrstuvwxyz; region=china-north; ");
      return null;
    }).when(request).putHeader(io.vertx.core.http.HttpHeaders.COOKIE, "region=china-north; sessionid=abcdefghijklmnopqrstuvwxyz; ");
    Mockito.when(request.headers()).thenReturn(map);

    RestClientRequestImpl restClientRequest = new RestClientRequestImpl(request, null, null);
    restClientRequest.addCookie("sessionid", "abcdefghijklmnopqrstuvwxyz");
    restClientRequest.addCookie("region", "china-north");
    restClientRequest.write(Buffer.buffer("I love servicecomb"));
    restClientRequest.end();
    Buffer buffer = restClientRequest.getBodyBuffer();
    Assertions.assertEquals("I love servicecomb", buffer.toString());
    Assertions.assertEquals("sessionid=abcdefghijklmnopqrstuvwxyz; region=china-north; ",
        restClientRequest.request.headers().get(HttpHeaders.COOKIE));
  }

  @Test
  public void fileBoundaryInfo_nullSubmittedFileName() {
    Part part = Mockito.mock(Part.class);
    Mockito.when(part.getSubmittedFileName()).thenReturn(null);
    Mockito.when(part.getContentType()).thenReturn("abc");

    RestClientRequestImpl restClientRequest = new RestClientRequestImpl(request, null, null);
    Buffer buffer = restClientRequest.fileBoundaryInfo("boundary", "name", part);
    Assertions.assertEquals("\r\n" +
        "--boundary\r\n" +
        "Content-Disposition: form-data; name=\"name\"; filename=\"null\"\r\n" +
        "Content-Type: abc\r\n" +
        "Content-Transfer-Encoding: binary\r\n" +
        "\r\n", buffer.toString());
  }

  @Test
  public void fileBoundaryInfo_validSubmittedFileName() {
    Part part = Mockito.mock(Part.class);
    Mockito.when(part.getSubmittedFileName()).thenReturn("a.txt");
    Mockito.when(part.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
    RestClientRequestImpl restClientRequest = new RestClientRequestImpl(request, null, null);
    Buffer buffer = restClientRequest.fileBoundaryInfo("boundary", "name", part);
    Assertions.assertEquals("\r\n" +
        "--boundary\r\n" +
        "Content-Disposition: form-data; name=\"name\"; filename=\"a.txt\"\r\n" +
        "Content-Type: text/plain\r\n" +
        "Content-Transfer-Encoding: binary\r\n" +
        "\r\n", buffer.toString());
  }

  @Test
  public void testAttach() {
    RestClientRequestImpl restClientRequest = new RestClientRequestImpl(request, null, null);
    Part part = Mockito.mock(Part.class);
    String fileName = "fileName";

    restClientRequest.attach(fileName, part);

    Multimap<String, Part> uploads = restClientRequest.uploads;
    Assertions.assertEquals(1, uploads.size());
    MatcherAssert.assertThat(uploads.asMap(), Matchers.hasEntry(fileName, Arrays.asList(part)));
  }

  @Test
  public void testAttachOnPartIsNull() {
    RestClientRequestImpl restClientRequest = new RestClientRequestImpl(request, null, null);

    restClientRequest.attach("fileName", null);

    Multimap<String, Part> uploads = restClientRequest.uploads;
    Assertions.assertTrue(uploads.isEmpty());
  }

  @Test
  @EnabledOnJre(JRE.JAVA_8)
  public void doEndWithUploadForJre8() {
    Map<String, String> headers = new HashMap<>();
    Mockito.doAnswer(invocation -> {
      headers.put(HttpHeaders.CONTENT_TYPE, "multipart/form-data; charset=UTF-8; boundary=boundarynull-null-null-null-null");
      return null;
    }).when(request).putHeader(HttpHeaders.CONTENT_TYPE, "multipart/form-data; charset=UTF-8; boundary=boundarynull-null-null-null-null");

    UUID uuid = new UUID(0, 0);
    try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
      mockedStatic.when(UUID::randomUUID).thenReturn(uuid);
      RestClientRequestImpl restClientRequest = new RestClientRequestImpl(request, context, null);
      restClientRequest.doEndWithUpload();

      Assertions.assertEquals("multipart/form-data; charset=UTF-8; boundary=boundarynull-null-null-null-null",
              headers.get(HttpHeaders.CONTENT_TYPE));
    }
  }

  @Test
  @EnabledForJreRange(min = JRE.JAVA_9)
  public void doEndWithUploadAfterJre8() {
    Map<String, String> headers = new HashMap<>();
    Mockito.doAnswer(invocation -> {
      headers.put(HttpHeaders.CONTENT_TYPE, "multipart/form-data; charset=UTF-8; boundary=boundary00000000-0000-0000-0000-000000000000");
      return null;
    }).when(request).putHeader(HttpHeaders.CONTENT_TYPE, "multipart/form-data; charset=UTF-8; boundary=boundary00000000-0000-0000-0000-000000000000");

    UUID uuid = new UUID(0, 0);
    try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
      mockedStatic.when(UUID::randomUUID).thenReturn(uuid);
      RestClientRequestImpl restClientRequest = new RestClientRequestImpl(request, context, null);
      restClientRequest.doEndWithUpload();

      Assertions.assertEquals("multipart/form-data; charset=UTF-8; boundary=boundary00000000-0000-0000-0000-000000000000",
              headers.get(HttpHeaders.CONTENT_TYPE));
    }
  }
}
