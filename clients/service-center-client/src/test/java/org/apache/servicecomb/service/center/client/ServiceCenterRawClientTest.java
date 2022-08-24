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

package org.apache.servicecomb.service.center.client;

import java.io.IOException;
import java.util.Arrays;

import org.apache.servicecomb.http.client.common.HttpResponse;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;

/**
 * Created by   on 2019/10/16.
 */
public class ServiceCenterRawClientTest {

  private static final String PROJECT_NAME = "default";

  private static final String TENANT_NAME = "default";

  @Test
  public void TestDefaultParameter() throws IOException {

    HttpTransport httpTransport = Mockito.mock(HttpTransport.class);
    ServiceCenterAddressManager addressManager = new ServiceCenterAddressManager(PROJECT_NAME, Arrays.asList("http://127.0.0.1:30100"), new EventBus());
    ServiceCenterRawClient client = new ServiceCenterRawClient.Builder()
        .setHttpTransport(httpTransport)
        .setAddressManager(addressManager)
        .setTenantName(TENANT_NAME)
        .build();

    HttpResponse httpResponse = new HttpResponse();
    httpResponse.setStatusCode(200);
    httpResponse.setContent("ok");

    Mockito.when(httpTransport.doRequest(Mockito.any())).thenReturn(httpResponse);

    HttpResponse actualGetResponse = client.getHttpRequest(null, null, null);
    HttpResponse actualPostResponse = client.postHttpRequest(null, null, null);
    HttpResponse actualPutResponse = client.putHttpRequest(null, null, null);
    HttpResponse actualDeleteResponse = client.putHttpRequest(null, null, null);

    Assertions.assertNotNull(actualGetResponse);
    Assertions.assertEquals("ok", actualGetResponse.getContent());
    Assertions.assertNotNull(actualPostResponse);
    Assertions.assertEquals("ok", actualPostResponse.getContent());
    Assertions.assertNotNull(actualPutResponse);
    Assertions.assertEquals("ok", actualPutResponse.getContent());
    Assertions.assertNotNull(actualDeleteResponse);
    Assertions.assertEquals("ok", actualDeleteResponse.getContent());
  }
}
