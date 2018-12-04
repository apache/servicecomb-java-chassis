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

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.core.transport.TransportVertxFactory;
import org.apache.servicecomb.foundation.metrics.PolledEvent;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.metrics.core.publish.DefaultLogPublisher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Registry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import mockit.Expectations;

public class TestVertxMetersInitializer {
  GlobalRegistry globalRegistry = new GlobalRegistry(new ManualClock());

  Registry registry = new DefaultRegistry(globalRegistry.getClock());

  EventBus eventBus = new EventBus();

  TransportVertxFactory transportVertxFactory;

  VertxMetersInitializer vertxMetersInitializer = new VertxMetersInitializer();

  DefaultLogPublisher logPublisher = new DefaultLogPublisher();

  static int port;

  static String body = "body";

  public static class TestServerVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) {
      Router mainRouter = Router.router(vertx);
      mainRouter.route("/").handler(context -> {
        context.response().end(context.getBody());
      });

      HttpServer server = vertx.createHttpServer();
      server.requestHandler(mainRouter::accept);
      server.listen(0, "0.0.0.0", ar -> {
        if (ar.succeeded()) {
          port = ar.result().actualPort();
          startFuture.complete();
          return;
        }

        startFuture.fail(ar.cause());
      });
    }
  }

  public static class TestClientVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) {
      HttpClient client = vertx.createHttpClient();
      client.post(port, "127.0.0.1", "/").handler(resp -> {
        resp.bodyHandler((buffer) -> {
        });
        startFuture.complete();
      }).end(body);
    }
  }

  @Before
  public void setup() {
    VertxUtils.blockCloseVertxByName("transport");
  }

  @After
  public void teardown() {
    VertxUtils.blockCloseVertxByName("transport");
  }

  @Test
  public void init() throws InterruptedException {
    transportVertxFactory = new TransportVertxFactory();
    new Expectations(AbstractTransport.class) {
      {
        AbstractTransport.getTransportVertxFactory();
        result = transportVertxFactory;
      }
    };

    globalRegistry.add(registry);
    vertxMetersInitializer.init(globalRegistry, eventBus, null);
    logPublisher.init(null, eventBus, null);
    VertxUtils
        .blockDeploy(transportVertxFactory.getTransportVertx(), TestServerVerticle.class, new DeploymentOptions());
    VertxUtils
        .blockDeploy(transportVertxFactory.getTransportVertx(), TestClientVerticle.class, new DeploymentOptions());

    globalRegistry.poll(1);
    List<Meter> meters = Lists.newArrayList(registry.iterator());
    List<Measurement> measurements = new ArrayList<>();
    for (Meter meter : meters) {
      meter.measure().forEach(measurements::add);
    }

    LogCollector logCollector = new LogCollector();

    testLog(logCollector, meters, measurements, true);
    logCollector.clear();
    testLog(logCollector, meters, measurements, false);

    logCollector.teardown();
  }

  private void testLog(LogCollector logCollector, List<Meter> meters, List<Measurement> measurements,
      boolean printDetail) {
    ArchaiusUtils.setProperty(DefaultLogPublisher.ENDPOINTS_CLIENT_DETAIL_ENABLED, String.valueOf(printDetail));
    logPublisher.onPolledEvent(new PolledEvent(meters, measurements));

    StringBuilder sb = new StringBuilder();
    logCollector.getEvents().forEach(event -> sb.append(event.getMessage()).append("\n"));
    String actual = sb.toString();
    int idx = actual.indexOf("vertx:\n");
    actual = actual.substring(idx);

    String expect = "vertx:\n"
        + "  instances:\n"
        + "    name       eventLoopContext-created\n"
        + "    transport  4\n"
        + "  transport:\n"
        + "    client.endpoints:\n"
        + "      remote                connectCount    disconnectCount connections     send         receive\n";
    if (printDetail) {
      expect += String.format(
          "      127.0.0.1:%-5s       1               0               1               4 B          21 B        \n",
          port);
    }
    expect += "      (summary)             1               0               1               4 B          21 B        \n"
        + "    server.endpoints:\n"
        + "      listen                connectCount    disconnectCount rejectByLimit   connections  send         receive\n"
        + "      0.0.0.0:0             1               0               0               1            21 B         4 B         \n"
        + "      (summary)             1               0               0               1            21 B         4 B         \n\n";
    Assert.assertEquals(expect, actual);
  }
}
