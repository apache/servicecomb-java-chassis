/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.client.http;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;

public class TestVertxGetMethod {

  @Test
  public void testVertxGetMethod() {

    HttpClient client = Mockito.mock(HttpClient.class);
    Invocation invocation = Mockito.mock(Invocation.class);
    IpPort ipPort = Mockito.mock(IpPort.class);
    Mockito.when(ipPort.getPort()).thenReturn(10);
    assertEquals(10, ipPort.getPort());
    Mockito.when(ipPort.getHostOrIp()).thenReturn("ever");
    assertEquals("ever", ipPort.getHostOrIp());
    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
    HttpClientRequest obj =
        VertxGetMethod.INSTANCE.createRequest(client, invocation, ipPort, "good", asyncResp);
    Assert.assertNull(obj);
  }
}
