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

import static io.vertx.core.http.HttpServerOptions.DEFAULT_COMPRESSION_LEVEL;
import static io.vertx.core.http.HttpServerOptions.DEFAULT_DECODER_INITIAL_BUFFER_SIZE;
import static io.vertx.core.http.HttpServerOptions.DEFAULT_DECOMPRESSION_SUPPORTED;
import static io.vertx.core.http.HttpServerOptions.DEFAULT_HTTP2_CONNECTION_WINDOW_SIZE;
import static io.vertx.core.http.HttpServerOptions.DEFAULT_MAX_CHUNK_SIZE;
import static io.vertx.core.http.HttpServerOptions.DEFAULT_MAX_FORM_ATTRIBUTE_SIZE;
import static io.vertx.core.http.HttpServerOptions.DEFAULT_MAX_INITIAL_LINE_LENGTH;
import static org.apache.servicecomb.common.accessLog.AccessLogConfig.CLIENT_LOG_ENABLED;
import static org.apache.servicecomb.common.accessLog.AccessLogConfig.CLIENT_LOG_PATTERN;
import static org.apache.servicecomb.common.accessLog.AccessLogConfig.DEFAULT_CLIENT_PATTERN;
import static org.apache.servicecomb.common.accessLog.AccessLogConfig.DEFAULT_SERVER_PATTERN;
import static org.apache.servicecomb.common.accessLog.AccessLogConfig.SERVER_LOG_ENABLED;
import static org.apache.servicecomb.common.accessLog.AccessLogConfig.SERVER_LOG_PATTERN;
import static org.apache.servicecomb.core.transport.AbstractTransport.PUBLISH_ADDRESS;
import static org.apache.servicecomb.transport.rest.vertx.TransportConfig.DEFAULT_SERVER_COMPRESSION_SUPPORT;
import static org.apache.servicecomb.transport.rest.vertx.TransportConfig.DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND;
import static org.apache.servicecomb.transport.rest.vertx.TransportConfig.DEFAULT_SERVER_MAX_HEADER_SIZE;
import static org.apache.servicecomb.transport.rest.vertx.TransportConfig.SERVICECOMB_CORS_CONFIG_BASE;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpClientConfig;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.file.impl.FileResolverImpl;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRestServerVerticle {

  private RestServerVerticle instance = null;

  Promise<Void> startPromise = null;

  Environment environment = Mockito.mock(Environment.class);

  @Before
  public void setUp() {
    Mockito.when(environment.getProperty(
            "servicecomb.request.timeout", long.class, (long) TcpClientConfig.DEFAULT_LOGIN_TIMEOUT))
        .thenReturn((long) TcpClientConfig.DEFAULT_LOGIN_TIMEOUT);
    Mockito.when(environment.getProperty("servicecomb.rest.client.verticle-count", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty("servicecomb.rest.client.thread-count", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty("servicecomb.rest.server.verticle-count", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty("servicecomb.rest.server.thread-count", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty("servicecomb.http.dispatcher.rest.order", int.class, Integer.MAX_VALUE))
        .thenReturn(Integer.MAX_VALUE);
    Mockito.when(environment.getProperty("servicecomb.rest.publishPort", int.class, 0))
        .thenReturn(0);
    Mockito.when(environment.getProperty("servicecomb.http.dispatcher.rest.enabled", boolean.class, true))
        .thenReturn(true);
    Mockito.when(environment.getProperty(SERVICECOMB_CORS_CONFIG_BASE + ".enabled", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty(PUBLISH_ADDRESS, String.class, ""))
        .thenReturn("");
    Mockito.when(environment.getProperty(
            RestConst.UPLOAD_MAX_SIZE, long.class, -1L))
        .thenReturn(-1L);
    Mockito.when(environment.getProperty(RestConst.UPLOAD_MAX_FILE_SIZE, long.class, -1L))
        .thenReturn(-1L);
    Mockito.when(environment.getProperty(RestConst.UPLOAD_FILE_SIZE_THRESHOLD, int.class, 0))
        .thenReturn(0);
    Mockito.when(environment.getProperty("servicecomb.rest.server.compression", boolean.class,
            DEFAULT_SERVER_COMPRESSION_SUPPORT))
        .thenReturn(false);
    Mockito.when(environment.getProperty("servicecomb.rest.server.maxHeaderSize", int.class,
            DEFAULT_SERVER_MAX_HEADER_SIZE))
        .thenReturn(DEFAULT_SERVER_MAX_HEADER_SIZE);
    Mockito.when(environment.getProperty("servicecomb.rest.server.maxFormAttributeSize", int.class,
            DEFAULT_MAX_FORM_ATTRIBUTE_SIZE))
        .thenReturn(DEFAULT_MAX_FORM_ATTRIBUTE_SIZE);
    Mockito.when(environment.getProperty("servicecomb.rest.server.compressionLevel", int.class,
            DEFAULT_COMPRESSION_LEVEL))
        .thenReturn(DEFAULT_COMPRESSION_LEVEL);
    Mockito.when(environment.getProperty("servicecomb.rest.server.maxChunkSize", int.class,
            DEFAULT_MAX_CHUNK_SIZE))
        .thenReturn(DEFAULT_MAX_CHUNK_SIZE);
    Mockito.when(environment.getProperty("servicecomb.rest.server.decompressionSupported", boolean.class,
            DEFAULT_DECOMPRESSION_SUPPORTED))
        .thenReturn(DEFAULT_DECOMPRESSION_SUPPORTED);
    Mockito.when(environment.getProperty("servicecomb.rest.server.decoderInitialBufferSize", int.class,
            DEFAULT_DECODER_INITIAL_BUFFER_SIZE))
        .thenReturn(DEFAULT_DECODER_INITIAL_BUFFER_SIZE);
    Mockito.when(environment.getProperty("servicecomb.rest.server.maxInitialLineLength", int.class,
            DEFAULT_MAX_INITIAL_LINE_LENGTH))
        .thenReturn(DEFAULT_MAX_INITIAL_LINE_LENGTH);
    Mockito.when(environment.getProperty("servicecomb.rest.server.connection.idleTimeoutInSeconds", int.class,
            DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND))
        .thenReturn(DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND);
    Mockito.when(environment.getProperty("servicecomb.rest.server.http2.useAlpnEnabled", boolean.class,
            true))
        .thenReturn(true);
    Mockito.when(environment.getProperty("servicecomb.rest.server.http2ConnectionWindowSize", int.class,
            DEFAULT_HTTP2_CONNECTION_WINDOW_SIZE))
        .thenReturn(DEFAULT_HTTP2_CONNECTION_WINDOW_SIZE);
    Mockito.when(environment.getProperty("servicecomb.rest.server.http2.connection.idleTimeoutInSeconds", int.class,
            DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND))
        .thenReturn(DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND);
    Mockito.when(environment.getProperty("servicecomb.rest.server.http2.pushEnabled", boolean.class,
            Http2Settings.DEFAULT_ENABLE_PUSH))
        .thenReturn(Http2Settings.DEFAULT_ENABLE_PUSH);
    Mockito.when(environment.getProperty("servicecomb.transport.eventloop.size", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty(FileResolverImpl.DISABLE_CP_RESOLVING_PROP_NAME, boolean.class, true))
        .thenReturn(true);
    Mockito.when(environment.getProperty(CLIENT_LOG_ENABLED, boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty(SERVER_LOG_ENABLED, boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty(CLIENT_LOG_PATTERN, String.class, DEFAULT_CLIENT_PATTERN))
        .thenReturn(DEFAULT_CLIENT_PATTERN);
    Mockito.when(environment.getProperty(SERVER_LOG_PATTERN, String.class, DEFAULT_SERVER_PATTERN))
        .thenReturn(DEFAULT_CLIENT_PATTERN);
    LegacyPropertyFactory.setEnvironment(environment);

    instance = new RestServerVerticle();
    startPromise = Promise.promise();

    SCBBootstrap.createSCBEngineForTest();
  }

  @After
  public void tearDown() {
    instance = null;
    startPromise = null;
    SCBEngine.getInstance().destroy();
  }

  @Test
  public void testRestServerVerticleWithRouter(@Mocked Transport transport, @Mocked Vertx vertx,
      @Mocked Context context,
      @Mocked JsonObject jsonObject, @Mocked Promise<Void> startPromise) throws Exception {

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
    server.start(startPromise);
  }

  @Test
  public void testRestServerVerticleWithRouterSSL(@Mocked Transport transport, @Mocked Vertx vertx,
      @Mocked Context context,
      @Mocked JsonObject jsonObject, @Mocked Promise<Void> startPromise) throws Exception {
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
    server.start(startPromise);
  }


  @Test
  public void testStartFutureAddressEmpty() {
    boolean status = false;
    try {
      instance.start(startPromise);
    } catch (Exception ex) {
      status = true;
    }
    Assertions.assertFalse(status);
  }

  @Test
  public void testStartFutureAddressNotEmpty() {
    boolean status = false;
    MockForRestServerVerticle.getInstance().mockTransportConfig();
    MockForRestServerVerticle.getInstance().mockRestServerVerticle();
    try {
      instance.start(startPromise);
    } catch (Exception ex) {
      status = true;
    }
    Assertions.assertFalse(status);
  }

  @Test
  public void testMountCorsHandler() {
    Mockito.when(environment.getProperty("servicecomb.cors.enabled", boolean.class,
            false))
        .thenReturn(true);
    Mockito.when(environment.getProperty("servicecomb.cors.origin", String.class,
            "*"))
        .thenReturn("*");
    Mockito.when(environment.getProperty("servicecomb.cors.allowedMethod"))
        .thenReturn("GET,PUT,POST");
    Mockito.when(environment.getProperty("servicecomb.cors.allowedHeader"))
        .thenReturn("abc,def");
    Mockito.when(environment.getProperty("servicecomb.cors.exposedHeader"))
        .thenReturn("abc2,def2");
    Mockito.when(environment.getProperty("servicecomb.cors.maxAge", int.class, -1))
        .thenReturn(1);
    Mockito.when(environment.getProperty("servicecomb.cors.allowCredentials", boolean.class, false))
        .thenReturn(false);

    Set<HttpMethod> methodSet = new HashSet<>(3);
    methodSet.add(HttpMethod.GET);
    methodSet.add(HttpMethod.PUT);
    methodSet.add(HttpMethod.POST);
    AtomicInteger counter = new AtomicInteger(0);

    CorsHandler corsHandler = new MockUp<CorsHandler>() {
      @Mock
      CorsHandler allowCredentials(boolean allow) {
        Assertions.assertFalse(allow);
        counter.incrementAndGet();
        return null;
      }

      @Mock
      CorsHandler allowedHeaders(Set<String> headerNames) {
        MatcherAssert.assertThat(headerNames, Matchers.containsInAnyOrder("abc", "def"));
        counter.incrementAndGet();
        return null;
      }

      @Mock
      CorsHandler exposedHeaders(Set<String> headerNames) {
        MatcherAssert.assertThat(headerNames, Matchers.containsInAnyOrder("abc2", "def2"));
        counter.incrementAndGet();
        return null;
      }

      @Mock
      CorsHandler allowedMethod(HttpMethod method) {
        Assertions.assertTrue(methodSet.contains(method));
        counter.incrementAndGet();
        methodSet.remove(method);
        return null;
      }

      @Mock
      CorsHandler maxAgeSeconds(int maxAgeSeconds) {
        Assertions.assertEquals(1, maxAgeSeconds);
        counter.incrementAndGet();
        return null;
      }
    }.getMockInstance();

    new MockUp<RestServerVerticle>() {
      @Mock
      CorsHandler getCorsHandler(String corsAllowedOrigin) {
        Assertions.assertEquals("*", corsAllowedOrigin);
        return corsHandler;
      }
    };
    Router router = Mockito.mock(Router.class);
    Mockito.when(router.route()).thenReturn(Mockito.mock(Route.class));

    RestServerVerticle server = new RestServerVerticle();

    server.mountCorsHandler(router);
    Assertions.assertEquals(7, counter.get());
  }

  @Test
  public void mountGlobalRestFailureHandler() {
    Router mainRouter = Mockito.mock(Router.class);
    Holder<Handler<RoutingContext>> handlerHolder = new Holder<>();
    Holder<Route> routeHolder = new Holder<>();
    Route route = new MockUp<Route>() {
      @Mock
      Route failureHandler(Handler<RoutingContext> failureHandler) {
        handlerHolder.value = failureHandler;
        return null;
      }

      @Mock
      Route handler(io.vertx.core.Handler<io.vertx.ext.web.RoutingContext> requestHandler) {
        return routeHolder.value;
      }
    }.getMockInstance();
    routeHolder.value = route;

    Mockito.when(mainRouter.route()).thenReturn(route);

    RestServerVerticle restServerVerticle = new RestServerVerticle();

    restServerVerticle.mountGlobalRestFailureHandler(mainRouter);
    Assertions.assertNotNull(handlerHolder.value);

    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse response = Mockito.mock(HttpServerResponse.class);
    Mockito.when(response.setStatusCode(500)).thenReturn(response);
    Mockito.when(response.putHeader("Content-Type", "application/json")).thenReturn(response);
    Mockito.when(routingContext.response()).thenReturn(response);

    handlerHolder.value.handle(routingContext);
    Mockito.verify(response).end("{\"message\":\"unknown error\"}");
  }
}
