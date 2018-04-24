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
package org.apache.servicecomb.foundation.vertx.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.vertx.SimpleJsonObject;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestClientVerticle {
  ClientVerticle<HttpClientWithContext> clientVerticle = new ClientVerticle<>();

  @Test
  public void start(@Mocked Context context) throws Exception {
    AtomicInteger count = new AtomicInteger();
    ClientPoolManager<HttpClientWithContext> clientMgr = new MockUp<ClientPoolManager<HttpClientWithContext>>() {
      @Mock
      HttpClientWithContext createClientPool(Context context) {
        count.incrementAndGet();
        return null;
      }
    }.getMockInstance();
    clientVerticle.init(null, context);

    JsonObject config = new SimpleJsonObject();
    config.put(ClientVerticle.CLIENT_MGR, clientMgr);
    new Expectations() {
      {
        context.config();
        result = config;
      }
    };

    clientVerticle.start();

    Assert.assertEquals(1, count.get());
  }
}
