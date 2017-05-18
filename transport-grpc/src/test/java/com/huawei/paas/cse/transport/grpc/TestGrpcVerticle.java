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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.huawei.paas.cse.transport.common.MockUtil;
import com.huawei.paas.foundation.common.net.IpPort;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestGrpcVerticle {

    @Test
    public void testGrpcVerticle(@Mocked Vertx vertx, @Mocked Context context, @Mocked JsonObject json) throws Exception {
        new Expectations() {
            {
                context.config();
                result = json;
                json.getString(anyString);
                result = "grpc://127.0.0.1:9090";
            }
        };
        GrpcVerticle grpcVerticle = new GrpcVerticle();
        grpcVerticle.init(vertx, context);
        @SuppressWarnings("unchecked")
        Future<Void> startFuture = Mockito.mock(Future.class);
        grpcVerticle.startListen(startFuture);
        MockUtil.getInstance().mockGrpcConfig();
        try {
            grpcVerticle.startListen(startFuture);
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        IpPort ipPort = Mockito.mock(IpPort.class);
        HttpServer server = Mockito.mock(HttpServer.class);
        Deencapsulation.invoke(grpcVerticle, "startListen", server, ipPort, startFuture);
        Assert.assertEquals("clientMgr", GrpcVerticle.CLIENT_MGR);
    }

}
