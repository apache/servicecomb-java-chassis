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

import io.vertx.core.net.NetClientOptions;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年2月17日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TcpClientConfig extends NetClientOptions {
    private static final int DEFAULT_TIMEOUT = 30000;

    private long msRequestTimeout;

    private TcpLogin tcpLogin;

    public TcpClientConfig() {
        // 30 second
        msRequestTimeout = DEFAULT_TIMEOUT;
    }

    public long getRequestTimeoutMillis() {
        return msRequestTimeout;
    }

    public void setRequestTimeoutMillis(long msTimeout) {
        this.msRequestTimeout = msTimeout;
    }

    public TcpLogin getTcpLogin() {
        return tcpLogin;
    }

    public void setTcpLogin(TcpLogin tcpLogin) {
        this.tcpLogin = tcpLogin;
    }
}
