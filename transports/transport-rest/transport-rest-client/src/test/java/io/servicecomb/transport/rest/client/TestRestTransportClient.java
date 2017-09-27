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

package io.servicecomb.transport.rest.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.foundation.vertx.VertxUtils;
import io.servicecomb.foundation.vertx.client.ClientPoolManager;
import io.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRestTransportClient {

  private RestTransportClient instance = null;

  private HttpClientOptions options;

  Invocation invocation = Mockito.mock(Invocation.class);

  AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

  OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

  RestOperationMeta swaggerRestOperation = Mockito.mock(RestOperationMeta.class);

  @Before
  public void setUp() throws Exception {
    instance = new RestTransportClient(false);
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
  public void testSSL(@Mocked Vertx vertx, @Mocked VertxUtils utils) throws Exception {
    new MockUp<VertxUtils>() {
      @Mock
      <CLIENT_POOL, CLIENT_OPTIONS> DeploymentOptions createClientDeployOptions(
          ClientPoolManager<CLIENT_POOL> clientMgr,
          int instanceCount,
          int poolCountPerVerticle, CLIENT_OPTIONS clientOptions) {
        options = (HttpClientOptions) clientOptions;
        return null;
      }

      @Mock
      <VERTICLE extends AbstractVerticle> boolean blockDeploy(Vertx vertx,
          Class<VERTICLE> cls,
          DeploymentOptions options) throws InterruptedException {
        return true;
      }
    };
    RestTransportClient client = new RestTransportClient(true);
    client.init(vertx);
    Assert.assertEquals(options.isSsl(), true);
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
  public void testCreateHttpClientOptions() {

    HttpClientOptions obj = (HttpClientOptions) Deencapsulation.invoke(instance, "createHttpClientOptions");
    Assert.assertNotNull(obj);
  }

  @Test
  public void testSend() {
    boolean validAssert;
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);

    new MockUp<ClientPoolManager<HttpClientWithContext>>() {

      @Mock
      public HttpClientWithContext findThreadBindClientPool() {
        return new HttpClientWithContext(null, null);
      }
    };

    Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(swaggerRestOperation);

    try {
      validAssert = true;
      instance.send(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }
    Assert.assertTrue(validAssert);
  }
}
