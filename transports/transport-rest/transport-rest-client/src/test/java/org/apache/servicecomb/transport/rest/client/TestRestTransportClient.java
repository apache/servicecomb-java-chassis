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

package org.apache.servicecomb.transport.rest.client;

import java.lang.reflect.Field;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.ClientPoolManager;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.impl.HttpClientImpl;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRestTransportClient {

  private RestTransportClient instance = null;

  Invocation invocation = Mockito.mock(Invocation.class);

  AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

  OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

  RestOperationMeta swaggerRestOperation = Mockito.mock(RestOperationMeta.class);

  URIEndpointObject uriEndpointObject = Mockito.mock(URIEndpointObject.class);

  Endpoint endPoint = Mockito.mock(Endpoint.class);

  HttpClientWithContext httpClientWithContext = Mockito.mock(HttpClientWithContext.class);

  HttpClientImpl httpClient = Mockito.mock(HttpClientImpl.class);

  HttpClientOptions httpClientOptions = Mockito.mock(HttpClientOptions.class);


  @Before
  public void setUp() throws Exception {
    instance = new RestTransportClient();
  }

  @After
  public void tearDown() throws Exception {
    instance = null;
  }

  @Test
  public void testGetInstance() {
    Assert.assertNotNull(instance);
  }

  @Test
  public void init(@Mocked Vertx vertx, @Mocked VertxUtils utils) throws Exception {
    new MockUp<VertxUtils>() {
      @Mock
      <VERTICLE extends AbstractVerticle> boolean blockDeploy(Vertx vertx,
          Class<VERTICLE> cls,
          DeploymentOptions options) throws InterruptedException {
        return true;
      }
    };
    RestTransportClient client = new RestTransportClient();
    client.init(vertx);

    ClientPoolManager<HttpClientWithContext> clientMgr = Deencapsulation.getField(client, "clientMgr");
    Assert.assertSame(vertx, Deencapsulation.getField(clientMgr, "vertx"));
  }

  @Test
  public void testRestTransportClientException() {
    boolean status = true;
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(operationMeta);
    try {
      instance.send(invocation, asyncResp);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertFalse(status);
  }


  @Test
  public void testRestTransportClientHttp2(@Mocked Vertx vertx, @Mocked VertxUtils utils)
      throws Exception {
    boolean status = true;
    Mockito.when(invocation.getEndpoint()).thenReturn(endPoint);
    Mockito.when(invocation.isSync()).thenReturn(true);
    Mockito.when(endPoint.getAddress()).thenReturn(uriEndpointObject);
    Mockito.when(uriEndpointObject.isHttp2Enabled()).thenReturn(true);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(operationMeta);
    Mockito.when(httpClientWithContext.getHttpClient()).thenReturn(httpClient);
    Mockito.when(httpClient.getOptions()).thenReturn(httpClientOptions);

    instance.init(vertx);
    Field clientMgrHttp2Field = instance.getClass().getDeclaredField("clientMgrHttp2");
    clientMgrHttp2Field.setAccessible(true);

    ClientPoolManager<HttpClientWithContext> client = new ClientPoolManager<HttpClientWithContext>(vertx, null) {
      @Mock
      public HttpClientWithContext findClientPool(boolean sync) {
        return httpClientWithContext;
      }
    };

    clientMgrHttp2Field.set(instance, client);

    try {
      instance.send(invocation, asyncResp);
    } catch (Exception e) {
      status = false;
    }

    Assert.assertTrue(status);
  }

  @Test
  public void testCreateHttpClientOptions() {
    HttpClientOptions obj = Deencapsulation.invoke(instance, "createHttpClientOptions", false);
    Assert.assertNotNull(obj);
  }
}
