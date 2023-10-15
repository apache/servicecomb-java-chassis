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

import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.DEFAULT_METRICS_WINDOW_TIME;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.METRICS_WINDOW_TIME;
import static org.apache.servicecomb.metrics.prometheus.PrometheusPublisher.METRICS_PROMETHEUS_ADDRESS;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;
import com.sun.net.httpserver.HttpServer;

import io.prometheus.client.exporter.HTTPServer;

public class TestPrometheusPublisher {
  GlobalRegistry globalRegistry = new GlobalRegistry(new ManualClock());

  PrometheusPublisher publisher = new PrometheusPublisher();

  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() {
    publisher.setEnvironment(environment);
    Mockito.when(environment.getProperty(METRICS_WINDOW_TIME, int.class, DEFAULT_METRICS_WINDOW_TIME))
        .thenReturn(DEFAULT_METRICS_WINDOW_TIME);
    Mockito.when(environment.getProperty(
            CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN, int.class, 7))
        .thenReturn(7);
  }

  @AfterAll
  public static void teardown() {
  }

  @Test
  public void testBadPublishAddress() {
    Mockito.when(environment.getProperty(METRICS_PROMETHEUS_ADDRESS, String.class, "0.0.0.0:9696"))
        .thenReturn("a:b:c");
    Assertions.assertThrows(ServiceCombException.class, () -> {
      publisher.init(globalRegistry, null, null);
    });
  }

  @Test
  public void testBadPublishAddress_BadPort() {
    Mockito.when(environment.getProperty(METRICS_PROMETHEUS_ADDRESS, String.class, "0.0.0.0:9696"))
        .thenReturn("localhost:xxxx");
    Assertions.assertThrows(ServiceCombException.class, () -> {
      publisher.init(globalRegistry, null, null);
    });
  }

  @Test
  public void testBadPublishAddress_TooLargePort() {
    Mockito.when(environment.getProperty(METRICS_PROMETHEUS_ADDRESS, String.class, "0.0.0.0:9696"))
        .thenReturn("localhost:9999999");
    Assertions.assertThrows(ServiceCombException.class, () -> {
      publisher.init(globalRegistry, null, null);
    });
  }

  @Test
  public void collect() throws IllegalAccessException, IOException {
    MicroserviceProperties microserviceProperties = Mockito.mock(MicroserviceProperties.class);
    Mockito.when(microserviceProperties.getApplication()).thenReturn("testAppId");
    Mockito.when(environment.getProperty(METRICS_PROMETHEUS_ADDRESS, String.class, "0.0.0.0:9696"))
        .thenReturn("localhost:0");
    publisher.setMicroserviceProperties(microserviceProperties);
    publisher.init(globalRegistry, null, new MetricsBootstrapConfig(environment));

    Registry registry = new DefaultRegistry(new ManualClock());
    globalRegistry.add(registry);

    Counter counter = registry.counter("count.name", "tag1", "tag1v", "tag2", "tag2v");
    counter.increment();

    HTTPServer httpServer = (HTTPServer) FieldUtils.readField(publisher, "httpServer", true);
    com.sun.net.httpserver.HttpServer server = (HttpServer) FieldUtils.readField(httpServer, "server", true);

    URL url = new URL("http://localhost:" + server.getAddress().getPort() + "/metrics");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try (InputStream is = conn.getInputStream()) {
      Assertions.assertEquals("""
              # HELP ServiceComb_Metrics ServiceComb Metrics
              # TYPE ServiceComb_Metrics untyped
              count_name{appId="testAppId",tag1="tag1v",tag2="tag2v",} 1.0
              """,
          IOUtils.toString(is, StandardCharsets.UTF_8));
    }

    publisher.destroy();
  }
}
