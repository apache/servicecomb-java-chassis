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

package org.apache.servicecomb.http.client.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpTransportImplTest {

  @Test
  public void TestHttpTransport() throws IOException {
    HttpClient httpClient = mock(HttpClient.class);

    org.apache.http.HttpResponse httpResponse = mock(org.apache.http.HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    when(statusLine.getStatusCode()).thenReturn(200);
    when(statusLine.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
    when(statusLine.getReasonPhrase()).thenReturn("OK");

    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(httpResponse.getEntity()).thenReturn(new StringEntity("Test", ContentType.APPLICATION_JSON));

    when(httpClient.execute(Mockito.any())).thenReturn(httpResponse);

    HttpTransportImpl httpTransport = new HttpTransportImpl();
    httpTransport.setHttpClient(httpClient);
    Map<String, String> extraHeaders = new HashMap<>();
    extraHeaders.put("test", "testContext");
    httpTransport.addHeaders(extraHeaders);

    HttpRequest httpRequest = new HttpRequest("111", null, null, HttpRequest.GET);
    HttpResponse actualResponse = httpTransport.get(httpRequest);

    Assert.assertNotNull(actualResponse);
    Assert.assertEquals(200, actualResponse.getStatusCode());
    Assert.assertEquals("OK", actualResponse.getMessage());
    Assert.assertEquals("Test", actualResponse.getContent());
  }
}
