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

package org.apache.servicecomb.core.handler.impl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestSimpleLoadBalanceHandler {
  SimpleLoadBalanceHandler handler = new SimpleLoadBalanceHandler();

  Map<String, AtomicInteger> indexMap = Deencapsulation.getField(handler, "indexMap");

  @Mocked
  Invocation invocation;

  @Mocked
  ServiceRegistry serviceRegistry;

  @Mocked
  InstanceCacheManager instanceCacheManager;

  @Mocked
  TransportManager transportManager;

  VersionedCache instanceVersionedCache = new VersionedCache().data(Collections.emptyMap()).name("parent");

  Response response;

  AsyncResponse ar = resp -> {
    response = resp;
  };

  @Before
  public void setUp() throws Exception {
    CseContext.getInstance().setTransportManager(transportManager);

    RegistryUtils.setServiceRegistry(serviceRegistry);
    new Expectations() {
      {
        serviceRegistry.getInstanceCacheManager();
        result = instanceCacheManager;
        instanceCacheManager.getOrCreateVersionedCache(anyString, anyString, anyString);
        result = instanceVersionedCache;
        invocation.getConfigTransportName();
        result = "";
      }
    };
  }

  @After
  public void tearDown() throws Exception {
    CseContext.getInstance().setTransportManager(null);
    RegistryUtils.setServiceRegistry(null);
  }

  @Test
  public void handle_emptyEndPoint() throws Exception {
    handler.handle(invocation, ar);

    Throwable result = response.getResult();
    Assert.assertEquals("InvocationException: code=490;msg=CommonExceptionData [message=Cse Internal Bad Request]",
        result.getMessage());
    Assert.assertEquals("No available address found. microserviceName=null, version=null, discoveryGroupName=parent/",
        result.getCause().getMessage());
  }

  @Test
  public void handle(@Mocked Transport transport) throws Exception {
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setInstanceId("id");
    instance.getEndpoints().add("rest://localhost:8080");
    instance.getEndpoints().add("highway://localhost:8081");
    instanceVersionedCache.data(Collections.singletonMap("id", instance)).autoCacheVersion().name("vr");

    new Expectations() {
      {
        transportManager.findTransport(anyString);
        result = transport;
        invocation.getConfigTransportName();
        result = "";
      }
    };

    handler.handle(invocation, ar);
    AtomicInteger idx = indexMap.values().iterator().next();
    Assert.assertEquals(1, idx.get());

    handler.handle(invocation, ar);
    Assert.assertEquals(2, idx.get());
  }
}
