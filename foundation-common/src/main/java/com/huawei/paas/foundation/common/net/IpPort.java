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

package com.huawei.paas.foundation.common.net;

import java.net.InetSocketAddress;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年12月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class IpPort {
    private String hostOrIp;

    private int port;

    private volatile InetSocketAddress socketAddress;

    private final Object lock = new Object();

    /**
     * <构造函数> [参数说明]
     */
    public IpPort() {

    }

    /**
     * <构造函数> [参数说明]
     */
    public IpPort(String hostOrIp, int port) {
        this.hostOrIp = hostOrIp;
        this.port = port;
    }

    /**
     * 获取hostOrIp的值
     * @return 返回 hostOrIp
     */
    public String getHostOrIp() {
        return hostOrIp;
    }

    public void setHostOrIp(String hostOrIp) {
        this.hostOrIp = hostOrIp;
    }

    /**
     * 获取port的值
     * @return 返回 port
     */
    public int getPort() {
        return port;
    }

    /**
     * 对port进行赋值
     * @param port port的新值
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 获取socketAddress的值
     * @return 返回 socketAddress
     */
    public InetSocketAddress getSocketAddress() {
        if (socketAddress == null) {
            synchronized (lock) {
                if (socketAddress == null) {
                    InetSocketAddress tmpSocketAddress = new InetSocketAddress(hostOrIp, port);
                    socketAddress = tmpSocketAddress;
                }
            }
        }

        return socketAddress;
    }
}
