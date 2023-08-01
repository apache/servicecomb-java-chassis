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
package org.apache.servicecomb.registry;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.registry.api.AbstractDiscoveryInstance;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance.HistoryStatus;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance.IsolationStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestDiscoveryManager {

  @Test
  public void test_initial_service_list_correct() {
    MyDiscovery discovery1 = Mockito.mock(MyDiscovery.class);
    MyDiscoveryInstance instance1 = Mockito.mock(MyDiscoveryInstance.class);
    DiscoveryManager discoveryManager = new DiscoveryManager(List.of(discovery1));
    Mockito.when(discovery1.findServiceInstances("app", "service"))
        .thenReturn(List.of(instance1));
    Mockito.when(instance1.getInstanceId()).thenReturn("instance1");
    //first read
    VersionedCache versionedCache = discoveryManager.getOrCreateVersionedCache("app", "service");
    List<StatefulDiscoveryInstance> result = versionedCache.data();
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals("instance1", result.get(0).getInstanceId());
    Assertions.assertEquals(HistoryStatus.CURRENT, result.get(0).getHistoryStatus());
    // second read
    versionedCache = discoveryManager.getOrCreateVersionedCache("app", "service");
    result = versionedCache.data();
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals("instance1", result.get(0).getInstanceId());
    Assertions.assertEquals(HistoryStatus.CURRENT, result.get(0).getHistoryStatus());
  }

  @Test
  public void test_initial_empty_service_list_correct() {
    MyDiscovery discovery1 = Mockito.mock(MyDiscovery.class);
    DiscoveryManager discoveryManager = new DiscoveryManager(List.of(discovery1));
    Mockito.when(discovery1.findServiceInstances("app", "service"))
        .thenReturn(Collections.emptyList());
    //first read
    VersionedCache versionedCache = discoveryManager.getOrCreateVersionedCache("app", "service");
    List<StatefulDiscoveryInstance> result = versionedCache.data();
    Assertions.assertEquals(0, result.size());
    // second read
    discoveryManager.getOrCreateVersionedCache("app", "service");
    result = versionedCache.data();
    Assertions.assertEquals(0, result.size());
  }

  @Test
  public void test_isolate_service_instance_correct() {
    MyDiscovery discovery1 = Mockito.mock(MyDiscovery.class);
    MyDiscoveryInstance instance1 = Mockito.mock(MyDiscoveryInstance.class);
    DiscoveryManager discoveryManager = new DiscoveryManager(List.of(discovery1));
    Mockito.when(discovery1.findServiceInstances("app", "service"))
        .thenReturn(List.of(instance1));
    Mockito.when(instance1.getInstanceId()).thenReturn("instance1");
    Mockito.when(instance1.getApplication()).thenReturn("app");
    Mockito.when(instance1.getServiceName()).thenReturn("service");

    VersionedCache versionedCache = discoveryManager.getOrCreateVersionedCache("app", "service");
    List<StatefulDiscoveryInstance> result = versionedCache.data();
    discoveryManager.onInstanceIsolated(result.get(0), 10000L);
    versionedCache = discoveryManager.getOrCreateVersionedCache("app", "service");
    result = versionedCache.data();
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals("instance1", result.get(0).getInstanceId());
    Assertions.assertEquals(HistoryStatus.CURRENT, result.get(0).getHistoryStatus());
    Assertions.assertEquals(IsolationStatus.ISOLATED, result.get(0).getIsolationStatus());
  }

  static class MyDiscovery implements Discovery<MyDiscoveryInstance> {

    @Override
    public String name() {
      return "my";
    }

    @Override
    public boolean enabled(String application, String serviceName) {
      return false;
    }

    @Override
    public List<MyDiscoveryInstance> findServiceInstances(String application, String serviceName) {
      return null;
    }

    @Override
    public void setInstanceChangedListener(InstanceChangedListener<MyDiscoveryInstance> instanceChangedListener) {

    }

    @Override
    public void init() {

    }

    @Override
    public void run() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean enabled() {
      return false;
    }
  }

  static class MyDiscoveryInstance extends AbstractDiscoveryInstance {
    @Override
    public MicroserviceInstanceStatus getStatus() {
      return null;
    }

    @Override
    public String getDiscoveryName() {
      return null;
    }

    @Override
    public String getEnvironment() {
      return null;
    }

    @Override
    public String getApplication() {
      return null;
    }

    @Override
    public String getServiceName() {
      return null;
    }

    @Override
    public String getAlias() {
      return null;
    }

    @Override
    public String getVersion() {
      return null;
    }

    @Override
    public DataCenterInfo getDataCenterInfo() {
      return null;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Map<String, String> getProperties() {
      return null;
    }

    @Override
    public Map<String, String> getSchemas() {
      return null;
    }

    @Override
    public List<String> getEndpoints() {
      return null;
    }

    @Override
    public String getInstanceId() {
      return null;
    }
  }
}
