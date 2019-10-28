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

package org.apache.servicecomb.kie.client;

import org.apache.servicecomb.kie.client.http.HttpRequest;
import org.apache.servicecomb.kie.client.http.HttpResponse;
import org.apache.servicecomb.kie.client.http.HttpTransport;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

/**
 * Created by   on 2019/10/24.
 */
public class KieRawClientTest {

  private static final String DEFAULT_HOST = "192.168.88.229";

  private static final int DEFAULT_PORT = 30110;

  private static final String DEFAULT_DOMAIN = "testProject";

  @Test
  public void TestDefaultUrl() throws IOException {

    HttpTransport httpTransport = Mockito.mock(HttpTransport.class);

    KieRawClient client = new KieRawClient.Builder()
        .setHttpTransport(httpTransport)
        .setHost(DEFAULT_HOST)
        .setPort(DEFAULT_PORT)
        .setDomainName(DEFAULT_DOMAIN)
        .build();

    HttpResponse httpResponse = new HttpResponse();
    httpResponse.setStatusCode(200);
    httpResponse.setContent("ok");

    Mockito.when(httpTransport.get((HttpRequest) Mockito.any())).thenReturn(httpResponse);
    Mockito.when(httpTransport.post((HttpRequest) Mockito.any())).thenReturn(httpResponse);
    Mockito.when(httpTransport.put((HttpRequest) Mockito.any())).thenReturn(httpResponse);
    Mockito.when(httpTransport.delete((HttpRequest) Mockito.any())).thenReturn(httpResponse);

    HttpResponse actualGetResponse = client.getHttpRequest(null, null, null);
    HttpResponse actualPostResponse = client.postHttpRequest(null, null, null);
    HttpResponse actualPutResponse = client.putHttpRequest(null, null, null);
    HttpResponse actualDeleteResponse = client.putHttpRequest(null, null, null);

    Assert.assertNotNull(actualGetResponse);
    Assert.assertEquals("ok", actualGetResponse.getContent());
    Assert.assertNotNull(actualPostResponse);
    Assert.assertEquals("ok", actualPostResponse.getContent());
    Assert.assertNotNull(actualPutResponse);
    Assert.assertEquals("ok", actualPutResponse.getContent());
    Assert.assertNotNull(actualDeleteResponse);
    Assert.assertEquals("ok", actualDeleteResponse.getContent());
  }
}
