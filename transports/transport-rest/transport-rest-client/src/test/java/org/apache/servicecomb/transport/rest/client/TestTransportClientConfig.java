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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestTransportClientConfig {

  @BeforeEach
  public void before() {
    ArchaiusUtils.resetConfig();
  }

  @AfterEach
  public void after() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void getThreadCount() {
    ArchaiusUtils.setProperty("servicecomb.rest.client.verticle-count", 1);
    Assertions.assertEquals(1, TransportClientConfig.getThreadCount());
  }

  @Test
  public void getConnectionMaxPoolSize() {
    Assertions.assertEquals(5, TransportClientConfig.getConnectionMaxPoolSize());
  }

  @Test
  public void getConnectionIdleTimeoutInSeconds() {
    Assertions.assertEquals(30, TransportClientConfig.getConnectionIdleTimeoutInSeconds());
  }

  @Test
  public void getHttp2MultiplexingLimit() {
    Assertions.assertEquals(-1, TransportClientConfig.getHttp2MultiplexingLimit());
  }

  @Test
  public void getHttp2ConnectionMaxPoolSize() {
    Assertions.assertEquals(1, TransportClientConfig.getHttp2ConnectionMaxPoolSize());
  }

  @Test
  public void getHttp2ConnectionIdleTimeoutInSeconds() {
    Assertions.assertEquals(0, TransportClientConfig.getHttp2ConnectionIdleTimeoutInSeconds());
  }

  @Test
  public void getUseAlpnEnabled() {
    Assertions.assertTrue(TransportClientConfig.getUseAlpn());
  }

  @Test
  public void getConnectionKeepAlive() {
    Assertions.assertTrue(TransportClientConfig.getConnectionKeepAlive());
  }

  @Test
  public void getConnectionCompression() {
    Assertions.assertFalse(TransportClientConfig.getConnectionCompression());
  }

  @Test
  public void getMaxHeaderSize() {
    Assertions.assertEquals(8192, TransportClientConfig.getMaxHeaderSize());
    ArchaiusUtils.setProperty("servicecomb.rest.client.maxHeaderSize", 1024);
    Assertions.assertEquals(1024, TransportClientConfig.getMaxHeaderSize());
  }

  @Test
  public void getMaxWaitQueueSize() {
    Assertions.assertEquals(-1, TransportClientConfig.getMaxWaitQueueSize());
    ArchaiusUtils.setProperty("servicecomb.rest.client.maxWaitQueueSize", 1024);
    Assertions.assertEquals(1024, TransportClientConfig.getMaxWaitQueueSize());
  }
}
