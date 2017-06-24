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
import org.mockito.Mockito;

import io.servicecomb.config.ConfigUtil;
import io.servicecomb.serviceregistry.RegistryThread;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.client.ClientException;
import io.servicecomb.serviceregistry.client.RegistryClientFactory;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestServiceRegistryClientImpl {

    private ServiceRegistryClientImpl oClient = null;

    @Before
    public void setUp() throws Exception {
        ConfigUtil.installDynamicConfig();
        oClient = (ServiceRegistryClientImpl) RegistryClientFactory.getRegistryClient();
    }

    @After
    public void tearDown() throws Exception {
        oClient = null;
    }

    @Test
    public void testPrivateMehtodCreateHttpClientOptions() {

        Microservice oInstance = RegistryUtils.getMicroservice();
        oClient.registerMicroservice(oInstance);
        oClient.registerMicroserviceInstance(RegistryUtils.getMicroserviceInstance());
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

        Microservice oInstance = RegistryUtils.getMicroservice();
        RegistryThread oThread = new RegistryThread();
        oThread.start();
        oClient.registerMicroservice(oInstance);
        oClient.registerMicroserviceInstance(RegistryUtils.getMicroserviceInstance());
        oClient.init();
        new MockUp<CountDownLatch>() {
            @Mock
            public void await() throws InterruptedException {
                throw new InterruptedException();
            }

        };
        Assert.assertEquals(null,
                oClient.getMicroserviceId(RegistryUtils.getMicroservice().getAppId(),
                        RegistryUtils.getMicroservice().getServiceName(),
                        RegistryUtils.getMicroservice().getVersion()));
        Assert.assertThat(oClient.getAllMicroservices().isEmpty(), is(true));
        Assert.assertEquals(null, oClient.registerMicroservice(RegistryUtils.getMicroservice()));
        Assert.assertEquals(null, oClient.getMicroservice("microserviceId"));
        Assert.assertEquals(null, oClient.getMicroserviceInstance("consumerId", "providerId"));
        Assert.assertEquals(false,
                oClient.unregisterMicroserviceInstance("microserviceId", "microserviceInstanceId"));
        Assert.assertEquals(null, oClient.heartbeat("microserviceId", "microserviceInstanceId"));
        Assert.assertEquals(null,
                oClient.findServiceInstance("selfMicroserviceId", "appId", "serviceName", "versionRule"));
        MicroserviceInstance microInstance = Mockito.mock(MicroserviceInstance.class);
        Assert.assertEquals(null, oClient.registerMicroserviceInstance(microInstance));

        Assert.assertEquals("a", new ClientException("a").getMessage());
    }
}
