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

package org.apache.servicecomb.config.center.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.google.common.eventbus.EventBus;

import mockit.Deencapsulation;

class AddressManagerTest {
  private static List<String> addresses = new ArrayList<>();

  private static AddressManager addressManager1;

  private static AddressManager addressManager2;

  @Test
  public void addressManagerTest() {
    addresses.add("http://127.0.0.1:30103");
    addresses.add("https://127.0.0.2:30103");
    addressManager1 = new AddressManager("project", addresses, new EventBus());
    addressManager2 = new AddressManager(null, addresses, new EventBus());

    Assert.assertNotNull(addressManager1);
    Assert.assertNotNull(addressManager2);

    List<String> addresses = Deencapsulation.getField(addressManager1, "addresses");
    Assert.assertEquals(2, addresses.size());
    Assert.assertEquals("http://127.0.0.1:30103/v3/project", addresses.get(0));

    Assert.assertEquals("https://127.0.0.2:30103/v3/project", addressManager1.address());
    Assert.assertEquals("http://127.0.0.1:30103/v3/project", addressManager1.address());
    Assert.assertEquals("https://127.0.0.2:30103/v3/default", addressManager2.address());
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
    addressManager1 = new AddressManager("project", addresses, new EventBus());
    RefreshEndpointEvent event = new RefreshEndpointEvent(zoneAndRegion, "CseConfigCenter");
    addressManager1.refreshEndpoint(event, "CseConfigCenter");

    List<String> availableZone = Deencapsulation.getField(addressManager1, "availableZone");
    Assert.assertEquals("http://127.0.0.3:30100/v3/project", availableZone.get(0));

    List<String> availableRegion = Deencapsulation.getField(addressManager1, "availableRegion");
    Assert.assertEquals("http://127.0.0.4:30100/v3/project", availableRegion.get(0));

    Assert.assertEquals("http://127.0.0.3:30100/v3/project", addressManager1.address());
  }
}