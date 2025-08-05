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
package org.apache.servicecomb.metrics.core;

import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.DEFAULT_METRICS_WINDOW_TIME;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.METRICS_WINDOW_TIME;
import static org.apache.servicecomb.metrics.core.publish.DefaultLogPublisher.ENDPOINTS_CLIENT_DETAIL_ENABLED;

import java.util.List;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.PolledEvent;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.foundation.vertx.SharedVertxFactory;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.apache.servicecomb.metrics.core.publish.DefaultLogPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.SysProps;
import io.vertx.ext.web.Router;

public class TestVertxMetersInitializer {
  MeterRegistry registry = new SimpleMeterRegistry();

  EventBus eventBus = new EventBus();

  VertxMetersInitializer vertxMetersInitializer = new VertxMetersInitializer();

  DefaultLogPublisher logPublisher = new DefaultLogPublisher();

  Environment environment = Mockito.mock(Environment.class);

  LogCollector logCollector = new LogCollector();

  static HttpClient client;

  static HttpServer server;

  static int port;

  static String body = "body";

  public static class TestServerVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
      Router mainRouter = Router.router(vertx);
      mainRouter.route("/").handler(context -> context.response().end(body));

      server = vertx.createHttpServer();
      server.requestHandler(mainRouter);
      Future<HttpServer> future = server.listen(0, "0.0.0.0");
      future.onComplete((s, f) -> {
        if (f == null) {
          port = s.actualPort();
          startPromise.complete();
          return;
        }

        startPromise.fail(f);
      });
    }
  }

  public static class TestClientVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
      client = vertx.createHttpClient();
      Future<HttpClientRequest> future = client.request(HttpMethod.GET, port, "127.0.0.1", "/");
      future.onComplete((s, f) -> {
        if (f == null) {
          Future<HttpClientResponse> responseFuture = s.send(body);
          responseFuture.onComplete((rs, rf) -> {
            if (rf == null) {
              rs.bodyHandler((buffer) -> startPromise.complete());
            } else {
              startPromise.fail(f);
            }
          });
        }
      });
    }
  }

  @BeforeEach
  public void setup() {
    Mockito.when(environment.getProperty("servicecomb.transport.eventloop.size", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty(SysProps.DISABLE_FILE_CP_RESOLVING.name, boolean.class, true))
        .thenReturn(true);
    LegacyPropertyFactory.setEnvironment(environment);
    HttpClients.load();
  }

  @AfterEach
  public void tearDown() {
    logCollector.clear();
    HttpClients.destroy();
    if (client != null) {
      client.shutdown();
    }
    if (server != null) {
      server.shutdown();
    }
  }

  @Test
  public void init() throws InterruptedException {
    Mockito.when(environment.getProperty(METRICS_WINDOW_TIME, int.class, DEFAULT_METRICS_WINDOW_TIME))
        .thenReturn(DEFAULT_METRICS_WINDOW_TIME);
    Mockito.when(environment.getProperty(
            CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN, int.class, 7))
        .thenReturn(7);
    Mockito.when(environment.getProperty(DefaultLogPublisher.ENABLED, boolean.class, false)).thenReturn(false);
    VertxUtils
        .blockDeploy(SharedVertxFactory.getSharedVertx(environment), TestServerVerticle.class, new DeploymentOptions());
    VertxUtils
        .blockDeploy(SharedVertxFactory.getSharedVertx(environment), TestClientVerticle.class, new DeploymentOptions());

    vertxMetersInitializer.init(registry, eventBus, new MetricsBootstrapConfig(environment));
    logPublisher.setEnvironment(environment);
    logPublisher.init(registry, eventBus, null);

    vertxMetersInitializer.poll(0, 1);
    List<Meter> meters = registry.getMeters();

    testLog(logCollector, meters, true);
    logCollector.clear();

    testLog(logCollector, meters, false);
  }

  private void testLog(LogCollector logCollector, List<Meter> meters, boolean printDetail) {
    Mockito.when(environment.getProperty(ENDPOINTS_CLIENT_DETAIL_ENABLED, boolean.class, true)).thenReturn(printDetail);
    logPublisher.onPolledEvent(new PolledEvent(meters));

    StringBuilder sb = new StringBuilder();
    logCollector.getEvents().forEach(event -> sb.append(event.getMessage()).append("\n"));
    String actual = sb.toString();
    int idx = actual.indexOf("vertx:\n");
    actual = actual.substring(idx);

    String clientLatency;
    String serverLatency;

    String expect = "vertx:\n"
        + "  instances:\n"
        + "    name       eventLoopContext-created\n"
        + "    transport  0\n"
        + "  transport:\n";

    int clientLatencyIndex = actual.indexOf("1            0               0             1           1        ")
        + "1            0               0             1           1        ".length();
    clientLatency = actual.substring(clientLatencyIndex, actual.indexOf(" ", clientLatencyIndex));
    int serverLatencyIndex = actual.lastIndexOf("1            0               0             1           1        ")
        + "1            0               0             1           1        ".length();
    serverLatency = actual.substring(serverLatencyIndex, actual.indexOf(" ", serverLatencyIndex));
    int portSize = String.valueOf(port).length();
    // in new vert.x version, bytes written must be higher than 4K or will be zero
    if (printDetail) {
      expect = expect + "    client.endpoints:\n"
          + "      connectCount disconnectCount queue         connections requests latency send(Bps) receive(Bps) remote\n";
      expect +=
          "      1            0               0             1           1        %-7s 4         4            http://127.0.0.1:%-"
              + portSize + "s\n";
    }
    expect += ""
        + "    server.endpoints:\n"
        + "      connectCount disconnectCount rejectByLimit connections requests latency send(Bps) receive(Bps) listen\n"
        + "      1            0               0             1           1        %-7s 4         4            0.0.0.0:0\n\n";

    if (printDetail) {
      expect = String
          .format(expect, clientLatency, port, serverLatency);
    } else {
      expect = String.format(expect, serverLatency);
    }

    Assertions.assertEquals(expect, actual);
  }
}
