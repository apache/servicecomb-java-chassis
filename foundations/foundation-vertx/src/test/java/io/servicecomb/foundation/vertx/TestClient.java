/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.vertx;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.foundation.vertx.client.http.HttpClientVerticle;
import io.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import io.vertx.core.Vertx;

public class TestClient {

  @Test
  public void testHttpClientVerticle() throws Exception {
    Vertx vertx = VertxUtils.init(null);

    HttpClientVerticle oVerticle = new HttpClientVerticle();
    oVerticle.init(vertx, null);
    Assert.assertEquals("clientMgr", HttpClientVerticle.CLIENT_MGR);
    Assert.assertEquals("poolCount", HttpClientVerticle.POOL_COUNT);
    Assert.assertEquals("clientOptions", HttpClientVerticle.CLIENT_OPTIONS);
    HttpClientWithContext oContextClient = new HttpClientWithContext(null, null);
    Assert.assertEquals(null, oContextClient.getHttpClient());

    vertx.close();
  }
}
