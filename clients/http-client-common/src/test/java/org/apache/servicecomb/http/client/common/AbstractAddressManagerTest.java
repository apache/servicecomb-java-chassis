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

package org.apache.servicecomb.http.client.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import mockit.Deencapsulation;
import mockit.Expectations;

public class AbstractAddressManagerTest {

  private static List<String> addresses = new ArrayList<>();

  private static AbstractAddressManager addressManager1;

  private static AbstractAddressManager addressManager2;

  private static AbstractAddressManager addressManager3;

  @BeforeEach
  public void setUp() {
    addresses.add("http://127.0.0.1:30103");
    addresses.add("https://127.0.0.2:30103");
    addressManager1 = new AbstractAddressManager(addresses);
    addressManager2 = new AbstractAddressManager("project", addresses);
    addressManager3 = new AbstractAddressManager(null, addresses);
  }

  @AfterEach
  public void tearDown() {
    addresses.clear();
    addressManager1 = null;
    addressManager2 = null;
    addressManager3 = null;
  }

  @Test
  public void abstractAddressManagerTest() {
    Assert.assertNotNull(addressManager1);
    Assert.assertNotNull(addressManager2);
    Assert.assertNotNull(addressManager3);

    Assert.assertEquals("https://127.0.0.2:30103", addressManager1.address());
    Assert.assertEquals("http://127.0.0.1:30103", addressManager1.address());
  }

  @Test
  public void formatUrlTest() {
    AddressStatus addressStatus = addressManager2.formatUrl("/test", true);
    Assert.assertNotNull(addressStatus);
    Assert.assertEquals("https://127.0.0.2:30103/v3/project", addressStatus.getCurrentAddress());
    Assert.assertEquals("https://127.0.0.2:30103/v3/project/test", addressStatus.getUrl());

    addressStatus = addressManager3.formatUrl("/test", true);
    Assert.assertNotNull(addressStatus);
    Assert.assertEquals("https://127.0.0.2:30103/v3/default", addressStatus.getCurrentAddress());
    Assert.assertEquals("https://127.0.0.2:30103/v3/default/test", addressStatus.getUrl());

    addressStatus = addressManager3.formatUrl("/test", false);
    Assert.assertNotNull(addressStatus);
    Assert.assertEquals("http://127.0.0.1:30103/v3/default", addressStatus.getCurrentAddress());
    Assert.assertEquals("http://127.0.0.1:30103/v3/default/v3/default/test", addressStatus.getUrl());
  }

  @Test
  public void recordStateTest() throws ExecutionException {
    List<String> addressAZ = new ArrayList<>();
    addressAZ.add("http://127.0.0.3:30100");
    List<String> addressRG = new ArrayList<>();
    addressRG.add("http://127.0.0.4:30100");
    Map<String, List<String>> zoneAndRegion = new HashMap<>();
    zoneAndRegion.put("sameZone", addressAZ);
    zoneAndRegion.put("sameRegion", addressRG);
    RefreshEndpointEvent event = new RefreshEndpointEvent(zoneAndRegion, "TEST");
    AbstractAddressManager addressManager = new AbstractAddressManager(addresses);

    addressManager.refreshEndpoint(event, "TEST");

    AddressStatus addressStatus1 = new AddressStatus(null, "http://127.0.0.3:30100");
    addressManager.recordFailState(addressStatus1);

    Assert.assertEquals("http://127.0.0.3:30100", addressManager.address());

    addressManager.recordFailState(addressStatus1);
    Assert.assertEquals("http://127.0.0.3:30100", addressManager.address());

    // test fail 2 times ,it will not be isolated
    addressManager.recordSuccessState(addressStatus1);
    Assert.assertEquals("http://127.0.0.3:30100", addressManager.address());

    // test recodeStatus times
    Map<String, Integer> recodeStatus = Deencapsulation.getField(addressManager, "recodeStatus");
    Assert.assertEquals(1, (int) recodeStatus.get("http://127.0.0.3:30100"));

    // test fail 3 times ,it will be isolated
    addressManager.recordFailState(addressStatus1);
    addressManager.recordFailState(addressStatus1);
    addressManager.recordFailState(addressStatus1);
    Assert.assertEquals("http://127.0.0.4:30100", addressManager.address());
  }

