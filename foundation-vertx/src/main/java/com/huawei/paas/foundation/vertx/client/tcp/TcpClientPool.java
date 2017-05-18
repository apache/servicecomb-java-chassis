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

package com.huawei.paas.foundation.vertx.client.tcp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.huawei.paas.foundation.vertx.tcp.TcpOutputStream;

import io.vertx.core.Context;
import io.vertx.core.net.NetClient;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年2月9日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TcpClientPool {
    // 是在哪个context中创建的
    private Context context;

    private TcpClientConfig clientConfig;

    private NetClient netClient;

    // key为address
    private Map<String, TcpClient> tcpClientMap = new ConcurrentHashMap<>();

    /**
     * <构造函数>
     * @param context
     * @param netClient
     * @param context [参数说明]
     */
    public TcpClientPool(TcpClientConfig clientConfig, Context context, NetClient netClient) {
        this.clientConfig = clientConfig;
        this.context = context;
        this.netClient = netClient;

        startCheckTimeout(clientConfig, context);
    }

    protected void startCheckTimeout(TcpClientConfig clientConfig, Context context) {
        context.owner().setPeriodic(clientConfig.getRequestTimeoutMillis(), this::onCheckTimeout);
    }

    private void onCheckTimeout(Long event) {
        for (TcpClient client : tcpClientMap.values()) {
            client.checkTimeout();
        }
    }

    public void send(String endpoint, TcpOutputStream os, TcpResonseCallback callback) {
        TcpClient tcpClient = findOrCreateClient(endpoint);

        tcpClient.send(os, clientConfig.getRequestTimeoutMillis(), callback);
    }

    private TcpClient findOrCreateClient(String endpoint) {
        TcpClient tcpClient = tcpClientMap.get(endpoint);
        if (tcpClient == null) {
            synchronized (this) {
                tcpClient = tcpClientMap.get(endpoint);
                if (tcpClient == null) {
                    tcpClient = create(endpoint);
                    tcpClientMap.put(endpoint, tcpClient);
                }
            }
        }

        return tcpClient;
    }

    protected TcpClient create(String endpoint) {
        return new TcpClient(context, netClient, endpoint, clientConfig);
    }
}
