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

package io.servicecomb.serviceregistry.client.http;

import org.junit.Test;

import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.vertx.core.http.HttpVersion;
import mockit.Mock;
import mockit.MockUp;

public class TestClientPool {
  @Test
  public void testHttpClientPool() {
    new MockUp<ServiceRegistryConfig>() {
      @Mock
      public HttpVersion getHttpVersion() {
        return HttpVersion.HTTP_2;
      }

      @Mock
      public boolean isSsl() {
        return true;
      }
    };
    HttpClientPool.INSTANCE.create();
  }

  @Test
  public void testWebsocketClientPool() {
    new MockUp<ServiceRegistryConfig>() {
      @Mock
      public HttpVersion getHttpVersion() {
        return HttpVersion.HTTP_2;
      }

      @Mock
      public boolean isSsl() {
        return true;
      }
    };
    WebsocketClientPool.INSTANCE.create();
  }
}
