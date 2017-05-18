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

package com.huawei.paas.foundation.vertx.server;

import java.net.InetSocketAddress;

import com.huawei.paas.foundation.common.net.URIEndpointObject;
import com.huawei.paas.foundation.ssl.SSLCustom;
import com.huawei.paas.foundation.ssl.SSLOption;
import com.huawei.paas.foundation.ssl.SSLOptionFactory;
import com.huawei.paas.foundation.vertx.AsyncResultCallback;
import com.huawei.paas.foundation.vertx.VertxTLSBuilder;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年2月9日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TcpServer {
    private URIEndpointObject endpointObject;

    /**
     * 构造
     * @param endpointObject               server addr
     */
    public TcpServer(URIEndpointObject endpointObject) {
        this.endpointObject = endpointObject;
    }

    public void init(Vertx vertx, String sslKey, AsyncResultCallback<InetSocketAddress> callback) {
        NetServer netServer;
        if (endpointObject.isSslEnabled()) {
            SSLOptionFactory factory =
                SSLOptionFactory.createSSLOptionFactory(sslKey, null);
            SSLOption sslOption;
            if (factory == null) {
                sslOption = SSLOption.buildFromYaml(sslKey);
            } else {
                sslOption = factory.createSSLOption();
            }
            SSLCustom sslCustom = SSLCustom.createSSLCustom(sslOption.getSslCustomClass());
            NetServerOptions serverOptions = new NetServerOptions();
            VertxTLSBuilder.buildNetServerOptions(sslOption, sslCustom, serverOptions);
            netServer = vertx.createNetServer(serverOptions);
        } else {
            netServer = vertx.createNetServer();
        }

        netServer.connectHandler(netSocket -> {
            TcpServerConnection connection = createTcpServerConnection();
            connection.init(netSocket);
        });

        InetSocketAddress socketAddress = endpointObject.getSocketAddress();
        netServer.listen(socketAddress.getPort(), socketAddress.getHostString(), ar -> {
            if (ar.succeeded()) {
                callback.success(socketAddress);
                return;
            }

            // 监听失败
            String msg = String.format("listen failed, address=%s", socketAddress.toString());
            callback.fail(new Exception(msg, ar.cause()));
        });
    }

    protected TcpServerConnection createTcpServerConnection() {
        return new TcpServerConnection();
    }
}
