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

package io.servicecomb.core;

public class Endpoint {
    // 格式：grpc://192.168.1.1:8080
    // see: http://www.ietf.org/rfc/rfc2396.txt
    private final String endpoint;

    private final Transport transport;

    // 内部格式， 只有Transport能够认识
    private final Object address;

    public Endpoint(Transport transport, String endpoint) {
        this.transport = transport;
        this.endpoint = endpoint;
        this.address = transport.parseAddress(this.endpoint);
    }

    /**
     * 获取endpoint的值
     * @return 返回 endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * 获取transport的值
     * @return 返回 transport
     */
    public Transport getTransport() {
        return transport;
    }

    /**
     * 获取address的值
     * @return 返回 address
     */
    public Object getAddress() {
        return address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return endpoint;
    }
}
