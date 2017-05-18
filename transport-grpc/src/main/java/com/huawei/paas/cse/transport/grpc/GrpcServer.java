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

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class GrpcServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServer.class);

    public GrpcServer(Router router) {
        router.post("/:schema/:operation").failureHandler(this::failureHandler).handler(this::onRequest);
    }

    private void failureHandler(RoutingContext routingContext) {
        LOGGER.error("http server failed.", routingContext.failure());
    }

    private void onRequest(RoutingContext routingContext) {
        GrpcServerInvoke invoke = new GrpcServerInvoke();
        invoke.init(routingContext);
        invoke.execute();
    }
}
