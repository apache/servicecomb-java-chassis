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

package org.apache.servicecomb.localservice.it;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.localservice.LocalServicePriorityFilter;
import org.apache.servicecomb.localservice.TestLocalServicePriorityFilter.StubInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

/**
 * Integration test: the endpoint rewritten by LocalServicePriorityFilter
 * (rest://127.0.0.1:port, SSL parameters dropped) must be dialable through
 * the real loopback interface.
 *
 * <p>Run explicitly: {@code mvn test -Dtest=*IT -pl handlers/handler-localservice}
 */
public class TestLocalServicePriorityFilterEndpointIT {
  private static final String SELF_HOST;

  private static final boolean HAS_REAL_LOCAL_IP;

  static {
    String host;
    boolean real;
    try {
      InetAddress localHost = InetAddress.getLocalHost();
      host = localHost.getHostAddress();
      real = !localHost.isLoopbackAddress();
    } catch (Exception e) {
      host = "127.0.0.1";
      real = false;
    }
    SELF_HOST = host;
    HAS_REAL_LOCAL_IP = real;
  }

  private static final LocalServicePriorityFilter FILTER = new LocalServicePriorityFilter();

  private static Vertx vertx;

  private static HttpServer providerServer;

  private static int port;

  private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(2))
      .build();

  @BeforeAll
  public static void setUp() throws Exception {
    Assumptions.assumeTrue(HAS_REAL_LOCAL_IP,
        "test machine only has loopback address, real network tests are skipped");

    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }

    vertx = Vertx.vertx();
    Router router = Router.router(vertx);
    router.route("/ping").handler(ctx -> ctx.response().setStatusCode(200).end("pong"));
    providerServer = vertx.createHttpServer(new HttpServerOptions().setHost("0.0.0.0").setPort(port));
    providerServer.requestHandler(router);
    providerServer.listen(port, "0.0.0.0")
        .toCompletionStage()
        .toCompletableFuture()
        .get(10, TimeUnit.SECONDS);
  }

  @AfterAll
  public static void tearDown() throws Exception {
    if (providerServer != null) {
      providerServer.close().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
    if (vertx != null) {
      vertx.close().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
  }

  private static int status(String url) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    return response.statusCode();
  }

  @Test
  public void rewrittenEndpointIsDialableThroughLoopback() throws Exception {
    // consumer and provider colocated on the same host, provider supports local service
    Map<String, String> providerProps = new HashMap<>();
    providerProps.put("supportLocalService", "true");
    providerProps.put(LocalServicePriorityFilter.PROPERTY_HOST_NAME, SELF_HOST);
    StubInstance provider = new StubInstance("p1", SELF_HOST, providerProps);
    // provider endpoint published with the external address and ssl enabled
    ServiceCombServer original = new ServiceCombServer("provider",
        new Endpoint(dummyTransport(), "rest://" + SELF_HOST + ":" + port + "?sslEnabled=true", provider));

    // C == P == 1 with a local P1: all traffic goes to the local instance
    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    FILTER.calculateInstanceWeights(weights, List.of(original), List.of(new StubInstance("c1", SELF_HOST, null)));

    Assertions.assertNotNull(weights.p1);

    for (int i = 0; i < 20; i++) {
      ServiceCombServer selected = FILTER.selectByWeights(weights);
      String rewrittenEndpoint = selected.getEndpoint().getEndpoint();

      // the rewritten endpoint is well formed, SSL parameters dropped
      URIEndpointObject endpointObject = new URIEndpointObject(rewrittenEndpoint);
      Assertions.assertEquals("127.0.0.1", endpointObject.getHostOrIp());
      Assertions.assertEquals(port, endpointObject.getPort());
      Assertions.assertFalse(endpointObject.isSslEnabled());
      Assertions.assertSame(provider, selected.getEndpoint().getMicroserviceInstance());

      // and it is really dialable through the loopback interface
      IpPort target = endpointObject;
      Assertions.assertEquals(200,
          status("http://" + target.getHostOrIp() + ":" + target.getPort() + "/ping"));
    }
  }

  @Test
  public void nonLocalProviderIsNotRewritten() {
    // provider not supporting local service on the same host is a P2 candidate
    Map<String, String> providerProps = new HashMap<>();
    providerProps.put(LocalServicePriorityFilter.PROPERTY_HOST_NAME, SELF_HOST);
    StubInstance provider = new StubInstance("p1", SELF_HOST, providerProps);
    ServiceCombServer original = new ServiceCombServer("provider",
        new Endpoint(dummyTransport(), "rest://" + SELF_HOST + ":" + port, provider));

    LocalServicePriorityFilter.InstanceWeights weights = new LocalServicePriorityFilter.InstanceWeights();
    FILTER.calculateInstanceWeights(weights, List.of(original), List.of(new StubInstance("c1", SELF_HOST, null)));

    Assertions.assertNull(weights.p1);
    Assertions.assertEquals(1, weights.p2List.size());
    // C == P == 1 without a local provider: the P2 weight is 1/P - 1/C = 0,
    // so no weighted selection happens and the filter falls back to returning
    // the full server list - in particular the endpoint is NOT rewritten
    Assertions.assertNull(FILTER.selectByWeights(weights));
    Assertions.assertSame(original, weights.p2List.get(0));
    Assertions.assertEquals("rest://" + SELF_HOST + ":" + port, original.getEndpoint().getEndpoint());
  }

  /**
   * A Transport stub only good for building Endpoint objects; the tests never
   * dial through this transport, the rewritten endpoint is called via HttpClient.
   * (Avoids Mockito self-attachment issues in some JVMs.)
   */
  private static Transport dummyTransport() {
    return new Transport() {
      @Override
      public String getName() {
        return "dummy";
      }

      @Override
      public boolean init() {
        return false;
      }

      @Override
      public void setEnvironment(Environment environment) {
        // no-op
      }

      @Override
      public Object parseAddress(String endpoint) {
        return null;
      }

      @Override
      public Endpoint getEndpoint() {
        return null;
      }

      @Override
      public Endpoint getPublishEndpoint() {
        return null;
      }
    };
  }
}
