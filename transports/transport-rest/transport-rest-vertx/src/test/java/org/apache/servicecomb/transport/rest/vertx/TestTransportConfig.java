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

package org.apache.servicecomb.transport.rest.vertx;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTransportConfig {

  @Before
  public void before() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void after() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testGetAddressNull() {
    Assert.assertNull(TransportConfig.getAddress());
  }

  @Test
  public void testGetAddressNormal() {
    ArchaiusUtils.setProperty("servicecomb.rest.address", "1.1.1.1");
    Assert.assertEquals("1.1.1.1", TransportConfig.getAddress());
  }

  @Test
  public void testGetThreadCountNull() {
    Assert.assertEquals(1, TransportConfig.getThreadCount());
  }

  @Test
  public void testGetThreadCountNormal() {
    ArchaiusUtils.setProperty("servicecomb.rest.server.thread-count", 10);
    Assert.assertEquals(10, TransportConfig.getThreadCount());
  }

  @Test
  public void testGetCompressedAndHeaderSize() {
    ArchaiusUtils.setProperty("servicecomb.rest.server.compression", true);
    Assert.assertEquals(true, TransportConfig.getCompressed());
    ArchaiusUtils.setProperty("servicecomb.rest.server.maxHeaderSize", 2048);
    Assert.assertEquals(2048, TransportConfig.getMaxHeaderSize());
  }

  @Test
  public void testIsCorsEnabled() {
    Assert.assertFalse(TransportConfig.isCorsEnabled());
    ArchaiusUtils.setProperty("servicecomb.cors.enabled", true);
    Assert.assertTrue(TransportConfig.isCorsEnabled());
    ArchaiusUtils.setProperty("servicecomb.cors.enabled", false);
    Assert.assertFalse(TransportConfig.isCorsEnabled());
  }

  @Test
  public void testGetCorsAllowedOrigin() {
    Assert.assertEquals("*", TransportConfig.getCorsAllowedOrigin());
    String origin = "http://localhost:8080";
    ArchaiusUtils.setProperty("servicecomb.cors.origin", origin);
    Assert.assertEquals(origin, TransportConfig.getCorsAllowedOrigin());
  }

  @Test
  public void testIsCorsAllowCredentials() {
    Assert.assertFalse(TransportConfig.isCorsAllowCredentials());
    ArchaiusUtils.setProperty("servicecomb.cors.allowCredentials", true);
    Assert.assertTrue(TransportConfig.isCorsAllowCredentials());
    ArchaiusUtils.setProperty("servicecomb.cors.allowCredentials", false);
    Assert.assertFalse(TransportConfig.isCorsAllowCredentials());
  }

  @Test
  public void testGetCorsAllowedHeaders() {
    String configKey = "servicecomb.cors.allowedHeader";
    Assert.assertTrue(TransportConfig.getCorsAllowedHeaders().isEmpty());
    ArchaiusUtils.setProperty(configKey, "abc");
    Assert.assertThat(TransportConfig.getCorsAllowedHeaders(), Matchers.containsInAnyOrder("abc"));
    ArchaiusUtils.setProperty(configKey, "abc, def");
    Assert.assertThat(TransportConfig.getCorsAllowedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
    ArchaiusUtils.setProperty(configKey, "abc ,, def");
    Assert.assertThat(TransportConfig.getCorsAllowedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
    ArchaiusUtils.setProperty(configKey, "");
    Assert.assertTrue(TransportConfig.getCorsAllowedHeaders().isEmpty());
  }

  @Test
  public void testGetCorsAllowedMethods() {
    String configKey = "servicecomb.cors.allowedMethod";
    Assert.assertTrue(TransportConfig.getCorsAllowedMethods().isEmpty());
    ArchaiusUtils.setProperty(configKey, "GET");
    Assert.assertThat(TransportConfig.getCorsAllowedMethods(), Matchers.containsInAnyOrder("GET"));
    ArchaiusUtils.setProperty(configKey, "GET, POST");
    Assert.assertThat(TransportConfig.getCorsAllowedMethods(), Matchers.containsInAnyOrder("GET", "POST"));
    ArchaiusUtils.setProperty(configKey, "GET,,POST");
    Assert.assertThat(TransportConfig.getCorsAllowedMethods(), Matchers.containsInAnyOrder("GET", "POST"));
    ArchaiusUtils.setProperty(configKey, "");
    Assert.assertTrue(TransportConfig.getCorsAllowedMethods().isEmpty());
  }

  @Test
  public void testGetCorsExposedHeaders() {
    String configKey = "servicecomb.cors.exposedHeader";
    Assert.assertTrue(TransportConfig.getCorsExposedHeaders().isEmpty());
    ArchaiusUtils.setProperty(configKey, "abc");
    Assert.assertThat(TransportConfig.getCorsExposedHeaders(), Matchers.containsInAnyOrder("abc"));
    ArchaiusUtils.setProperty(configKey, "abc, def");
    Assert.assertThat(TransportConfig.getCorsExposedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
    ArchaiusUtils.setProperty(configKey, "abc ,, def");
    Assert.assertThat(TransportConfig.getCorsExposedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
    ArchaiusUtils.setProperty(configKey, "");
    Assert.assertTrue(TransportConfig.getCorsExposedHeaders().isEmpty());
  }

  @Test
  public void testGetCorsMaxAge() {
    Assert.assertEquals(-1, TransportConfig.getCorsMaxAge());
    ArchaiusUtils.setProperty("servicecomb.cors.maxAge", 3600);
    Assert.assertEquals(3600, TransportConfig.getCorsMaxAge());
  }

  @Test
  public void testMaxConcurrentStreams() {
    Assert.assertEquals(100L, TransportConfig.getMaxConcurrentStreams());
    ArchaiusUtils.setProperty("servicecomb.rest.server.http2.concurrentStreams", 200L);
    Assert.assertEquals(200L, TransportConfig.getMaxConcurrentStreams());
  }

  @Test
  public void testUseAlpn() {
    Assert.assertTrue(TransportConfig.getUseAlpn());
    ArchaiusUtils.setProperty("servicecomb.rest.server.http2.useAlpnEnabled", false);
    Assert.assertFalse(TransportConfig.getUseAlpn());
  }


  @Test
  public void testGetMaxInitialLineLength() {
    Assert.assertEquals(4096, TransportConfig.getMaxInitialLineLength());
    ArchaiusUtils.setProperty("servicecomb.rest.server.maxInitialLineLength", 8000);
    Assert.assertEquals(8000, TransportConfig.getMaxInitialLineLength());
  }
}
