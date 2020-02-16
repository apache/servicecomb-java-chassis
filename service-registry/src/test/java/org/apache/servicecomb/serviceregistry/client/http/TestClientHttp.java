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

package org.apache.servicecomb.serviceregistry.client.http;

import java.util.concurrent.CountDownLatch;

import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.serviceregistry.client.Endpoints;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestClientHttp {
  private Microservice microservice = new Microservice();

  @SuppressWarnings("unchecked")
  @Test
  public void testServiceRegistryClientImpl(@Mocked IpPortManager manager) {
    IpPort ipPort = new IpPort("127.0.0.1", 8853);
    new Expectations() {
      {
        manager.getAvailableAddress();
        result = ipPort;
      }
    };
    new MockUp<RegistryUtils>() {
      @Mock
      Microservice getMicroservice() {
        return microservice;
      }
    };
    new MockUp<CountDownLatch>() {
      @Mock
      public void await() throws InterruptedException {
      }
    };
    new MockUp<RestClientUtil>() {
      @Mock
      void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
      }
    };
    new MockUp<WebsocketClientUtil>() {
      @Mock
      void open(IpPort ipPort, String url, Handler<Void> onOpen, Handler<Void> onClose,
          Handler<Buffer> onMessage, Handler<Throwable> onException,
          Handler<Throwable> onConnectFailed) {
      }
    };
    // mock up this two client pool, since this UT case doesn't require the client pool actually boot up.
    new MockUp<HttpClientPool>() {
      @Mock
      void create() {
      }
    };
    new MockUp<WebsocketClientPool>() {
      @Mock
      void create() {
      }
    };

    MicroserviceFactory microserviceFactory = new MicroserviceFactory();
    Microservice microservice = microserviceFactory.create("app", "ms");

    ServiceRegistryClientImpl oClient = new ServiceRegistryClientImpl(ServiceRegistryConfig.INSTANCE);
    oClient.init();
    oClient.registerMicroservice(microservice);
    oClient.registerMicroserviceInstance(microservice.getInstance());
    Assert.assertNull(oClient.getMicroservice(microservice.getServiceId()));
    Assert.assertNull(oClient.getMicroserviceInstance("testConsumerID", "testproviderID"));
    Assert.assertNull(oClient.findServiceInstance(microservice.getServiceId(),
        microservice.getAppId(),
        microservice.getServiceName(),
        microservice.getVersion()));
    Assert.assertNull(oClient.findServiceInstances(microservice.getServiceId(),
        microservice.getAppId(),
        microservice.getServiceName(),
        microservice.getVersion(),
        "0"));
    Assert.assertNull(oClient.getMicroserviceId(microservice.getAppId(),
        microservice.getServiceName(),
        microservice.getVersion(),
        microservice.getEnvironment()));
    Assert.assertNull(oClient.heartbeat(microservice.getServiceId(),
        microservice.getInstance().getInstanceId()));
    oClient.watch("",
        Mockito.mock(AsyncResultCallback.class));
    Assert.assertFalse(oClient.unregisterMicroserviceInstance(microservice.getServiceId(),
        microservice.getInstance().getInstanceId()));
  }

  @Test
  public void testRequestContext() {
    RequestContext oContext = new RequestContext();
    oContext.setUri("//test");
    oContext.setMethod(io.vertx.core.http.HttpMethod.POST);
    oContext.setIpPort(new IpPort("145.0.0.1", 8080));
    oContext.setParams(null);

    Assert.assertEquals("//test", oContext.getUri());
    Assert.assertEquals(io.vertx.core.http.HttpMethod.POST, oContext.getMethod());
    Assert.assertEquals(8080, oContext.getIpPort().getPort());
    Assert.assertNull(oContext.getParams());

    RestResponse oResponse = new RestResponse(null, null);
    oResponse.setRequestContext(oContext);
    Assert.assertEquals(oContext, oResponse.getRequestContext());
    Assert.assertNull(oResponse.getResponse());
  }

  @Test
  public void testRequestParam() {
    RequestParam oParam = new RequestParam();
    oParam.setCookies(null);
    oParam.setBody(null);
    oParam.setHeaders(null);
    oParam.addHeader("testKey", "testValue");
    oParam.addQueryParam("testParam", "ValueParam");
    oParam.addQueryParam("testParam1", "ValueParam");
    Assert.assertNull(oParam.getCookies());
    Assert.assertNull(oParam.getBody());
    Assert.assertNotEquals(null, oParam.getHeaders());
    Assert.assertNotEquals(null, oParam.getQueryParams());
    oParam.setQueryParams(null);
    Assert.assertEquals("", oParam.getQueryParams());
    oParam.setFormFields(null);
    Assert.assertNull(oParam.getFormFields());
    Endpoints oEndpoints = new Endpoints();
    oEndpoints.setInstances(null);
    oEndpoints.setVersion("1.0");
    Assert.assertNull(oEndpoints.getInstances());
    Assert.assertEquals("1.0", oEndpoints.getVersion());
  }

  @Test
  public void testIpPortManager() {
    IpPortManager oManager = new IpPortManager(ServiceRegistryConfig.INSTANCE);
    IpPort oIPPort = oManager.getNextAvailableAddress(new IpPort("", 33));
    Assert.assertEquals(oIPPort.getHostOrIp(), oManager.getAvailableAddress().getHostOrIp());
  }
}
