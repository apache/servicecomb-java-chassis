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

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import mockit.Expectations;
import mockit.Mocked;

public class TestRestServerVerticle {

  private RestServerVerticle instance = null;

  Future<Void> startFuture = null;

  @Before
  public void setUp() throws Exception {
    instance = new RestServerVerticle();
    startFuture = Future.future();

    CseContext.getInstance().setTransportManager(new TransportManager());
  }

  @After
  public void tearDown() throws Exception {
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
}