  @Test
  public void addressForOnlyDefaultTest() {
    Assert.assertEquals("https://127.0.0.2:30103", addressManager1.address());
    Assert.assertEquals("http://127.0.0.1:30103", addressManager1.address());

    Assert.assertEquals("https://127.0.0.2:30103/v3/project", addressManager2.address());
    Assert.assertEquals("http://127.0.0.1:30103/v3/project", addressManager2.address());

    Assert.assertEquals("https://127.0.0.2:30103/v3/default", addressManager3.address());
    Assert.assertEquals("http://127.0.0.1:30103/v3/default", addressManager3.address());
  }

  @Test
  public void addressForOnlyAzTest() {
    List<String> addressAZ = new ArrayList<>();
    addressAZ.add("http://127.0.0.1:30100");
    addressAZ.add("https://127.0.0.2:30100");
    addressAZ.add("rest://127.0.0.1:30100?sslEnabled=true");
    addressAZ.add("rest://127.0.0.2:30100");

    Map<String, List<String>> zoneAndRegion = new HashMap<>();
    zoneAndRegion.put("sameZone", addressAZ);
    zoneAndRegion.put("sameRegion", new ArrayList<>());
    RefreshEndpointEvent event1 = new RefreshEndpointEvent(zoneAndRegion, "TEST");
    addressManager1.refreshEndpoint(event1, "TEST");

    Assert.assertEquals("https://127.0.0.2:30100", addressManager1.address());
    Assert.assertEquals("https://127.0.0.1:30100?sslEnabled=true", addressManager1.address());
    Assert.assertEquals("http://127.0.0.2:30100", addressManager1.address());
    Assert.assertEquals("http://127.0.0.1:30100", addressManager1.address());
    Assert.assertEquals("https://127.0.0.2:30100", addressManager1.address());
  }

  @Test
  public void addressForOnlyRegionTest() {
    List<String> addressRG = new ArrayList<>();
    addressRG.add("rest://127.0.0.5:30100?sslEnabled=true");
    addressRG.add("rest://127.0.0.6:30100");
    addressRG.add("http://127.0.0.7:30100");
    addressRG.add("https://127.0.0.8:30100");
    Map<String, List<String>> zoneAndRegion = new HashMap<>();
    zoneAndRegion.put("sameZone", new ArrayList<>());
    zoneAndRegion.put("sameRegion", addressRG);
    RefreshEndpointEvent event = new RefreshEndpointEvent(zoneAndRegion, "TEST");
    addressManager1.refreshEndpoint(event, "TEST");

    Assert.assertEquals("http://127.0.0.6:30100", addressManager1.address());
    Assert.assertEquals("http://127.0.0.7:30100", addressManager1.address());
    Assert.assertEquals("https://127.0.0.8:30100", addressManager1.address());
    Assert.assertEquals("https://127.0.0.5:30100?sslEnabled=true", addressManager1.address());
    Assert.assertEquals("http://127.0.0.6:30100", addressManager1.address());
  }

