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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.SpectatorUtils;
import com.sun.net.httpserver.HttpServer;

import io.prometheus.client.exporter.HTTPServer;

@SuppressWarnings("restriction")
public class TestPrometheusPublisher {
  CompositeRegistry globalRegistry = SpectatorUtils.createCompositeRegistry(new ManualClock());

  PrometheusPublisher publisher = new PrometheusPublisher();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @AfterClass
  public static void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testBadPublishAddress() {
    thrown.expect(ServiceCombException.class);

    ArchaiusUtils.setProperty(PrometheusPublisher.METRICS_PROMETHEUS_ADDRESS, "a:b:c");
    publisher.init(globalRegistry, null, null);
  }

  @Test
  public void testBadPublishAddress_BadPort() {
    thrown.expect(ServiceCombException.class);

    ArchaiusUtils.setProperty(PrometheusPublisher.METRICS_PROMETHEUS_ADDRESS, "localhost:xxxx");
    publisher.init(globalRegistry, null, null);
  }

  @Test
  public void testBadPublishAddress_TooLargePort() {
    thrown.expect(ServiceCombException.class);

    ArchaiusUtils.setProperty(PrometheusPublisher.METRICS_PROMETHEUS_ADDRESS, "localhost:9999999");
    publisher.init(globalRegistry, null, null);
  }

  @Test
  public void collect() throws IllegalAccessException, IOException {
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
      Assert.assertEquals("# HELP ServiceComb Metrics ServiceComb Metrics\n" +
          "# TYPE ServiceComb Metrics untyped\n" +
          "count_name{tag1=\"tag1v\",tag2=\"tag2v\",} 1.0\n", IOUtils.toString(is));
    }

    publisher.uninit();
  }
}
