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

package org.apache.servicecomb.transport.rest.vertx;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRestServerVerticle {

  private RestServerVerticle instance = null;

  Future<Void> startFuture = null;

  @Before
  public void setUp() {
    instance = new RestServerVerticle();
    startFuture = Future.future();

    CseContext.getInstance().setTransportManager(new TransportManager());
  }

  @After
  public void tearDown() {
    instance = null;
    startFuture = null;
  }

  @Test
  public void testRestServerVerticleWithRouter(@Mocked Transport transport, @Mocked Vertx vertx,
      @Mocked Context context,
      @Mocked JsonObject jsonObject, @Mocked Future<Void> startFuture) throws Exception {
    URIEndpointObject endpointObject = new URIEndpointObject("http://127.0.0.1:8080");
    new Expectations() {
      {
        transport.parseAddress("http://127.0.0.1:8080");
        result = endpointObject;
      }
    };
    Endpoint endpiont = new Endpoint(transport, "http://127.0.0.1:8080");

    new Expectations() {
      {
        context.config();
        result = jsonObject;
        jsonObject.getValue(AbstractTransport.ENDPOINT_KEY);
        result = endpiont;
      }
    };
    RestServerVerticle server = new RestServerVerticle();
    // process stuff done by Expectations
    server.init(vertx, context);
    server.start(startFuture);
  }

  @Test
  public void testRestServerVerticleWithRouterSSL(@Mocked Transport transport, @Mocked Vertx vertx,
      @Mocked Context context,
      @Mocked JsonObject jsonObject, @Mocked Future<Void> startFuture) throws Exception {
    URIEndpointObject endpointObject = new URIEndpointObject("http://127.0.0.1:8080?sslEnabled=true");
    new Expectations() {
      {
        transport.parseAddress("http://127.0.0.1:8080?sslEnabled=true");
        result = endpointObject;
      }
    };
    Endpoint endpiont = new Endpoint(transport, "http://127.0.0.1:8080?sslEnabled=true");

    new Expectations() {
      {
        context.config();
        result = jsonObject;
        jsonObject.getValue(AbstractTransport.ENDPOINT_KEY);
        result = endpiont;
      }
    };
    RestServerVerticle server = new RestServerVerticle();
    // process stuff done by Expectations
    server.init(vertx, context);
    server.start(startFuture);
  }

  @Test
  public void testRestServerVerticleWithHttp2(@Mocked Transport transport, @Mocked Vertx vertx,
      @Mocked Context context,
      @Mocked JsonObject jsonObject, @Mocked Future<Void> startFuture) {
    URIEndpointObject endpointObject = new URIEndpointObject("http://127.0.0.1:8080?protocol=http2");
    new Expectations() {
      {
        transport.parseAddress("http://127.0.0.1:8080?protocol=http2");
        result = endpointObject;
      }
    };
    Endpoint endpiont = new Endpoint(transport, "http://127.0.0.1:8080?protocol=http2");

    new Expectations() {
      {
        context.config();
        result = jsonObject;
        jsonObject.getValue(AbstractTransport.ENDPOINT_KEY);
        result = endpiont;
      }
    };
    RestServerVerticle server = new RestServerVerticle();
    boolean status = false;
    try {
      server.init(vertx, context);
      server.start(startFuture);
    } catch (Exception e) {
      status = true;
    }
    Assert.assertFalse(status);
  }

  @Test
  public void testStartFutureAddressEmpty() {
    boolean status = false;
    try {
      instance.start(startFuture);
    } catch (Exception ex) {
      status = true;
    }
    Assert.assertFalse(status);
  }

  @Test
  public void testStartFutureAddressNotEmpty() {
    boolean status = false;
    MockForRestServerVerticle.getInstance().mockTransportConfig();
    MockForRestServerVerticle.getInstance().mockRestServerVerticle();
    try {
      instance.start(startFuture);
    } catch (Exception ex) {
      status = true;
    }
    Assert.assertFalse(status);
  }

  @Test
  public void testMountCorsHandler() {
    ArchaiusUtils.setProperty("servicecomb.cors.enabled", true);
    ArchaiusUtils.setProperty("servicecomb.cors.allowedMethod", "GET,PUT,POST");
    ArchaiusUtils.setProperty("servicecomb.cors.allowedHeader", "abc,def");
    ArchaiusUtils.setProperty("servicecomb.cors.exposedHeader", "abc2,def2");
    ArchaiusUtils.setProperty("servicecomb.cors.maxAge", 1);
    Set<HttpMethod> methodSet = new HashSet<>(3);
    methodSet.add(HttpMethod.GET);
    methodSet.add(HttpMethod.PUT);
    methodSet.add(HttpMethod.POST);
    AtomicInteger counter = new AtomicInteger(0);

    CorsHandler corsHandler = new MockUp<CorsHandler>() {
      @Mock
      CorsHandler allowCredentials(boolean allow) {
        Assert.assertFalse(allow);
        counter.incrementAndGet();
        return null;
      }

      @Mock
      CorsHandler allowedHeaders(Set<String> headerNames) {
        Assert.assertThat(headerNames, Matchers.containsInAnyOrder("abc", "def"));
        counter.incrementAndGet();
        return null;
      }

      @Mock
      CorsHandler exposedHeaders(Set<String> headerNames) {
        Assert.assertThat(headerNames, Matchers.containsInAnyOrder("abc2", "def2"));
        counter.incrementAndGet();
        return null;
      }

      @Mock
      CorsHandler allowedMethod(HttpMethod method) {
        Assert.assertTrue(methodSet.contains(method));
        counter.incrementAndGet();
        methodSet.remove(method);
        return null;
      }

      @Mock
      CorsHandler maxAgeSeconds(int maxAgeSeconds) {
        Assert.assertEquals(1, maxAgeSeconds);
        counter.incrementAndGet();
        return null;
      }
    }.getMockInstance();

    new MockUp<RestServerVerticle>() {
      @Mock
      CorsHandler getCorsHandler(String corsAllowedOrigin) {
        Assert.assertEquals("*", corsAllowedOrigin);
        return corsHandler;
      }
    };
    Router router = Mockito.mock(Router.class);
    Mockito.when(router.route()).thenReturn(Mockito.mock(Route.class));

    RestServerVerticle server = new RestServerVerticle();

    Deencapsulation.invoke(server, "mountCorsHandler", router);
    Assert.assertEquals(7, counter.get());
  }
}
