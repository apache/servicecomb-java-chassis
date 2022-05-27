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

package org.apache.servicecomb.serviceregistry.api.registry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestServiceCenterConfig {
  ServiceCenterConfig config = new ServiceCenterConfig();

  @Test
  public void testDefaultValues() {
    Assertions.assertEquals(0, config.getMaxHeaderBytes());
    Assertions.assertEquals(0, config.getMaxBodyBytes());
    Assertions.assertNull(config.getReadHeaderTimeout());
    Assertions.assertNull(config.getReadTimeout());
    Assertions.assertNull(config.getIdleTimeout());
    Assertions.assertNull(config.getWriteTimeout());
    Assertions.assertNull(config.getLimitTTLUnit());
    Assertions.assertEquals(0, config.getLimitConnections());
    Assertions.assertNull(config.getLimitIPLookup());
    Assertions.assertNull(config.getSslEnabled());
    Assertions.assertNull(config.getSslMinVersion());
    Assertions.assertNull(config.getSslVerifyPeer());
    Assertions.assertNull(config.getSslCiphers());
    Assertions.assertNull(config.getAutoSyncInterval());
    Assertions.assertEquals(0, config.getCompactIndexDelta());
    Assertions.assertNull(config.getCompactInterval());
    Assertions.assertEquals(0, config.getLogRotateSize());
    Assertions.assertEquals(0, config.getLogBackupCount());
  }

  @Test
  public void testInitializedValues() {
    initMicroservice(); //Initialize the Object
    Assertions.assertEquals(10, config.getMaxHeaderBytes());
    Assertions.assertEquals(10, config.getMaxBodyBytes());
    Assertions.assertEquals("60s", config.getReadHeaderTimeout());
    Assertions.assertEquals("60s", config.getReadTimeout());
    Assertions.assertEquals("60s", config.getIdleTimeout());
    Assertions.assertEquals("60s", config.getWriteTimeout());
    Assertions.assertEquals("s", config.getLimitTTLUnit());
    Assertions.assertEquals(0, config.getLimitConnections());
    Assertions.assertEquals("xxx", config.getLimitIPLookup());
    Assertions.assertEquals("false", config.getSslEnabled());
    Assertions.assertEquals("xxx", config.getSslMinVersion());
    Assertions.assertEquals("true", config.getSslVerifyPeer());
    Assertions.assertEquals("xxx", config.getSslCiphers());
    Assertions.assertEquals("30s", config.getAutoSyncInterval());
    Assertions.assertEquals(100, config.getCompactIndexDelta());
    Assertions.assertEquals("100", config.getCompactInterval());
    Assertions.assertEquals(20, config.getLogRotateSize());
    Assertions.assertEquals(50, config.getLogBackupCount());
  }

  private void initMicroservice() {
    config.setMaxHeaderBytes(10);
    config.setMaxBodyBytes(10);
    config.setReadHeaderTimeout("60s");
    config.setReadTimeout("60s");
    config.setIdleTimeout("60s");
    config.setWriteTimeout("60s");
    config.setLimitTTLUnit("s");
    config.setLimitConnections(0);
    config.setLimitIPLookup("xxx");
    config.setSslEnabled("false");
    config.setSslMinVersion("xxx");
    config.setSslVerifyPeer("true");
    config.setSslCiphers("xxx");
    config.setAutoSyncInterval("30s");
    config.setCompactIndexDelta(100);
    config.setCompactInterval("100");
    config.setLogRotateSize(20);
    config.setLogBackupCount(50);
  }
}