  @Test
  public void addressForAzAndRegionTest() {
    List<String> addressAZ = new ArrayList<>();
    addressAZ.add("rest://127.0.0.1:30100?sslEnabled=true");
    addressAZ.add("https://127.0.0.2:30100");
    List<String> addressRG = new ArrayList<>();
    addressRG.add("rest://127.0.0.3:30100?sslEnabled=true");
    addressRG.add("https://127.0.0.4:30100");
    Map<String, List<String>> zoneAndRegion = new HashMap<>();
    zoneAndRegion.put("sameZone", addressAZ);
    zoneAndRegion.put("sameRegion", addressRG);
    RefreshEndpointEvent event = new RefreshEndpointEvent(zoneAndRegion, "TEST");
    addressManager1.refreshEndpoint(event, "TEST");

    Assert.assertEquals("https://127.0.0.2:30100", addressManager1.address());
    Assert.assertEquals("https://127.0.0.1:30100?sslEnabled=true", addressManager1.address());
    Assert.assertEquals("https://127.0.0.2:30100", addressManager1.address());

    addressManager1.removeAddress("https://127.0.0.2:30100");
    addressManager1.removeAddress("https://127.0.0.1:30100?sslEnabled=true");
    Assert.assertEquals("https://127.0.0.3:30100?sslEnabled=true", addressManager1.address());
    Assert.assertEquals("https://127.0.0.4:30100", addressManager1.address());
    Assert.assertEquals("https://127.0.0.3:30100?sslEnabled=true", addressManager1.address());

    addressManager1.removeAddress("https://127.0.0.4:30100");
    addressManager1.removeAddress("https://127.0.0.3:30100?sslEnabled=true");
    Assert.assertEquals("https://127.0.0.2:30103", addressManager1.address());
    Assert.assertEquals("http://127.0.0.1:30103", addressManager1.address());
    Assert.assertEquals("https://127.0.0.2:30103", addressManager1.address());
  }

  @Test
  public void sslEnabledTest() {
    Assert.assertEquals(true, addressManager1.sslEnabled());
    Assert.assertEquals(false, addressManager1.sslEnabled());
    Assert.assertEquals(true, addressManager1.sslEnabled());

    Assert.assertEquals(true, addressManager2.sslEnabled());
    Assert.assertEquals(false, addressManager2.sslEnabled());
    Assert.assertEquals(true, addressManager2.sslEnabled());
  }

  @Test
  public void transformAddressTest() {
    List<String> address = new ArrayList<>();
    address.add("rest://127.0.0.1:30100?sslEnabled=true");
    address.add("rest://127.0.0.2:30100");
    address.add("http://127.0.0.3:30100");
    address.add("https://127.0.0.4:30100");

    List<String> formAddress = addressManager2.transformAddress(address);

    Assert.assertEquals("rest://127.0.0.1:30100?sslEnabled=true/v3/project", formAddress.get(0));
    Assert.assertEquals("rest://127.0.0.2:30100/v3/project", formAddress.get(1));
    Assert.assertEquals("http://127.0.0.3:30100/v3/project", formAddress.get(2));
    Assert.assertEquals("https://127.0.0.4:30100/v3/project", formAddress.get(3));

    formAddress = addressManager3.transformAddress(address);
    Assert.assertEquals("rest://127.0.0.1:30100?sslEnabled=true/v3/default", formAddress.get(0));
    Assert.assertEquals("rest://127.0.0.2:30100/v3/default", formAddress.get(1));
    Assert.assertEquals("http://127.0.0.3:30100/v3/default", formAddress.get(2));
    Assert.assertEquals("https://127.0.0.4:30100/v3/default", formAddress.get(3));
  }

  @Test
  public void getUrlPrefixTest() {
    Assert.assertEquals("http://127.0.0.3:30100/v3/", addressManager2.getUrlPrefix("http://127.0.0.3:30100"));
    Assert.assertEquals("http://127.0.0.3:30100/v3/", addressManager3.getUrlPrefix("http://127.0.0.3:30100"));
  }

  @Test
  public void refreshEndpointTest() {
    List<String> addressAZ = new ArrayList<>();
    addressAZ.add("rest://127.0.0.1:30100");
    Map<String, List<String>> zoneAndRegion = new HashMap<>();
    zoneAndRegion.put("sameZone", addressAZ);
    zoneAndRegion.put("sameRegion", new ArrayList<>());
    RefreshEndpointEvent event = new RefreshEndpointEvent(zoneAndRegion, "TEST");

    addressManager1.refreshEndpoint(event, "KIE");
    Assert.assertEquals("https://127.0.0.2:30103", addressManager1.address());
    Assert.assertEquals("http://127.0.0.1:30103", addressManager1.address());
    Assert.assertEquals("https://127.0.0.2:30103", addressManager1.address());

    addressManager2.refreshEndpoint(event, "TEST");
    Assert.assertEquals("http://127.0.0.1:30100", addressManager2.address());
    Assert.assertEquals("http://127.0.0.1:30100", addressManager2.address());
  }
}