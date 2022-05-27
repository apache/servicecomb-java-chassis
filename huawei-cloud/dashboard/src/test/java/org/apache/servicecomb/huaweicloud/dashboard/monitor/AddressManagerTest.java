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

package org.apache.servicecomb.huaweicloud.dashboard.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.eventbus.EventBus;

import mockit.Deencapsulation;

class AddressManagerTest {

  private static final List<String> addresses = new ArrayList<>();

  private static AddressManager addressManager1;

  @Test
  public void kieAddressManagerTest() {
    addresses.add("http://127.0.0.1:30103");
    addresses.add("https://127.0.0.2:30103");
    addressManager1 = new AddressManager(addresses, new EventBus());

    Assertions.assertNotNull(addressManager1);

    List<String> addresses = Deencapsulation.getField(addressManager1, "addresses");
    Assertions.assertEquals(2, addresses.size());
    Assertions.assertEquals("http://127.0.0.1:30103", addresses.get(0));

    Assertions.assertEquals("https://127.0.0.2:30103", addressManager1.address());
    Assertions.assertEquals("http://127.0.0.1:30103", addressManager1.address());
  }


  @Test
  public void onRefreshEndpointEvent() {
    List<String> addressAZ = new ArrayList<>();
    addressAZ.add("http://127.0.0.3:30100");
    List<String> addressRG = new ArrayList<>();
    addressRG.add("http://127.0.0.4:30100");
    Map<String, List<String>> zoneAndRegion = new HashMap<>();
    zoneAndRegion.put("sameZone", addressAZ);
    zoneAndRegion.put("sameRegion", addressRG);
    addressManager1 = new AddressManager(addresses, new EventBus());
    RefreshEndpointEvent event = new RefreshEndpointEvent(zoneAndRegion, "CseMonitoring");
    addressManager1.refreshEndpoint(event, "CseMonitoring");

    List<String> availableZone = Deencapsulation.getField(addressManager1, "availableZone");
    Assertions.assertEquals("http://127.0.0.3:30100", availableZone.get(0));

    List<String> availableRegion = Deencapsulation.getField(addressManager1, "availableRegion");
    Assertions.assertEquals("http://127.0.0.4:30100", availableRegion.get(0));
  }
}
