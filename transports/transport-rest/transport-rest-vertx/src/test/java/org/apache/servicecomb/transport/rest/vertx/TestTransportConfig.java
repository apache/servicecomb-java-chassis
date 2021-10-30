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

import mockit.Mock;
import mockit.MockUp;

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
    try {
      ArchaiusUtils.setProperty("servicecomb.rest.address", "1.1.1.1");
      Assert.assertEquals("1.1.1.1", TransportConfig.getAddress());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.address", null);
    }
  }

  @Test
  public void testGetThreadCountNull() {
    new MockUp<Runtime>() {
      @Mock
      int availableProcessors() {
        return 1;
      }
    };
    Assert.assertEquals(1, TransportConfig.getThreadCount());
  }

  @Test
  public void testGetThreadCountNormal() {
    try {
      ArchaiusUtils.setProperty("servicecomb.rest.server.thread-count", 10);
      Assert.assertEquals(10, TransportConfig.getThreadCount());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.server.thread-count", null);
    }
  }

  @Test
  public void testGetCompressedAndHeaderSize() {
    try {
      ArchaiusUtils.setProperty("servicecomb.rest.server.compression", true);
      Assert.assertTrue(TransportConfig.getCompressed());
      ArchaiusUtils.setProperty("servicecomb.rest.server.maxHeaderSize", 2048);
      Assert.assertEquals(2048, TransportConfig.getMaxHeaderSize());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.server.compression", null);
      ArchaiusUtils.setProperty("servicecomb.rest.server.maxHeaderSize", null);
    }
  }

  @Test
  public void testGetDecompressed() {
    try {
      Assert.assertFalse(TransportConfig.getDecompressionSupported());
      ArchaiusUtils.setProperty("servicecomb.rest.server.decompressionSupported", true);
      Assert.assertTrue(TransportConfig.getDecompressionSupported());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.server.decompressionSupported", null);
    }
  }

  @Test
  public void testGetDecoderInitialBufferSize() {
    try {
      Assert.assertEquals(128, TransportConfig.getDecoderInitialBufferSize());
      ArchaiusUtils.setProperty("servicecomb.rest.server.decoderInitialBufferSize", 256);
      Assert.assertEquals(256, TransportConfig.getDecoderInitialBufferSize());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.server.decoderInitialBufferSize", null);
    }
  }

  @Test
  public void testGetHttp2ConnectionWindowSize() {
    try {
      Assert.assertEquals(-1, TransportConfig.getHttp2ConnectionWindowSize());
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2ConnectionWindowSize", 1);
      Assert.assertEquals(1, TransportConfig.getHttp2ConnectionWindowSize());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2ConnectionWindowSize", null);
    }
  }

  @Test
  public void testGetMaxFormAttributeSize() {
    try {
      Assert.assertEquals(8192, TransportConfig.getMaxFormAttributeSize());
      ArchaiusUtils.setProperty("servicecomb.rest.server.maxFormAttributeSize", 3072);
      Assert.assertEquals(3072, TransportConfig.getMaxFormAttributeSize());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.server.maxFormAttributeSize", null);
    }
  }

  @Test
  public void testGeCompressionLevel() {
    try {
      Assert.assertEquals(6, TransportConfig.getCompressionLevel());
      ArchaiusUtils.setProperty("servicecomb.rest.server.compressionLevel", 8);
      Assert.assertEquals(8, TransportConfig.getCompressionLevel());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.server.compressionLevel", null);
    }
  }

  @Test
  public void testGetMaxChunkSize() {
    try {
      Assert.assertEquals(8192, TransportConfig.getMaxChunkSize());
      ArchaiusUtils.setProperty("servicecomb.rest.server.maxChunkSize", 65536);
      Assert.assertEquals(65536, TransportConfig.getMaxChunkSize());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.server.maxChunkSize", null);
    }
  }

  @Test
  public void testIsCorsEnabled() {
    try {
      Assert.assertFalse(TransportConfig.isCorsEnabled());
      ArchaiusUtils.setProperty("servicecomb.cors.enabled", true);
      Assert.assertTrue(TransportConfig.isCorsEnabled());
      ArchaiusUtils.setProperty("servicecomb.cors.enabled", false);
      Assert.assertFalse(TransportConfig.isCorsEnabled());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.cors.enabled", null);
    }
  }

  @Test
  public void testGetCorsAllowedOrigin() {
    try {
      Assert.assertEquals("*", TransportConfig.getCorsAllowedOrigin());
      String origin = "http://localhost:8080";
      ArchaiusUtils.setProperty("servicecomb.cors.origin", origin);
      Assert.assertEquals(origin, TransportConfig.getCorsAllowedOrigin());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.cors.origin", null);
    }
  }

  @Test
  public void testIsCorsAllowCredentials() {
    try {
      Assert.assertFalse(TransportConfig.isCorsAllowCredentials());
      ArchaiusUtils.setProperty("servicecomb.cors.allowCredentials", true);
      Assert.assertTrue(TransportConfig.isCorsAllowCredentials());
      ArchaiusUtils.setProperty("servicecomb.cors.allowCredentials", false);
      Assert.assertFalse(TransportConfig.isCorsAllowCredentials());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.cors.allowCredentials", null);
    }
  }

  @Test
  public void testGetCorsAllowedHeaders() {
    String configKey = "servicecomb.cors.allowedHeader";
    try {
      Assert.assertTrue(TransportConfig.getCorsAllowedHeaders().isEmpty());
      ArchaiusUtils.setProperty(configKey, "abc");
      Assert.assertThat(TransportConfig.getCorsAllowedHeaders(), Matchers.containsInAnyOrder("abc"));
      ArchaiusUtils.setProperty(configKey, "abc, def");
      Assert.assertThat(TransportConfig.getCorsAllowedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
      ArchaiusUtils.setProperty(configKey, "abc ,, def");
      Assert.assertThat(TransportConfig.getCorsAllowedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
      ArchaiusUtils.setProperty(configKey, "");
      Assert.assertTrue(TransportConfig.getCorsAllowedHeaders().isEmpty());
    } finally {
      ArchaiusUtils.setProperty(configKey, null);
    }
  }

  @Test
  public void testGetCorsAllowedMethods() {
    String configKey = "servicecomb.cors.allowedMethod";
    try {
      Assert.assertTrue(TransportConfig.getCorsAllowedMethods().isEmpty());
      ArchaiusUtils.setProperty(configKey, "GET");
      Assert.assertThat(TransportConfig.getCorsAllowedMethods(), Matchers.containsInAnyOrder("GET"));
      ArchaiusUtils.setProperty(configKey, "GET, POST");
      Assert.assertThat(TransportConfig.getCorsAllowedMethods(), Matchers.containsInAnyOrder("GET", "POST"));
      ArchaiusUtils.setProperty(configKey, "GET,,POST");
      Assert.assertThat(TransportConfig.getCorsAllowedMethods(), Matchers.containsInAnyOrder("GET", "POST"));
      ArchaiusUtils.setProperty(configKey, "");
      Assert.assertTrue(TransportConfig.getCorsAllowedMethods().isEmpty());
    } finally {
      ArchaiusUtils.setProperty(configKey, null);
    }
  }

  @Test
  public void testGetCorsExposedHeaders() {
    String configKey = "servicecomb.cors.exposedHeader";
    try {
      Assert.assertTrue(TransportConfig.getCorsExposedHeaders().isEmpty());
      ArchaiusUtils.setProperty(configKey, "abc");
      Assert.assertThat(TransportConfig.getCorsExposedHeaders(), Matchers.containsInAnyOrder("abc"));
      ArchaiusUtils.setProperty(configKey, "abc, def");
      Assert.assertThat(TransportConfig.getCorsExposedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
      ArchaiusUtils.setProperty(configKey, "abc ,, def");
      Assert.assertThat(TransportConfig.getCorsExposedHeaders(), Matchers.containsInAnyOrder("abc", "def"));
      ArchaiusUtils.setProperty(configKey, "");
      Assert.assertTrue(TransportConfig.getCorsExposedHeaders().isEmpty());
    } finally {
      ArchaiusUtils.setProperty(configKey, null);
    }
  }

  @Test
  public void testGetCorsMaxAge() {
    try {
      Assert.assertEquals(-1, TransportConfig.getCorsMaxAge());
      ArchaiusUtils.setProperty("servicecomb.cors.maxAge", 3600);
      Assert.assertEquals(3600, TransportConfig.getCorsMaxAge());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.cors.maxAge", null);
    }
  }

  @Test
  public void testHttp2Setting() {
    try {
      Assert.assertEquals(100L, TransportConfig.getMaxConcurrentStreams());
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.concurrentStreams", 200L);
      Assert.assertEquals(200L, TransportConfig.getMaxConcurrentStreams());

      Assert.assertEquals(4096L, TransportConfig.getHttp2HeaderTableSize());
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.HeaderTableSize", 8192L);
      Assert.assertEquals(8192L, TransportConfig.getHttp2HeaderTableSize());

      Assert.assertTrue(TransportConfig.getPushEnabled());
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.pushEnabled", false);
      Assert.assertFalse(TransportConfig.getPushEnabled());

      Assert.assertEquals(65535, TransportConfig.getInitialWindowSize());
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.initialWindowSize", 2 * 65535);
      Assert.assertEquals(2 * 65535, TransportConfig.getInitialWindowSize());

      Assert.assertEquals(16384, TransportConfig.getMaxFrameSize());
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.maxFrameSize", 65535);
      Assert.assertEquals(65535, TransportConfig.getMaxFrameSize());

      Assert.assertEquals(Integer.MAX_VALUE, TransportConfig.getMaxHeaderListSize());
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.maxHeaderListSize", 65535);
      Assert.assertEquals(65535, TransportConfig.getMaxHeaderListSize());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.concurrentStreams", null);
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.HeaderTableSize", null);
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.pushEnabled", null);
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.initialWindowSize", null);
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.maxFrameSize", null);
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.maxHeaderListSize", null);
    }
  }


  @Test
  public void testUseAlpn() {
    try {
      Assert.assertTrue(TransportConfig.getUseAlpn());
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.useAlpnEnabled", false);
      Assert.assertFalse(TransportConfig.getUseAlpn());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.server.http2.useAlpnEnabled", null);
    }
  }

  @Test
  public void testGetMaxInitialLineLength() {
    try {
      Assert.assertEquals(4096, TransportConfig.getMaxInitialLineLength());
      ArchaiusUtils.setProperty("servicecomb.rest.server.maxInitialLineLength", 8000);
      Assert.assertEquals(8000, TransportConfig.getMaxInitialLineLength());
    } finally {
      ArchaiusUtils.setProperty("servicecomb.rest.server.maxInitialLineLength", null);
    }
  }
}
