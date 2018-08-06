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

package org.apache.servicecomb.transport.highway;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.transport.common.MockUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import mockit.Expectations;
import mockit.Mocked;

public class TestHighwayVerticle {
  @Test
  public void testHighwayVerticle(@Mocked Transport transport, @Mocked Vertx vertx, @Mocked Context context,
      @Mocked JsonObject json) {
    HighwayServerVerticle highwayVerticle = new HighwayServerVerticle(new AtomicInteger());
    URIEndpointObject endpiontObject = new URIEndpointObject("highway://127.0.0.1:9090");
    new Expectations() {
      {
        transport.parseAddress(anyString);
        result = endpiontObject;
      }
    };

    Endpoint endpoint = new Endpoint(transport, "highway://127.0.0.1:9090");

    new Expectations() {
      {
        context.config();
        result = json;
        json.getValue(anyString);
        result = endpoint;
      }
    };

    highwayVerticle.init(vertx, context);
    @SuppressWarnings("unchecked")
    Future<Void> startFuture = Mockito.mock(Future.class);
    highwayVerticle.startListen(startFuture);
    MockUtil.getInstance().mockHighwayConfig();
    try {
      highwayVerticle.startListen(startFuture);
      assertTrue(true);
    } catch (Exception e) {
      Assert.fail();
    }
  }
}
