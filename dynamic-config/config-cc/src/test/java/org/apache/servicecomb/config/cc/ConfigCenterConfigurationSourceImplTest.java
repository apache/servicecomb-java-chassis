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

package org.apache.servicecomb.config.cc;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.config.center.client.ConfigCenterAddressManager;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigCenterConfigurationSourceImplTest {
  @Test
  void configAddressManagerTest() throws IllegalAccessException, NoSuchFieldException {
    List<String> addresses = new ArrayList<>();
    addresses.add("http://127.0.0.1:30103");
    addresses.add("http://127.0.0.2:30103");
    ConfigCenterAddressManager addressManager = new ConfigCenterAddressManager("test", addresses, EventManager.getEventBus());
    Field addressManagerField = addressManager.getClass().getSuperclass().getDeclaredField("index");
    addressManagerField.setAccessible(true);
    addressManagerField.set(addressManager, 0);
    Assertions.assertNotNull(addressManager);

    String address = addressManager.address();
    Assertions.assertEquals("http://127.0.0.2:30103/v3/test", address);
    address = addressManager.address();
    Assertions.assertEquals("http://127.0.0.1:30103/v3/test", address);

    addressManager = new ConfigCenterAddressManager(null, addresses, EventManager.getEventBus());
    addressManagerField = addressManager.getClass().getSuperclass().getDeclaredField("index");
    addressManagerField.setAccessible(true);
    addressManagerField.set(addressManager, 0);
    address = addressManager.address();
    Assertions.assertEquals("http://127.0.0.2:30103/v3/default", address);
  }

  @Test
  void onRefreshEndpointEventTest() {
    List<String> addresses = new ArrayList<>();
    addresses.add("http://127.0.0.1:30103");
    List<String> addressAZ = new ArrayList<>();
    addressAZ.add("rest://127.0.0.1:30100?sslEnabled=true");
    Map<String, List<String>> zoneAndRegion = new HashMap<>();
    zoneAndRegion.put("sameZone", addressAZ);
    zoneAndRegion.put("sameRegion", new ArrayList<>());
    RefreshEndpointEvent event = new RefreshEndpointEvent(zoneAndRegion, "CseConfigCenter");
    ConfigCenterAddressManager addressManager = new ConfigCenterAddressManager("test", addresses, EventManager.getEventBus());
    addressManager.onRefreshEndpointEvent(event);

    List<String> availableAZ = addressManager.getAvailableZone();
    Assertions.assertEquals("https://127.0.0.1:30100/v3/test", availableAZ.get(0));
  }
}
