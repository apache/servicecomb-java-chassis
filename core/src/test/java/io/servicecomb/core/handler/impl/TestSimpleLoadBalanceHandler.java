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

package io.servicecomb.core.handler.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.endpoint.EndpointsCache;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.swagger.invocation.AsyncResponse;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestSimpleLoadBalanceHandler {
  MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();

  SimpleLoadBalanceHandler simpleLoadBalanceHandler = null;

  Invocation invocation = null;

  AsyncResponse asyncResp = null;

  public void mock() {

    Microservice microService = new Microservice();
    microService.setAppId("100");
    new MockUp<RegistryUtils>() {
      @Mock
      public Microservice getMicroservice() {
        return microService;
      }
    };
  }

  @Before
  public void setUp() throws Exception {
    simpleLoadBalanceHandler = new SimpleLoadBalanceHandler();
    invocation = Mockito.mock(Invocation.class);
    asyncResp = Mockito.mock(AsyncResponse.class);
  }

  @After
  public void tearDown() throws Exception {
    simpleLoadBalanceHandler = null;
    invocation = null;
    asyncResp = null;
  }

  @Test
  public void testHandler() {
    boolean status = false;
    mock();
    Assert.assertNotNull(simpleLoadBalanceHandler);
    try {
      Mockito.when(invocation.getMicroserviceName()).thenReturn(microserviceMetaManager.getName());
      Mockito.when(invocation.getMicroserviceVersionRule()).thenReturn("MicroserviceVersionRule");
      Mockito.when(invocation.getConfigTransportName()).thenReturn("TransportName");
      simpleLoadBalanceHandler.handle(invocation, asyncResp);
    } catch (Exception e) {
      status = true;
    }
    Assert.assertTrue(status);
  }

  @Test
  public void testHandlerWithMap() {
    boolean status = false;
    mock();
    Assert.assertNotNull(simpleLoadBalanceHandler);
    try {
      Mockito.when(invocation.getMicroserviceName()).thenReturn("MicroserviceName");
      Mockito.when(invocation.getMicroserviceVersionRule()).thenReturn("MicroserviceVersionRule");
      Mockito.when(invocation.getConfigTransportName()).thenReturn("TransportName");
      Map<String, EndpointsCache> endpointsCacheMap = new ConcurrentHashMap<>();
      endpointsCacheMap.put("TransportName", Mockito.mock(EndpointsCache.class));
      Deencapsulation.setField(simpleLoadBalanceHandler, "endpointsCacheMap", endpointsCacheMap);
      simpleLoadBalanceHandler.handle(invocation, asyncResp);
    } catch (Exception e) {
      status = true;
    }
    Assert.assertFalse(status);
  }
}
