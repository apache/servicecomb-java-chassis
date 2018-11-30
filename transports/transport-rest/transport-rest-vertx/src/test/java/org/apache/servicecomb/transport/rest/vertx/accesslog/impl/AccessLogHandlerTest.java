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

package org.apache.servicecomb.transport.rest.vertx.accesslog.impl;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Holder;

import org.apache.log4j.Level;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import mockit.Mock;
import mockit.MockUp;

public class AccessLogHandlerTest {
  private static final AccessLogHandler ACCESS_LOG_HANDLER = new AccessLogHandler(
      "%h - - %s durationMillisecond=[%D] %{test-config}user-defined %{cookie-name}C %v");

  private LogCollector logCollector;

  @Before
  public void setUp() {
    logCollector = new LogCollector();
    logCollector.setLogLevel("accesslog", Level.INFO);
  }

  @After
  public void tearDown() {
    logCollector.teardown();
  }

  @Test
  public void testHandle() {
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    HashSet<Cookie> cookies = new HashSet<>();
    Cookie cookie = Mockito.mock(Cookie.class);
    HttpServerResponse httpServerResponse = new MockUp<HttpServerResponse>() {
      @Mock
      public HttpServerResponse endHandler(Handler<Void> handler) {
        handler.handle(null);
        return null;
      }

      @Mock
      public int getStatusCode() {
        return 200;
      }
    }.getMockInstance();
    HttpServerRequest httpServerRequest = Mockito.mock(HttpServerRequest.class);
    SocketAddress remoteSocketAddress = Mockito.mock(SocketAddress.class);
    SocketAddress localSocketAddress = Mockito.mock(SocketAddress.class);

    Holder<Integer> counter = new Holder<>();
    counter.value = 0;
    String testThreadName = Thread.currentThread().getName();
    new MockUp<System>() {
      @Mock
      long currentTimeMillis() {
        if (!testThreadName.equals(Thread.currentThread().getName())) {
          return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        }
        if (counter.value < 1) {
          ++counter.value;
          return 1L;
        }
        return 123L;
      }
    };
    cookies.add(cookie);
    Mockito.when(cookie.getName()).thenReturn("cookie-name");
    Mockito.when(cookie.getValue()).thenReturn("cookie-value");
    Mockito.when(routingContext.cookies()).thenReturn(cookies);
    Mockito.when(routingContext.response()).thenReturn(httpServerResponse);
    Mockito.when(routingContext.request()).thenReturn(httpServerRequest);
    Mockito.when(httpServerRequest.remoteAddress()).thenReturn(remoteSocketAddress);
    Mockito.when(remoteSocketAddress.host()).thenReturn("192.168.0.22");
    Mockito.when(httpServerRequest.localAddress()).thenReturn(localSocketAddress);
    Mockito.when(localSocketAddress.host()).thenReturn("192.168.0.33");
    ACCESS_LOG_HANDLER.handle(routingContext);

    Assert.assertEquals(
        "192.168.0.22 - - 200 durationMillisecond=[122] user-defined-test-config cookie-value 192.168.0.33",
        logCollector.getEvents().get(0).getMessage());
  }
}