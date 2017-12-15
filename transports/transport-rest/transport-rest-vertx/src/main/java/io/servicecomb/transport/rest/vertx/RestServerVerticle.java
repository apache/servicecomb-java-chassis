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

package io.servicecomb.transport.rest.vertx;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.Endpoint;
import io.servicecomb.core.transport.AbstractTransport;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;
import io.servicecomb.foundation.ssl.SSLCustom;
import io.servicecomb.foundation.ssl.SSLOption;
import io.servicecomb.foundation.ssl.SSLOptionFactory;
import io.servicecomb.foundation.vertx.VertxTLSBuilder;
import io.servicecomb.transport.rest.vertx.accesslog.AccessLogConfiguration;
import io.servicecomb.transport.rest.vertx.accesslog.impl.AccessLogHandlerImpl;
import io.servicecomb.transport.rest.vertx.accesslog.parser.impl.DefaultAccessLogPatternParser;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.impl.LoggerHandlerImpl;

public class RestServerVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestServerVerticle.class);

  private static final String SSL_KEY = "rest.provider";

  private static final int ACCEPT_BACKLOG = 2048;

  private static final int SEND_BUFFER_SIZE = 4096;

  private static final int RECEIVE_BUFFER_SIZE = 4096;

  private Endpoint endpoint;

  private URIEndpointObject endpointObject;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    this.endpoint = (Endpoint) context.config().getValue(AbstractTransport.ENDPOINT_KEY);
    this.endpointObject = (URIEndpointObject) endpoint.getAddress();
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    super.start();

    // 如果本地未配置地址，则表示不必监听，只需要作为客户端使用即可
    if (endpointObject == null) {
      LOGGER.warn("rest listen address is not configured, will not start.");
      startFuture.complete();
      return;
    }

    Router mainRouter = Router.router(vertx);
    mountAccessLogHandler(mainRouter);
    initDispatcher(mainRouter);

    HttpServer httpServer = createHttpServer();
    httpServer.requestHandler(mainRouter::accept);

    startListen(httpServer, startFuture);
  }

  private void mountAccessLogHandler(Router mainRouter) {
    if (AccessLogConfiguration.INSTANCE.getAccessLogEnabled()) {
      String pattern = AccessLogConfiguration.INSTANCE.getAccesslogPattern();
      LOGGER.info("access log enabled, pattern = {}", pattern);
      mainRouter.route()
          .handler(new AccessLogHandlerImpl(
              pattern,
              new DefaultAccessLogPatternParser()));
    }
  }

  private void initDispatcher(Router mainRouter) {
    List<VertxHttpDispatcher> dispatchers = SPIServiceUtils.getSortedService(VertxHttpDispatcher.class);
    for (VertxHttpDispatcher dispatcher : dispatchers) {
      LOGGER.info("init vertx http dispatcher {}", dispatcher.getClass().getName());
      dispatcher.init(mainRouter);
    }
  }

  private void startListen(HttpServer server, Future<Void> startFuture) {
    server.listen(endpointObject.getPort(), endpointObject.getHostOrIp(), ar -> {
      if (ar.succeeded()) {
        LOGGER.info("rest listen success. address={}:{}",
            endpointObject.getHostOrIp(),
            ar.result().actualPort());
        startFuture.complete();
        return;
      }

      String msg = String.format("rest listen failed, address=%s:%d",
          endpointObject.getHostOrIp(),
          endpointObject.getPort());
      LOGGER.error(msg, ar.cause());
      startFuture.fail(ar.cause());
    });
  }

  private HttpServer createHttpServer() {
    HttpServerOptions serverOptions = createDefaultHttpServerOptions();
    return vertx.createHttpServer(serverOptions);
  }

  private HttpServerOptions createDefaultHttpServerOptions() {
    HttpServerOptions serverOptions = new HttpServerOptions();
    serverOptions.setAcceptBacklog(ACCEPT_BACKLOG);
    serverOptions.setSendBufferSize(SEND_BUFFER_SIZE);
    serverOptions.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
    serverOptions.setUsePooledBuffers(true);
    serverOptions.setIdleTimeout(TransportConfig.getConnectionIdleTimeoutInSeconds());

    if (endpointObject.isSslEnabled()) {
      SSLOptionFactory factory =
          SSLOptionFactory.createSSLOptionFactory(SSL_KEY, null);
      SSLOption sslOption;
      if (factory == null) {
        sslOption = SSLOption.buildFromYaml(SSL_KEY);
      } else {
        sslOption = factory.createSSLOption();
      }
      SSLCustom sslCustom = SSLCustom.createSSLCustom(sslOption.getSslCustomClass());
      VertxTLSBuilder.buildNetServerOptions(sslOption, sslCustom, serverOptions);
    }

    return serverOptions;
  }
}
