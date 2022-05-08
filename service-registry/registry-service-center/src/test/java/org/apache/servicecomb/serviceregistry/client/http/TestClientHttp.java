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

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.client.Endpoints;
import org.apache.servicecomb.serviceregistry.client.IpPortManager;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestClientHttp {
  private final Microservice microservice = new Microservice();

  @SuppressWarnings("unchecked")
  @Test
  public void testServiceRegistryClientImpl(@Mocked IpPortManager manager) {
    Configuration configuration = ConfigUtil.createLocalConfig();
    configuration.setProperty(BootStrapProperties.CONFIG_SERVICE_APPLICATION, "app");
    configuration.setProperty(BootStrapProperties.CONFIG_SERVICE_NAME, "ms");
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
    Microservice microservice = microserviceFactory.create(configuration);

    ServiceRegistryClientImpl oClient = new ServiceRegistryClientImpl(ServiceRegistryConfig.INSTANCE);
    oClient.init();
    oClient.registerMicroservice(microservice);
    oClient.registerMicroserviceInstance(microservice.getInstance());
    Assertions.assertNull(oClient.getMicroservice(microservice.getServiceId()));
    Assertions.assertNull(oClient.getMicroserviceInstance("testConsumerID", "testproviderID"));
    Assertions.assertNull(oClient.findServiceInstance(microservice.getServiceId(),
        microservice.getAppId(),
        microservice.getServiceName(),
        microservice.getVersion()));
    Assertions.assertNull(oClient.findServiceInstances(microservice.getServiceId(),
        microservice.getAppId(),
        microservice.getServiceName(),
        microservice.getVersion(),
        "0"));
    Assertions.assertNull(oClient.getMicroserviceId(microservice.getAppId(),
        microservice.getServiceName(),
        microservice.getVersion(),
        microservice.getEnvironment()));
    Assertions.assertNull(oClient.heartbeat(microservice.getServiceId(),
        microservice.getInstance().getInstanceId()));
    oClient.watch("",
        Mockito.mock(AsyncResultCallback.class));
    Assertions.assertFalse(oClient.unregisterMicroserviceInstance(microservice.getServiceId(),
        microservice.getInstance().getInstanceId()));
  }

  @Test
  public void testRequestContext() {
    RequestContext oContext = new RequestContext();
    oContext.setUri("//test");
    oContext.setMethod(io.vertx.core.http.HttpMethod.POST);
    oContext.setIpPort(new IpPort("145.0.0.1", 8080));
    oContext.setParams(null);

    Assertions.assertEquals("//test", oContext.getUri());
    Assertions.assertEquals(io.vertx.core.http.HttpMethod.POST, oContext.getMethod());
    Assertions.assertEquals(8080, oContext.getIpPort().getPort());
    Assertions.assertNull(oContext.getParams());

    RestResponse oResponse = new RestResponse(null, null);
    oResponse.setRequestContext(oContext);
    Assertions.assertEquals(oContext, oResponse.getRequestContext());
    Assertions.assertNull(oResponse.getResponse());
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
    Assertions.assertNull(oParam.getCookies());
    Assertions.assertNull(oParam.getBody());
    Assertions.assertNotEquals(null, oParam.getHeaders());
    Assertions.assertNotEquals(null, oParam.getQueryParams());
    oParam.setQueryParams(null);
    Assertions.assertEquals("", oParam.getQueryParams());
    oParam.setFormFields(null);
    Assertions.assertNull(oParam.getFormFields());
    Endpoints oEndpoints = new Endpoints();
    oEndpoints.setInstances(null);
    oEndpoints.setVersion("1.0");
    Assertions.assertNull(oEndpoints.getInstances());
    Assertions.assertEquals("1.0", oEndpoints.getVersion());
  }

  @Test
  public void testIpPortManager() {
    IpPortManager oManager = new IpPortManager(ServiceRegistryConfig.INSTANCE);
    IpPort oIPPort = oManager.getAvailableAddress();
    Assertions.assertEquals(oIPPort.getHostOrIp(), oManager.getAvailableAddress().getHostOrIp());
  }
}
