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

package io.servicecomb.loadbalance;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.loadbalancer.Server;

import io.servicecomb.core.Transport;
import io.servicecomb.serviceregistry.cache.CacheEndpoint;
import io.servicecomb.serviceregistry.cache.InstanceCache;
import io.servicecomb.serviceregistry.cache.InstanceCacheManager;
import mockit.Mock;
import mockit.MockUp;

public class TestServerListCache {

  private ServerListCache instance = null;

  private Transport transport = null;

  private void mockTestCases() {
    new MockUp<InstanceCacheManager>() {
      @Mock
      public InstanceCache getOrCreate(String appId, String microserviceName, String microserviceVersionRule) {
        return null;
      }
    };

    transport = Mockito.mock(Transport.class);
  }

  @Before
  public void setUp() throws Exception {
    mockTestCases();
    instance = new ServerListCache("appId", "microserviceName", "microserviceVersionRule", "transportName");
  }

  @After
  public void tearDown() throws Exception {
    instance = null;
  }

  @Test
  public void testServerListCache() {
    Assert.assertNotNull(instance);
  }

  @Test
  public void testCreateEndpointTransportString() {
    Server server = instance.createEndpoint(transport, new CacheEndpoint("stringAddress", null));
    Assert.assertNotNull(server);
  }
}
