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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestTransportConfig {

  @BeforeEach
  public void before() {
    ArchaiusUtils.resetConfig();
  }

  @AfterEach
  public void after() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testGetAddressNull() {
    Assertions.assertNull(TransportConfig.getAddress());
  }

  @Test
  public void testGetAddressNormal() {
    ArchaiusUtils.setProperty("servicecomb.rest.address", "1.1.1.1");
    Assertions.assertEquals("1.1.1.1", TransportConfig.getAddress());
  }

  @Test
  public void testGetThreadCountNull() {
    new MockUp<Runtime>() {
      @Mock
      int availableProcessors() {
        return 1;
      }
    };
    Assertions.assertEquals(1, TransportConfig.getThreadCount());
  }

  @Test
  public void testGetThreadCountNormal() {
    ArchaiusUtils.setProperty("servicecomb.rest.server.thread-count", 10);
    Assertions.assertEquals(10, TransportConfig.getThreadCount());
  }

  @Test
  public void testGetCompressedAndHeaderSize() {
    ArchaiusUtils.setProperty("servicecomb.rest.server.compression", true);
    Assertions.assertTrue(TransportConfig.getCompressed());
    ArchaiusUtils.setProperty("servicecomb.rest.server.maxHeaderSize", 2048);
    Assertions.assertEquals(2048, TransportConfig.getMaxHeaderSize());
  }

  @Test
  public void testGetDecompressed() {
    Assertions.assertFalse(TransportConfig.getDecompressionSupported());
    ArchaiusUtils.setProperty("servicecomb.rest.server.decompressionSupported", true);
    Assertions.assertTrue(TransportConfig.getDecompressionSupported());
  }

  @Test
  public void testGetDecoderInitialBufferSize() {
    Assertions.assertEquals(128, TransportConfig.getDecoderInitialBufferSize());
    ArchaiusUtils.setProperty("servicecomb.rest.server.decoderInitialBufferSize", 256);
    Assertions.assertEquals(256, TransportConfig.getDecoderInitialBufferSize());
  }

  @Test
  public void testGetHttp2ConnectionWindowSize() {
    Assertions.assertEquals(-1, TransportConfig.getHttp2ConnectionWindowSize());
    ArchaiusUtils.setProperty("servicecomb.rest.server.http2ConnectionWindowSize", 1);
    Assertions.assertEquals(1, TransportConfig.getHttp2ConnectionWindowSize());
  }

  @Test
  public void testGetMaxFormAttributeSize() {
    Assertions.assertEquals(8192, TransportConfig.getMaxFormAttributeSize());
    ArchaiusUtils.setProperty("servicecomb.rest.server.maxFormAttributeSize", 3072);
    Assertions.assertEquals(3072, TransportConfig.getMaxFormAttributeSize());
  }

  @Test
  public void testGeCompressionLevel() {
    Assertions.assertEquals(6, TransportConfig.getCompressionLevel());
    ArchaiusUtils.setProperty("servicecomb.rest.server.compressionLevel", 8);
    Assertions.assertEquals(8, TransportConfig.getCompressionLevel());
  }

  @Test
  public void testGetMaxChunkSize() {
    Assertions.assertEquals(8192, TransportConfig.getMaxChunkSize());
    ArchaiusUtils.setProperty("servicecomb.rest.server.maxChunkSize", 65536);
    Assertions.assertEquals(65536, TransportConfig.getMaxChunkSize());
  }

  @Test
  public void testIsCorsEnabled() {
    Assertions.assertFalse(TransportConfig.isCorsEnabled());
    ArchaiusUtils.setProperty("servicecomb.cors.enabled", true);
    Assertions.assertTrue(TransportConfig.isCorsEnabled());
    ArchaiusUtils.setProperty("servicecomb.cors.enabled", false);
    Assertions.assertFalse(TransportConfig.isCorsEnabled());
  }

  @Test
  public void testGetCorsAllowedOrigin() {
    Assertions.assertEquals("*", TransportConfig.getCorsAllowedOrigin());
    String origin = "http://localhost:8080";
    ArchaiusUtils.setProperty("servicecomb.cors.origin", origin);
    Assertions.assertEquals(origin, TransportConfig.getCorsAllowedOrigin());
  }

  @Test
  public void testIsCorsAllowCredentials() {
    Assertions.assertFalse(TransportConfig.isCorsAllowCredentials());
    ArchaiusUtils.setProperty("servicecomb.cors.allowCredentials", true);
    Assertions.assertTrue(TransportConfig.isCorsAllowCredentials());
    ArchaiusUtils.setProperty("servicecomb.cors.allowCredentials", false);
    Assertions.assertFalse(TransportConfig.isCorsAllowCredentials());
  }

  @Test
  public void testGetCorsAllowedHeaders() {
    String configKey = "servicecomb.cors.allowedHeader";
    Assertions.assertTrue(TransportConfig.getCorsAllowedHeaders().isEmpty());
    ArchaiusUtils.setProperty(configKey, "abc");
    MatcherAssert.assertThat(TransportConfig.getCorsAllowedHeaders(), Matchers.containsInAnyOrder("abc"));
    ArchaiusUtils.setProperty(configKey, "abc, def");
    MatcherAssert.assertThat(TransportConfig.getCorsAllowedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
    ArchaiusUtils.setProperty(configKey, "abc ,, def");
    MatcherAssert.assertThat(TransportConfig.getCorsAllowedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
    ArchaiusUtils.setProperty(configKey, "");
    Assertions.assertTrue(TransportConfig.getCorsAllowedHeaders().isEmpty());
  }

  @Test
  public void testGetCorsAllowedMethods() {
    String configKey = "servicecomb.cors.allowedMethod";
    Assertions.assertTrue(TransportConfig.getCorsAllowedMethods().isEmpty());
    ArchaiusUtils.setProperty(configKey, "GET");
    MatcherAssert.assertThat(TransportConfig.getCorsAllowedMethods(), Matchers.containsInAnyOrder("GET"));
    ArchaiusUtils.setProperty(configKey, "GET, POST");
    MatcherAssert.assertThat(TransportConfig.getCorsAllowedMethods(), Matchers.containsInAnyOrder("GET", "POST"));
    ArchaiusUtils.setProperty(configKey, "GET,,POST");
    MatcherAssert.assertThat(TransportConfig.getCorsAllowedMethods(), Matchers.containsInAnyOrder("GET", "POST"));
    ArchaiusUtils.setProperty(configKey, "");
    Assertions.assertTrue(TransportConfig.getCorsAllowedMethods().isEmpty());
  }

  @Test
  public void testGetCorsExposedHeaders() {
    String configKey = "servicecomb.cors.exposedHeader";
    Assertions.assertTrue(TransportConfig.getCorsExposedHeaders().isEmpty());
    ArchaiusUtils.setProperty(configKey, "abc");
    MatcherAssert.assertThat(TransportConfig.getCorsExposedHeaders(), Matchers.containsInAnyOrder("abc"));
    ArchaiusUtils.setProperty(configKey, "abc, def");
    MatcherAssert.assertThat(TransportConfig.getCorsExposedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
    ArchaiusUtils.setProperty(configKey, "abc ,, def");
    MatcherAssert.assertThat(TransportConfig.getCorsExposedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
    ArchaiusUtils.setProperty(configKey, "");
    Assertions.assertTrue(TransportConfig.getCorsExposedHeaders().isEmpty());
  }

  @Test
  public void testGetCorsMaxAge() {
    Assertions.assertEquals(-1, TransportConfig.getCorsMaxAge());
    ArchaiusUtils.setProperty("servicecomb.cors.maxAge", 3600);
    Assertions.assertEquals(3600, TransportConfig.getCorsMaxAge());
  }

  @Test
  public void testHttp2Setting() {
    Assertions.assertEquals(100L, TransportConfig.getMaxConcurrentStreams());
    ArchaiusUtils.setProperty("servicecomb.rest.server.http2.concurrentStreams", 200L);
    Assertions.assertEquals(200L, TransportConfig.getMaxConcurrentStreams());

    Assertions.assertEquals(4096L, TransportConfig.getHttp2HeaderTableSize());
    ArchaiusUtils.setProperty("servicecomb.rest.server.http2.HeaderTableSize", 8192L);
    Assertions.assertEquals(8192L, TransportConfig.getHttp2HeaderTableSize());

    Assertions.assertTrue(TransportConfig.getPushEnabled());
    ArchaiusUtils.setProperty("servicecomb.rest.server.http2.pushEnabled", false);
    Assertions.assertFalse(TransportConfig.getPushEnabled());

    Assertions.assertEquals(65535, TransportConfig.getInitialWindowSize());
    ArchaiusUtils.setProperty("servicecomb.rest.server.http2.initialWindowSize", 2 * 65535);
    Assertions.assertEquals(2 * 65535, TransportConfig.getInitialWindowSize());

    Assertions.assertEquals(16384, TransportConfig.getMaxFrameSize());
    ArchaiusUtils.setProperty("servicecomb.rest.server.http2.maxFrameSize", 65535);
    Assertions.assertEquals(65535, TransportConfig.getMaxFrameSize());

    Assertions.assertEquals(8192, TransportConfig.getMaxHeaderListSize());
    ArchaiusUtils.setProperty("servicecomb.rest.server.http2.maxHeaderListSize", 65535);
    Assertions.assertEquals(65535, TransportConfig.getMaxHeaderListSize());
  }


  @Test
  public void testUseAlpn() {
    Assertions.assertTrue(TransportConfig.getUseAlpn());
    ArchaiusUtils.setProperty("servicecomb.rest.server.http2.useAlpnEnabled", false);
    Assertions.assertFalse(TransportConfig.getUseAlpn());
  }

  @Test
  public void testGetMaxInitialLineLength() {
    Assertions.assertEquals(4096, TransportConfig.getMaxInitialLineLength());
    ArchaiusUtils.setProperty("servicecomb.rest.server.maxInitialLineLength", 8000);
    Assertions.assertEquals(8000, TransportConfig.getMaxInitialLineLength());
  }
}
