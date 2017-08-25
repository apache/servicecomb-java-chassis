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

package io.servicecomb.demo.client.perf;

import io.servicecomb.core.CseContext;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;
import io.servicecomb.foundation.vertx.VertxUtils;
import io.vertx.core.Vertx;

public class PerfClient {
  public static void main(String[] args) throws Exception {
    Log4jUtils.init();
    BeanUtils.init();

    System.out.println("mode:" + Config.getMode());

    CseContext.getInstance().getConsumerProviderManager().setTransport("pojo", Config.getTransport());
    System.out.printf("test %s performance\n", Config.getTransport());

    if ("reactive".equals(Config.getMode())) {
      Vertx vertx = VertxUtils.getOrCreateVertxByName("perfClient", null);
      VertxUtils.deployVerticle(vertx, ClientVerticle.class, Config.getClientThread());
      return;
    }

    for (int idx = 0; idx < Config.getClientThread(); idx++) {
      new ClientThread().start();
    }
  }
}
