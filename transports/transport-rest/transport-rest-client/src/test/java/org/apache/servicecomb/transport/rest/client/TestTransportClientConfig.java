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

package org.apache.servicecomb.transport.rest.client;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTransportClientConfig {

  @Before
  public void before() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void after() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void getThreadCount() {
    ArchaiusUtils.setProperty("servicecomb.rest.client.verticle-count", 1);
    Assert.assertEquals(1, TransportClientConfig.getThreadCount());
  }

  @Test
  public void getConnectionMaxPoolSize() {
    Assert.assertEquals(5, TransportClientConfig.getConnectionMaxPoolSize());
  }

  @Test
  public void getConnectionIdleTimeoutInSeconds() {
    Assert.assertEquals(30, TransportClientConfig.getConnectionIdleTimeoutInSeconds());
  }

  @Test
  public void getHttp2MultiplexingLimit() {
    Assert.assertEquals(-1, TransportClientConfig.getHttp2MultiplexingLimit());
  }

  @Test
  public void getHttp2ConnectionMaxPoolSize() {
    Assert.assertEquals(1, TransportClientConfig.getHttp2ConnectionMaxPoolSize());
  }

  @Test
  public void getHttp2ConnectionIdleTimeoutInSeconds() {
    Assert.assertEquals(0, TransportClientConfig.getHttp2ConnectionIdleTimeoutInSeconds());
  }

  @Test
  public void getUseAlpnEnabled() {
    Assert.assertTrue(TransportClientConfig.getUseAlpn());
  }

  @Test
  public void getConnectionKeepAlive() {
    Assert.assertTrue(TransportClientConfig.getConnectionKeepAlive());
  }

  @Test
  public void getConnectionCompression() {
    Assert.assertFalse(TransportClientConfig.getConnectionCompression());
  }

  @Test
  public void getMaxHeaderSize() {
    Assert.assertEquals(8192, TransportClientConfig.getMaxHeaderSize());
    ArchaiusUtils.setProperty("servicecomb.rest.client.maxHeaderSize", 1024);
    Assert.assertEquals(1024, TransportClientConfig.getMaxHeaderSize());
  }
}
