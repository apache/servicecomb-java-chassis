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

package org.apache.servicecomb.metrics.prometheus;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.RegistrationManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;

import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;
import com.sun.net.httpserver.HttpServer;

import io.prometheus.client.exporter.HTTPServer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@SuppressWarnings("restriction")
public class TestPrometheusPublisher {
  GlobalRegistry globalRegistry = new GlobalRegistry(new ManualClock());

  PrometheusPublisher publisher = new PrometheusPublisher();

  @AfterAll
  public static void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testBadPublishAddress() {
    Assertions.assertThrows(ServiceCombException.class, () -> {
      ArchaiusUtils.setProperty(PrometheusPublisher.METRICS_PROMETHEUS_ADDRESS, "a:b:c");
      publisher.init(globalRegistry, null, null);
    });
  }

  @Test
  public void testBadPublishAddress_BadPort() {
    Assertions.assertThrows(ServiceCombException.class, () -> {
      ArchaiusUtils.setProperty(PrometheusPublisher.METRICS_PROMETHEUS_ADDRESS, "localhost:xxxx");
      publisher.init(globalRegistry, null, null);
    });
  }

  @Test
  public void testBadPublishAddress_TooLargePort() {
    Assertions.assertThrows(ServiceCombException.class, () -> {
      ArchaiusUtils.setProperty(PrometheusPublisher.METRICS_PROMETHEUS_ADDRESS, "localhost:9999999");
      publisher.init(globalRegistry, null, null);
    });
  }

  @Test
  public void collect() throws IllegalAccessException, IOException {
    RegistrationManager.INSTANCE = Mockito.spy(RegistrationManager.INSTANCE);
    Mockito.doReturn("testAppId").when(RegistrationManager.INSTANCE).getAppId();
    ArchaiusUtils.setProperty(PrometheusPublisher.METRICS_PROMETHEUS_ADDRESS, "localhost:0");
    publisher.init(globalRegistry, null, null);

    Registry registry = new DefaultRegistry(new ManualClock());
    globalRegistry.add(registry);

    Counter counter = registry.counter("count.name", "tag1", "tag1v", "tag2", "tag2v");
    counter.increment();

    HTTPServer httpServer = (HTTPServer) FieldUtils.readField(publisher, "httpServer", true);
    com.sun.net.httpserver.HttpServer server = (HttpServer) FieldUtils.readField(httpServer, "server", true);

    URL url = new URL("http://localhost:" + server.getAddress().getPort() + "/metrics");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try (InputStream is = conn.getInputStream()) {
      Assertions.assertEquals("# HELP ServiceComb_Metrics ServiceComb Metrics\n" +
              "# TYPE ServiceComb_Metrics untyped\n" +
              "count_name{appId=\"testAppId\",tag1=\"tag1v\",tag2=\"tag2v\",} 1.0\n",
          IOUtils.toString(is, StandardCharsets.UTF_8));
    }

    publisher.destroy();
  }
}
