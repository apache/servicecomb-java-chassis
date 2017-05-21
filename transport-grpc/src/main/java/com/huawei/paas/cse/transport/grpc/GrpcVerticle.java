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

package com.huawei.paas.cse.transport.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.servicecomb.core.transport.AbstractTransport;
import com.huawei.paas.foundation.common.net.IpPort;
import com.huawei.paas.foundation.common.net.NetUtils;
import com.huawei.paas.foundation.vertx.client.http.HttpClientVerticle;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;

public class GrpcVerticle extends HttpClientVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcVerticle.class);

    private static final int ACCEPT_BACKLOG = 2048;

    private static final int SEND_BUFFER_SIZE = 4096;

    private static final int RECEIVE_BUFFER_SIZE = 4096;

    private String endpoint;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.endpoint = context.config().getString(AbstractTransport.ENDPOINT_KEY);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();

        startListen(startFuture);
    }

    protected void startListen(Future<Void> startFuture) {
        // 如果本地未配置grpc地址，则表示不必监听，只需要作为客户端使用即可
        if (StringUtils.isEmpty(this.endpoint)) {
            LOGGER.warn("grpc listen address is not configured, will not listen.");
            startFuture.complete();
            return;
        }

        Router mainRouter = Router.router(vertx);
        mainRouter.route().handler(new GrpcBodyHandler());
        new GrpcServer(mainRouter);

        HttpServerOptions serverOptions = new HttpServerOptions();
        serverOptions.setAcceptBacklog(ACCEPT_BACKLOG);
        serverOptions.setSendBufferSize(SEND_BUFFER_SIZE);
        serverOptions.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
        serverOptions.setUsePooledBuffers(true);
        String key = System.getProperty("store.key");
        if (key != null && !key.isEmpty()) {
            serverOptions.setUseAlpn(true);
            serverOptions.setSsl(true);
            serverOptions.setKeyStoreOptions(new JksOptions().setPath(System.getProperty("store.key"))
                    .setPassword(System.getProperty("store.pass")));
        }

        HttpServer server = vertx.createHttpServer(serverOptions).requestHandler(mainRouter::accept);

        IpPort ipPort = NetUtils.parseIpPortFromURI(this.endpoint);
        if (ipPort == null) {
            LOGGER.error("wrong grpc listen address {}", this.endpoint);
            return;
        }

        startListen(server, ipPort, startFuture);
    }

    private void startListen(HttpServer server, IpPort ipPort, Future<Void> startFuture) {
        server.listen(ipPort.getPort(), ipPort.getHostOrIp(), ar -> {
            if (ar.succeeded()) {
                LOGGER.info("gRPC listen success. address={}:{}", ipPort.getHostOrIp(), ar.result().actualPort());
                startFuture.complete();
                return;
            }

            String msg = String.format("gRPC listen failed, address=%s:%d", ipPort.getHostOrIp(), ipPort.getPort());
            LOGGER.error(msg, ar.cause());
            startFuture.fail(ar.cause());
        });
    }
}
