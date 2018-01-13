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

package org.apache.servicecomb.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.endpoint.EndpointsCache;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;
import org.apache.servicecomb.serviceregistry.cache.InstanceCache;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestTransport {
  @Test
  public void testEndpoint() throws Exception {
    Endpoint oEndpoint = new Endpoint(new Transport() {

      @Override
      public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
      }

      @Override
      public Object parseAddress(String address) {
        return "127.0.0.1";
      }

      @Override
      public boolean init() throws Exception {
        return true;
      }

      @Override
      public String getName() {
        return "test";
      }

      @Override
      public Endpoint getEndpoint() {
        return (new Endpoint(this, "testEndpoint"));
      }

      @Override
      public Endpoint getPublishEndpoint() throws Exception {
        return (new Endpoint(this, "testEndpoint"));
      }
    }, "rest://127.0.0.1:8080");
    oEndpoint.getTransport().init();
    Assert.assertEquals("rest://127.0.0.1:8080", oEndpoint.getEndpoint());
    Assert.assertEquals("127.0.0.1", oEndpoint.getAddress());
    Assert.assertEquals("test", oEndpoint.getTransport().getName());
    Assert.assertEquals("rest://127.0.0.1:8080", oEndpoint.getEndpoint().toString());
  }

  @Test
  public void testAbstractTransport(@Mocked Microservice microservice,
      @Injectable InstanceCacheManager instanceCacheManager, @Injectable TransportManager transportManager,
      @Mocked InstanceCache instanceCache, @Injectable MicroserviceInstance instance)
      throws Exception {
    EndpointsCache.init(instanceCacheManager, transportManager);
    EndpointsCache oEndpointsCache = new EndpointsCache("app", "testname", "test", "rest");

    List<Endpoint> endpoionts = oEndpointsCache.getLatestEndpoints();
    Assert.assertEquals(endpoionts.size(), 0);

    Map<String, List<CacheEndpoint>> allTransportMap = new HashMap<>();
    CacheEndpoint cacheEndpoint = new CacheEndpoint("rest://127.0.0.1:9999", instance);
    List<CacheEndpoint> restEndpoints = new ArrayList<>();
    restEndpoints.add(cacheEndpoint);
    allTransportMap.put("rest", restEndpoints);

    new Expectations() {
      {
        instanceCacheManager.getOrCreate(anyString, anyString, anyString);
        result = instanceCache;
        instanceCache.cacheChanged((InstanceCache) any);
        result = true;
        instanceCache.getOrCreateTransportMap();
        result = allTransportMap;
      }
    };

    endpoionts = oEndpointsCache.getLatestEndpoints();
    Assert.assertEquals(endpoionts.size(), 1);
  }
}
