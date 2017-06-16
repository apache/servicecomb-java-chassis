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

import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.config.ConfigUtil;
import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.foundation.vertx.AsyncResultCallback;
import io.servicecomb.serviceregistry.RegistryThread;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.client.Endpoints;
import io.servicecomb.serviceregistry.client.IpPortManager;
import io.servicecomb.serviceregistry.client.RegistryClientFactory;
import mockit.Expectations;
import mockit.Mocked;

public class TestClienthttp {

    @Before
    public void setUp() throws Exception {
        ConfigUtil.installDynamicConfig();
    }

    @After
    public void tearDown() throws Exception {
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testServiceRegistryClientImpl(@Mocked IpPortManager manager) {
        IpPort ipPort = new IpPort("127.0.0.1", 8853);
        new Expectations() {
            {
                manager.get();
                result = ipPort;
                manager.next();
                result = null;
            }
        };

        Microservice oInstance = RegistryUtils.getMicroservice();
        RegistryThread oThread = new RegistryThread();
        oThread.start();
        ServiceRegistryClientImpl oClient = (ServiceRegistryClientImpl) RegistryClientFactory.getRegistryClient();
        oClient.registerMicroservice(oInstance);
        oClient.registerMicroserviceInstance(RegistryUtils.getMicroserviceInstance());
        oClient.init();
        Assert.assertEquals(null, oClient.getMicroservice(RegistryUtils.getMicroservice().getServiceId()));
        Assert.assertEquals(null, oClient.getMicroserviceInstance("testConsumerID", "testproviderID"));
        Assert.assertEquals(null,
                oClient.findServiceInstance(RegistryUtils.getMicroservice().getServiceId(),
                        RegistryUtils.getMicroservice().getAppId(),
                        RegistryUtils.getMicroservice().getServiceName(),
                        RegistryUtils.getMicroservice().getVersion()));
        Assert.assertEquals(null,
                oClient.getMicroserviceId(RegistryUtils.getMicroservice().getAppId(),
                        RegistryUtils.getMicroservice().getServiceName(),
                        RegistryUtils.getMicroservice().getVersion()));
        Assert.assertEquals(null,
                oClient.heartbeat(RegistryUtils.getMicroservice().getServiceId(),
                        RegistryUtils.getMicroserviceInstance().getInstanceId()));
        oClient.watch("",
                Mockito.mock(AsyncResultCallback.class));
        Assert.assertEquals(false,
                oClient.unregisterMicroserviceInstance(RegistryUtils.getMicroservice().getServiceId(),
                        RegistryUtils.getMicroserviceInstance().getInstanceId()));
    }

    @Test
    public void testRequestContext() {
        RequestContext oContext = new RequestContext();
        oContext.setUri("//test");
        oContext.setMethod(io.vertx.core.http.HttpMethod.POST);
        oContext.setIpPort(new IpPort("145.0.0.1", 8080));
        oContext.setParams(null);

        Assert.assertEquals("//test", oContext.getUri());
        Assert.assertEquals(io.vertx.core.http.HttpMethod.POST, oContext.getMethod());
        Assert.assertEquals(8080, oContext.getIpPort().getPort());
        Assert.assertEquals(null, oContext.getParams());

        RestResponse oResponse = new RestResponse(null, null);
        oResponse.setRequestContext(oContext);
        Assert.assertEquals(oContext, oResponse.getRequestContext());
        Assert.assertEquals(null, oResponse.getResponse());

    }

    @Test
    public void testRequestParam() {
        RequestParam oParam = new RequestParam();
        oParam.setCookies(null);
        oParam.setBody(null);
        oParam.setHeaders(null);
        oParam.addHeader("testKey", "testValue");
        oParam.addQueryParam("testParam", "ValueParam");
        oParam.addQueryParam("testParam1", "ValueParam");
        Assert.assertEquals(null, oParam.getCookies());
        Assert.assertEquals(null, oParam.getBody());
        Assert.assertNotEquals(null, oParam.getHeaders());
        Assert.assertNotEquals(null, oParam.getQueryParams());
        oParam.setQueryParams(null);
        Assert.assertEquals("", oParam.getQueryParams());
        oParam.setFormFields(null);
        Assert.assertEquals(null, oParam.getFormFields());
        Endpoints oEndpoints = new Endpoints();
        oEndpoints.setInstances(null);
        oEndpoints.setVersion("1.0");
        Assert.assertEquals(null, oEndpoints.getInstances());
        Assert.assertEquals("1.0", oEndpoints.getVersion());

    }

    @Test
    public void testIpPortManager() throws Exception {
        IpPortManager oManager = new IpPortManager();
        ArrayList<IpPort> oIPPort = oManager.getDefaultIpPortList();
        oManager.next();
        Assert.assertEquals(oIPPort.get(0).getHostOrIp(), oManager.get().getHostOrIp());

        try {
            Assert.assertNull(oManager.next()); //This will return Null as the address cache is not able to get the address of the microservice which is registered above. 
        } catch (Exception e) {
            // TODO: Currently the address cache is failing because of absence of Thread
            // TODO: Need to find out a way to Assert it properly
            Assert.assertEquals("/ by zero", e.getMessage());
        }
    }

}
