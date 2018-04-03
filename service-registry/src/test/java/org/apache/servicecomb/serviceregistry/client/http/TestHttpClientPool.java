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

package org.apache.servicecomb.serviceregistry.client.http;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;

public class TestHttpClientPool {
  @Before
  public void setup() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void createHttpClientOptions_proxy() {
    ArchaiusUtils.setProperty(ServiceRegistryConfig.PROXY_ENABLE, "true");
    ArchaiusUtils.setProperty(ServiceRegistryConfig.PROXY_HOST, "host");
    ArchaiusUtils.setProperty(ServiceRegistryConfig.PROXY_PORT, "1234");
    ArchaiusUtils.setProperty(ServiceRegistryConfig.PROXY_USERNAME, "user");
    ArchaiusUtils.setProperty(ServiceRegistryConfig.PROXY_PASSWD, "pass");

    HttpClientOptions httpClientOptions = HttpClientPool.INSTANCE.createHttpClientOptions();

    Assert.assertEquals(
        "{"
            + "\"host\":\"host\","
            + "\"password\":\"pass\","
            + "\"port\":1234,"
            + "\"type\":\"HTTP\","
            + "\"username\":\"user\""
            + "}",
        httpClientOptions.getProxyOptions().toJson().toString());
  }

  @Test
  public void createHttpClientOptions_noProxy() {
    ArchaiusUtils.setProperty(ServiceRegistryConfig.PROXY_ENABLE, "false");

    HttpClientOptions httpClientOptions = HttpClientPool.INSTANCE.createHttpClientOptions();

    Assert.assertNull(httpClientOptions.getProxyOptions());
  }

  @Test
  public void createHttpClientOptions_http2() {
    ArchaiusUtils.setProperty("cse.service.registry.client.httpVersion", HttpVersion.HTTP_2.name());

    HttpClientOptions httpClientOptions = HttpClientPool.INSTANCE.createHttpClientOptions();

    Assert.assertEquals(HttpVersion.HTTP_2, httpClientOptions.getProtocolVersion());
    Assert.assertFalse(httpClientOptions.isHttp2ClearTextUpgrade());
  }

  @Test
  public void createHttpClientOptions_notHttp2() {
    HttpClientOptions httpClientOptions = HttpClientPool.INSTANCE.createHttpClientOptions();

    Assert.assertEquals(HttpVersion.HTTP_1_1, httpClientOptions.getProtocolVersion());
    Assert.assertTrue(httpClientOptions.isHttp2ClearTextUpgrade());
  }
}
