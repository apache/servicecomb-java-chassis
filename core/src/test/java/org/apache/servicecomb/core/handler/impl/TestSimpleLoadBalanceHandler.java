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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryFilter;
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
  SimpleLoadBalanceHandler handler;

  Map<String, AtomicInteger> indexMap;

  @Mocked
  Invocation invocation;

  VersionedCache instanceVersionedCache = new VersionedCache().data(Collections.emptyMap()).name("parent");

  Response response;

  AsyncResponse ar = resp -> response = resp;

  SCBEngine scbEngine = new SCBBootstrap().useLocalRegistry().createSCBEngineForTest();

  @Before
  public void setUp() throws Exception {
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getSortedService(DiscoveryFilter.class);
        result = Collections.emptyList();
        invocation.getConfigTransportName();
        result = "";
      }
    };

    new Expectations(RegistryUtils.getInstanceCacheManager()) {
      {
        RegistryUtils.getInstanceCacheManager()
            .getOrCreateVersionedCache(anyString, anyString, anyString);
        result = instanceVersionedCache;
      }
    };

    handler = new SimpleLoadBalanceHandler();
    indexMap = Deencapsulation.getField(handler, "indexMap");
  }

  @After
  public void teardown() {
    scbEngine.destroy();
  }

  @Test
  public void handle_emptyEndPoint() throws Exception {
    handler.handle(invocation, ar);

    Throwable result = response.getResult();
    Assert.assertEquals("InvocationException: code=490;msg=CommonExceptionData [message=Unexpected consumer error, please check logs for details]",
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

    new Expectations(scbEngine.getTransportManager()) {
      {
        SCBEngine.getInstance().getTransportManager().findTransport(anyString);
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
