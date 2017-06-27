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

package io.servicecomb.serviceregistry.client.http;

import static org.hamcrest.core.Is.is;

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceManager;
import io.servicecomb.serviceregistry.client.ClientException;
import io.servicecomb.serviceregistry.client.IpPortManager;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestServiceRegistryClientImpl {
    @Mocked
    private IpPortManager ipPortManager;

    private ServiceRegistryClientImpl oClient = null;

    @Before
    public void setUp() throws Exception {
        oClient = new ServiceRegistryClientImpl(ipPortManager);

        new MockUp<RestUtils>() {
            @Mock
            void httpDo(RequestContext requestContext, Handler<RestResponse> responseHandler) {
            }
        };

        new MockUp<CountDownLatch>() {
            @Mock
            public void await() throws InterruptedException {
            }

        };
    }

    @After
    public void tearDown() throws Exception {
        oClient = null;
    }

    @Test
    public void testPrivateMehtodCreateHttpClientOptions() {
        MicroserviceManager microserviceManager = new MicroserviceManager();
        Microservice microservice = microserviceManager.addMicroservice("app", "ms");
        oClient.registerMicroservice(microservice);
        oClient.registerMicroserviceInstance(microservice.getIntance());
        new MockUp<ServiceRegistryConfig>() {
            @Mock
            public HttpVersion getHttpVersion() {
                return HttpVersion.HTTP_2;
            }

            @Mock
            public boolean isSsl() {
                return true;
            }
        };
        try {
            oClient.init();
            HttpClientOptions httpClientOptions = Deencapsulation.invoke(oClient, "createHttpClientOptions");
            Assert.assertNotNull(httpClientOptions);
            Assert.assertEquals(80, httpClientOptions.getDefaultPort());
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testException() {
        MicroserviceManager microserviceManager = new MicroserviceManager();
        Microservice microservice = microserviceManager.addMicroservice("app", "ms");
        Assert.assertEquals(null, oClient.registerMicroservice(microservice));
        Assert.assertEquals(null, oClient.registerMicroserviceInstance(microservice.getIntance()));
        oClient.init();
        Assert.assertEquals(null,
                oClient.getMicroserviceId(microservice.getAppId(),
                        microservice.getServiceName(),
                        microservice.getVersion()));
        Assert.assertThat(oClient.getAllMicroservices().isEmpty(), is(true));
        Assert.assertEquals(null, oClient.registerMicroservice(microservice));
        Assert.assertEquals(null, oClient.getMicroservice("microserviceId"));
        Assert.assertEquals(null, oClient.getMicroserviceInstance("consumerId", "providerId"));
        Assert.assertEquals(false,
                oClient.unregisterMicroserviceInstance("microserviceId", "microserviceInstanceId"));
        Assert.assertEquals(null, oClient.heartbeat("microserviceId", "microserviceInstanceId"));
        Assert.assertEquals(null,
                oClient.findServiceInstance("selfMicroserviceId", "appId", "serviceName", "versionRule"));

        Assert.assertEquals("a", new ClientException("a").getMessage());
    }
}
