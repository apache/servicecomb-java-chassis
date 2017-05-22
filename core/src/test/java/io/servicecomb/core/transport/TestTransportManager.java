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

import java.util.ArrayList;
import java.util.List;

import io.servicecomb.core.Transport;
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.core.Endpoint;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestTransportManager {
    @Test
    public void testTransportManagerInitFail(@Injectable Transport transport) throws Exception {
        new Expectations() {
            {
                transport.getName();
                result = "test";
                transport.init();
                result = false;
            }
        };
        List<Transport> transports = new ArrayList<>();
        transports.add(transport);

        TransportManager manager = new TransportManager();
        Deencapsulation.setField(manager, "transportList", transports);

        manager.init();
        Assert.assertEquals(manager.findTransport("test"), transport);
    }

    @Test
    public void testTransportManagerInitSucc(@Injectable Transport transport, @Injectable Endpoint endpoint,
            @Mocked RegistryUtils util, @Injectable MicroserviceInstance instance) throws Exception {
        List<Endpoint> endpoints = new ArrayList<>();
        new Expectations() {
            {
                transport.getName();
                result = "test";
                transport.init();
                result = true;
                transport.getPublishEndpoint();
                result = endpoint;
                endpoint.getEndpoint();
                result = "http://local:90";
                RegistryUtils.getMicroserviceInstance();
                result = instance;
                instance.getEndpoints();
                result = endpoints;
            }
        };
        List<Transport> transports = new ArrayList<>();
        transports.add(transport);

        TransportManager manager = new TransportManager();
        Deencapsulation.setField(manager, "transportList", transports);

        manager.init();
        Assert.assertEquals(manager.findTransport("test"), transport);
    }
}
