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

import org.junit.Assert;
import org.junit.Test;

public class TestServiceCenterConfig {
  ServiceCenterConfig config = new ServiceCenterConfig();

  @Test
  public void testDefaultValues() {
    Assert.assertEquals(0, config.getMaxHeaderBytes());
    Assert.assertEquals(0, config.getMaxBodyBytes());
    Assert.assertNull(config.getReadHeaderTimeout());
    Assert.assertNull(config.getReadTimeout());
    Assert.assertNull(config.getIdleTimeout());
    Assert.assertNull(config.getWriteTimeout());
    Assert.assertNull(config.getLimitTTLUnit());
    Assert.assertEquals(0, config.getLimitConnections());
    Assert.assertNull(config.getLimitIPLookup());
    Assert.assertNull(config.getSslEnabled());
    Assert.assertNull(config.getSslMinVersion());
    Assert.assertNull(config.getSslVerifyPeer());
    Assert.assertNull(config.getSslCiphers());
    Assert.assertNull(config.getAutoSyncInterval());
    Assert.assertEquals(0, config.getCompactIndexDelta());
    Assert.assertNull(config.getCompactInterval());
    Assert.assertEquals(0, config.getLogRotateSize());
    Assert.assertEquals(0, config.getLogBackupCount());
  }

  @Test
  public void testInitializedValues() {
    initMicroservice(); //Initialize the Object
    Assert.assertEquals(10, config.getMaxHeaderBytes());
    Assert.assertEquals(10, config.getMaxBodyBytes());
    Assert.assertEquals("60s", config.getReadHeaderTimeout());
    Assert.assertEquals("60s", config.getReadTimeout());
    Assert.assertEquals("60s", config.getIdleTimeout());
    Assert.assertEquals("60s", config.getWriteTimeout());
    Assert.assertEquals("s", config.getLimitTTLUnit());
    Assert.assertEquals(0, config.getLimitConnections());
    Assert.assertEquals("xxx", config.getLimitIPLookup());
    Assert.assertEquals("false", config.getSslEnabled());
    Assert.assertEquals("xxx", config.getSslMinVersion());
    Assert.assertEquals("true", config.getSslVerifyPeer());
    Assert.assertEquals("xxx", config.getSslCiphers());
    Assert.assertEquals("30s", config.getAutoSyncInterval());
    Assert.assertEquals(100, config.getCompactIndexDelta());
    Assert.assertEquals("100", config.getCompactInterval());
    Assert.assertEquals(20, config.getLogRotateSize());
    Assert.assertEquals(50, config.getLogBackupCount());
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
