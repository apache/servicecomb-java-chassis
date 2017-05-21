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

package io.servicecomb.core.transport;

import java.util.Map;
import java.util.Map.Entry;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.core.Endpoint;
import io.servicecomb.core.Transport;
import io.servicecomb.foundation.common.net.NetUtils;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.foundation.vertx.VertxUtils;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.vertx.core.Vertx;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年12月3日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class AbstractTransport implements Transport {
    /**
     * 用于参数传递：比如王RestServerVerticle传递endpoint地址。
     */
    public static final String ENDPOINT_KEY = "cse.endpoint";

    private static final long DEFAULT_TIMEOUT_MILLIS = 30000;

    private static Long msReqeustTimeout = null;

    public static long getRequestTimeout() {
        if (msReqeustTimeout != null) {
            return msReqeustTimeout;
        }

        long msTimeout = DynamicPropertyFactory.getInstance()
                .getLongProperty("cse.request.timeout", DEFAULT_TIMEOUT_MILLIS)
                .get();
        if (msTimeout <= 0) {
            msTimeout = DEFAULT_TIMEOUT_MILLIS;
        }

        msReqeustTimeout = msTimeout;
        return msReqeustTimeout;
    }

    // 所有transport使用同一个vertx实例，避免创建太多的线程
    protected Vertx transportVertx = VertxUtils.getOrCreateVertxByName("transport", null);

    protected Endpoint endpoint;

    protected Endpoint publishEndpoint;

    /**
     * {@inheritDoc}
     */
    @Override
    public Endpoint getPublishEndpoint() {
        return publishEndpoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Endpoint getEndpoint() throws Exception {
        return endpoint;
    }

    protected void setListenAddressWithoutSchema(String addressWithoutSchema) {
        setListenAddressWithoutSchema(addressWithoutSchema, null);
    }

    /**
     * 将配置的URI转换为endpoint
     * @param addressWithoutSchema 配置的URI，没有schema部分
     */
    protected void setListenAddressWithoutSchema(String addressWithoutSchema,
            Map<String, String> pairs) {
        if (addressWithoutSchema != null && pairs != null && !pairs.isEmpty()) {
            int idx = addressWithoutSchema.indexOf('?');
            if (idx == -1) {
                addressWithoutSchema += "?";
            } else {
                addressWithoutSchema += "&";
            }

            StringBuilder sb = new StringBuilder();
            for (Entry<String, String> entry : pairs.entrySet()) {
                sb.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
            }
            sb.setLength(sb.length() - 1);
            addressWithoutSchema += sb.toString();
        }
        this.endpoint = new Endpoint(this, NetUtils.getRealListenAddress(getName(), addressWithoutSchema));
        if (this.endpoint.getEndpoint() != null) {
            this.publishEndpoint = new Endpoint(this, RegistryUtils.getPublishAddress(getName(), 
                    addressWithoutSchema));
        } else {
            this.publishEndpoint = null;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object parseAddress(String address) {
        if (address == null) {
            return null;
        }
        return new URIEndpointObject(address);
    }
}
