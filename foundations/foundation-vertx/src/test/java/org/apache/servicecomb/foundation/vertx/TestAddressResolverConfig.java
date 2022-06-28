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

package org.apache.servicecomb.foundation.vertx;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;

import io.vertx.core.dns.AddressResolverOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestAddressResolverConfig {

  @BeforeAll
  public static void classSetup() {
    ArchaiusUtils.resetConfig();
  }

  @AfterAll
  public static void classTeardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testGetResolverFromResource() {
    Configuration finalConfig = Mockito.mock(Configuration.class);
    ArchaiusUtils.resetConfig();
    ArchaiusUtils.setProperty("addressResolver.servers", "8.8.8.8,8.8.4.4");

    Mockito.when(finalConfig.getStringArray("addressResolver.servers")).thenReturn(new String[] {"6.6.6.6", "6.6.4.4"});
    Mockito.when(finalConfig.getStringArray("addressResolver.searchDomains")).thenReturn(new String[] {"default.svc.local.cluster"});
    Mockito.when(finalConfig.getInteger("addressResolver.queryTimeout", null)).thenReturn(2000);
    Mockito.when(finalConfig.getInteger("addressResolver.maxQueries", null)).thenReturn(-2);

    AddressResolverOptions resolverOptions = AddressResolverConfig.getAddressResover("test", finalConfig);
    Assertions.assertEquals(Arrays.asList("6.6.6.6", "6.6.4.4"), resolverOptions.getServers());
    Assertions.assertEquals(Collections.singletonList("default.svc.local.cluster"), resolverOptions.getSearchDomains());
    Assertions.assertEquals(resolverOptions.getQueryTimeout(),
        2000);
    Assertions.assertNotEquals(resolverOptions.getMaxQueries(),
        -2);
  }

  @Test
  public void testGetResolver() {
    ArchaiusUtils.resetConfig();
    ArchaiusUtils.setProperty("addressResolver.servers", "8.8.8.8,8.8.4.4");
    ArchaiusUtils.setProperty("addressResolver.optResourceEnabled", true);
    ArchaiusUtils.setProperty("addressResolver.cacheMinTimeToLive", 0);
    ArchaiusUtils.setProperty("addressResolver.cacheMaxTimeToLive", 10000);
    ArchaiusUtils.setProperty("addressResolver.cacheNegativeTimeToLive", 0);
    ArchaiusUtils.setProperty("addressResolver.queryTimeout", 1000);
    ArchaiusUtils.setProperty("addressResolver.maxQueries", 3);
    ArchaiusUtils.setProperty("addressResolver.test.maxQueries", 3);
    ArchaiusUtils.setProperty("addressResolver.rdFlag", true);
    ArchaiusUtils.setProperty("addressResolver.searchDomains",
        "default.svc.local.cluster,svc.local.cluster,local.cluster");
    ArchaiusUtils.setProperty("addressResolver.test.searchDomains",
        "test.svc.local.cluster,svc.local.cluster,local.cluster");
    ArchaiusUtils.setProperty("addressResolver.ndots", 3);
    ArchaiusUtils.setProperty("addressResolver.rotateServers", true);
    AddressResolverOptions aroc = AddressResolverConfig.getAddressResover("test");
    Assertions.assertEquals(Arrays.asList("8.8.8.8", "8.8.4.4"), aroc.getServers());
    Assertions.assertEquals(Arrays.asList("test.svc.local.cluster", "svc.local.cluster", "local.cluster"),
            aroc.getSearchDomains());
    AddressResolverOptions aroc1 = AddressResolverConfig.getAddressResover("test1");
    Assertions.assertEquals(Arrays.asList("default.svc.local.cluster", "svc.local.cluster", "local.cluster"),
            aroc1.getSearchDomains());
    Assertions.assertTrue(aroc.isOptResourceEnabled());
  }

  @Test
  public void testGetResolverDefault() {
    ArchaiusUtils.resetConfig();
    ArchaiusUtils.setProperty("addressResolver.servers", "8.8.8.8,8.8.4.4");
    ArchaiusUtils.setProperty("addressResolver.maxQueries", 3);
    ArchaiusUtils.setProperty("addressResolver.rdFlag", false);
    AddressResolverOptions resolverOptions = AddressResolverConfig.getAddressResover("test");
    Assertions.assertEquals(Arrays.asList("8.8.8.8", "8.8.4.4"), resolverOptions.getServers());
    Assertions.assertEquals(3, resolverOptions.getMaxQueries());
    Assertions.assertEquals(Integer.MAX_VALUE, resolverOptions.getCacheMaxTimeToLive());
    Assertions.assertFalse(resolverOptions.isOptResourceEnabled());
    Assertions.assertNull(resolverOptions.getSearchDomains());
  }
}
