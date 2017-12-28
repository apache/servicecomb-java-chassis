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
package io.servicecomb.foundation.vertx.client.http;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.foundation.vertx.SimpleJsonObject;
import io.servicecomb.foundation.vertx.client.AbstractClientVerticle;
import io.servicecomb.foundation.vertx.client.ClientPoolManager;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import mockit.Expectations;
import mockit.Mocked;

public class TestHttpClientVerticle {
  HttpClientVerticle verticle = new HttpClientVerticle();

  @Test
  public void start(@Mocked Vertx vertx, @Mocked Context context) throws Exception {
    ClientPoolManager<HttpClientWithContext> clientMgr = new ClientPoolManager<>();

    JsonObject jsonObject = new SimpleJsonObject();
    jsonObject.put(AbstractClientVerticle.CLIENT_MGR, clientMgr);

    new Expectations() {
      {
        context.config();
        result = jsonObject;
      }
    };
    verticle.init(vertx, context);
    verticle.start();

    HttpClientWithContext result = clientMgr.findThreadBindClientPool();
    Assert.assertSame(context, result.context());
  }
}
