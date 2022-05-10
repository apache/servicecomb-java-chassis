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

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.transport.common.MockUtil;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import mockit.Expectations;
import mockit.Mocked;

public class TestHighwayVerticle {
  @Test
  public void testHighwayVehicle(@Mocked Transport transport, @Mocked Vertx vertx, @Mocked Context context,
                                 @Mocked JsonObject json) {
    HighwayServerVerticle highwayVehicle = new HighwayServerVerticle();
    URIEndpointObject endpointObject = new URIEndpointObject("highway://127.0.0.1:9090");
    new Expectations() {
      {
        transport.parseAddress(anyString);
        result = endpointObject;
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

    highwayVehicle.init(vertx, context);
    @SuppressWarnings("unchecked")
    Promise<Void> startPromise = Mockito.mock(Promise.class);
    highwayVehicle.startListen(startPromise);
    MockUtil.getInstance().mockHighwayConfig();
    try {
      highwayVehicle.startListen(startPromise);
      Assertions.assertTrue(true);
    } catch (Exception e) {
      Assertions.fail();
    }
  }
}
