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

package io.servicecomb.transport.rest.vertx;

import org.mockito.Mockito;

import io.servicecomb.foundation.common.net.IpPort;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import mockit.Mock;
import mockit.MockUp;

public class MockForRestServerVerticle {

  private static MockForRestServerVerticle instance = new MockForRestServerVerticle();

  private MockForRestServerVerticle() {
    // private constructor for Singleton
  }

  public static MockForRestServerVerticle getInstance() {
    return instance;
  }

  public void mockRestServerVerticle() {
    final HttpServer server = Mockito.mock(HttpServer.class);
    new MockUp<RestServerVerticle>() {

      @Mock
      private void startListen(HttpServer server, IpPort ipPort, Future<Void> startFuture) {

      }

      @Mock
      private HttpServer createHttpServer(boolean isHttp_2) {

        return server;
      }
    };
  }

  public void mockTransportConfig() {
    new MockUp<TransportConfig>() {
      @Mock
      public String getAddress() {
        return "Address";
      }
    };
  }
}
