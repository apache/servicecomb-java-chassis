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

import java.net.InetSocketAddress;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class HighwayServerVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(HighwayServerVerticle.class);

  public static final String SSL_KEY = "highway.provider";

  private Endpoint endpoint;

  private URIEndpointObject endpointObject;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    this.endpoint = (Endpoint) context.config().getValue(AbstractTransport.ENDPOINT_KEY);
    this.endpointObject = (URIEndpointObject) this.endpoint.getAddress();
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    super.start();

    startListen(startFuture);
  }

  protected void startListen(Future<Void> startFuture) {
    // 如果本地未配置地址，则表示不必监听，只需要作为客户端使用即可
    if (endpointObject == null) {
      LOGGER.warn("highway listen address is not configured, will not listen.");
      startFuture.complete();
      return;
    }

    HighwayServer server = new HighwayServer(endpointObject);
    server.init(vertx, SSL_KEY, ar -> {
      if (ar.succeeded()) {
        InetSocketAddress socketAddress = ar.result();
        LOGGER.info("highway listen success. address={}:{}",
            socketAddress.getHostString(),
            socketAddress.getPort());
        startFuture.complete();
        return;
      }

      LOGGER.error(HighwayTransport.NAME, ar.cause());
      startFuture.fail(ar.cause());
    });
  }
}
