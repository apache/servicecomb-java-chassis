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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.servicecomb.core.Endpoint;
import io.servicecomb.core.Transport;
import io.servicecomb.core.endpoint.AbstractEndpointsCache;
import io.servicecomb.serviceregistry.RegistryUtils;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年11月29日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class TransportManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportManager.class);

    @Inject
    private List<Transport> transportList;

    private Map<String, Transport> transportMap = new HashMap<>();

    public void init() throws Exception {
        AbstractEndpointsCache.setTransportManager(this);

        for (Transport transport : transportList) {
            transportMap.put(transport.getName(), transport);

            if (transport.init()) {
                Endpoint endpoint = transport.getPublishEndpoint();
                if (endpoint != null && endpoint.getEndpoint() != null) {
                    LOGGER.info("endpoint to publish: {}", endpoint.getEndpoint());
                    RegistryUtils.getMicroserviceInstance().getEndpoints().add(endpoint.getEndpoint());
                }
                continue;
            }
        }
    }

    public Transport findTransport(String transportName) {
        return transportMap.get(transportName);
    }
}
