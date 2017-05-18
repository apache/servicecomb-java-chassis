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

package com.huawei.paas.cse.serviceregistry;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.huawei.paas.cse.serviceregistry.api.registry.HealthCheck;
import com.huawei.paas.cse.serviceregistry.api.registry.HealthCheckMode;
import com.huawei.paas.cse.serviceregistry.api.registry.Microservice;
import com.huawei.paas.cse.serviceregistry.api.registry.MicroserviceInstance;
import com.huawei.paas.cse.serviceregistry.api.registry.MicroserviceInstanceStatus;
import com.huawei.paas.cse.serviceregistry.api.registry.MicroserviceStatus;
import com.huawei.paas.cse.serviceregistry.api.response.HeartbeatResponse;
import com.huawei.paas.cse.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import com.huawei.paas.cse.serviceregistry.cache.CacheRegistryListener;
import com.huawei.paas.cse.serviceregistry.client.http.ServiceRegistryClientImpl;
import com.huawei.paas.cse.serviceregistry.notify.NotifyManager;
import com.huawei.paas.cse.serviceregistry.notify.RegistryEvent;
import com.huawei.paas.cse.serviceregistry.utils.Timer;
import com.huawei.paas.cse.serviceregistry.utils.TimerException;
import com.huawei.paas.foundation.common.utils.BeanUtils;
import com.huawei.paas.foundation.common.utils.Log4jUtils;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRegistry {

    /**
     * Test RegistryUtils
     * @throws Exception
     */
    @BeforeClass
    public static void initSetup() throws Exception {
        Log4jUtils.init();
        BeanUtils.init();
        RegistryUtils.setSrClient(null);
    }

    @Test
    public void testRegistryUtils() throws Exception {
        Microservice oInstance = RegistryUtils.getMicroservice();
        List<String> schemas = new ArrayList<>();
        schemas.add("testSchema");
        oInstance.setSchemas(schemas);
        oInstance.setServiceId("testServiceId");
        oInstance.setStatus(MicroserviceStatus.UNKNOWN.name());
        Map<String, String> properties = new HashMap<>();
        properties.put("proxy", "testPorxy");
        oInstance.setProperties(properties);

        Assert.assertEquals("default", oInstance.getServiceName());
        Assert.assertEquals("default", oInstance.getAppId());
        Assert.assertEquals("", oInstance.getDescription());
        Assert.assertEquals("FRONT", oInstance.getLevel());
        Assert.assertEquals("testPorxy", oInstance.getProperties().get("proxy"));
        Assert.assertEquals("testServiceId", oInstance.getServiceId());
        Assert.assertEquals("0.0.1", oInstance.getVersion());
        Assert.assertEquals(1, oInstance.getSchemas().size());
        Assert.assertEquals(MicroserviceStatus.UNKNOWN.name(), oInstance.getStatus());

        RegistryUtils.getMicroserviceInstance().setHostName("test");
        RegistryUtils.getMicroserviceInstance().setServiceId("testServiceID");
        RegistryUtils.getMicroserviceInstance().setInstanceId("testID");
        RegistryUtils.getMicroserviceInstance().setStage("testStage");

        List<String> endpoints = new ArrayList<>();
        endpoints.add("localhost");

        RegistryUtils.getMicroserviceInstance().setEndpoints(endpoints);
        RegistryUtils.getMicroserviceInstance().setStatus(MicroserviceInstanceStatus.STARTING);
        RegistryUtils.getMicroserviceInstance().setProperties(properties);

        HealthCheck oHealthCheck = new HealthCheck();
        oHealthCheck.setInterval(10);
        oHealthCheck.setPort(8080);
        oHealthCheck.setTimes(20);
        HealthCheckMode oHealthCheckMode = HealthCheckMode.PLATFORM;
        oHealthCheck.setMode(oHealthCheckMode);

        RegistryUtils.getMicroserviceInstance().setHealthCheck(oHealthCheck);

        Assert.assertEquals("test", RegistryUtils.getMicroserviceInstance().getHostName());
        Assert.assertEquals("testServiceID", RegistryUtils.getMicroserviceInstance().getServiceId());
        Assert.assertEquals("testID", RegistryUtils.getMicroserviceInstance().getInstanceId());
        Assert.assertEquals(endpoints, RegistryUtils.getMicroserviceInstance().getEndpoints());
        Assert.assertEquals(MicroserviceInstanceStatus.STARTING, RegistryUtils.getMicroserviceInstance().getStatus());
        Assert.assertEquals(10, RegistryUtils.getMicroserviceInstance().getHealthCheck().getInterval());
        Assert.assertEquals(8080, RegistryUtils.getMicroserviceInstance().getHealthCheck().getPort());
        Assert.assertEquals(20, RegistryUtils.getMicroserviceInstance().getHealthCheck().getTimes());
        Assert.assertEquals("pull", RegistryUtils.getMicroserviceInstance().getHealthCheck().getMode().getName());
        Assert.assertEquals(0, RegistryUtils.getMicroserviceInstance().getHealthCheck().getTTL());
        RegistryUtils.getMicroserviceInstance().getHealthCheck().setMode(HealthCheckMode.HEARTBEAT);
        Assert.assertNotEquals(0, RegistryUtils.getMicroserviceInstance().getHealthCheck().getTTL());
        Assert.assertEquals("testPorxy", RegistryUtils.getMicroserviceInstance().getProperties().get("proxy"));
        Assert.assertEquals("testStage", RegistryUtils.getMicroserviceInstance().getStage());

    }

    /**
    * Test With Stubs
    * @throws Exception
    * @author  
    * @since Mar 7, 2017 
    */
    @Test
    public void testRegistryUtilsWithStub(
            final @Mocked ServiceRegistryClientImpl oMockServiceRegistryClient) throws Exception {
        HeartbeatResponse response = new HeartbeatResponse();
        response.setOk(true);
        response.setMessage("OK");

        new Expectations() {
            {
                oMockServiceRegistryClient.init();
                oMockServiceRegistryClient.registerMicroservice((Microservice) any);
                result = "sampleServiceID";
                oMockServiceRegistryClient.registerMicroserviceInstance((MicroserviceInstance) any);
                result = "sampleInstanceID";
                oMockServiceRegistryClient.unregisterMicroserviceInstance(anyString, anyString);
                result = true;
            }
        };

        RegistryUtils.setSrClient(oMockServiceRegistryClient);
        RegistryUtils.init();
        Assert.assertEquals(true, RegistryUtils.unregsiterInstance());
    }

    @Test
    public void testRegistryUtilsWithStubFailure(
            final @Mocked ServiceRegistryClientImpl oMockServiceRegistryClient) throws Exception {
        new Expectations() {
            {
                oMockServiceRegistryClient.init();
                oMockServiceRegistryClient.registerMicroservice((Microservice) any);
                result = "sampleServiceID";
                oMockServiceRegistryClient.registerMicroserviceInstance((MicroserviceInstance) any);
                result = "sampleInstanceID";
                oMockServiceRegistryClient.unregisterMicroserviceInstance(anyString, anyString);
                result = false;
            }
        };

        RegistryUtils.setSrClient(oMockServiceRegistryClient);
        RegistryUtils.init();
        Assert.assertEquals(false, RegistryUtils.unregsiterInstance());
    }

    @Test
    public void testRegistryUtilsWithStubHeartbeat(
            final @Mocked ServiceRegistryClientImpl oMockServiceRegistryClient) throws Exception {
        HeartbeatResponse response = new HeartbeatResponse();
        response.setOk(true);
        response.setMessage("OK");

        new Expectations() {
            {
                oMockServiceRegistryClient.init();
                oMockServiceRegistryClient.registerMicroservice((Microservice) any);
                result = "sampleServiceID";
                oMockServiceRegistryClient.registerMicroserviceInstance((MicroserviceInstance) any);
                result = "sampleInstanceID";
                oMockServiceRegistryClient.heartbeat(anyString, anyString);
                result = response;
                oMockServiceRegistryClient.unregisterMicroserviceInstance(anyString, anyString);
                result = false;
            }
        };

        RegistryUtils.setSrClient(oMockServiceRegistryClient);
        RegistryUtils.init();
        Assert.assertEquals(true, RegistryUtils.heartbeat().isOk());
        Assert.assertEquals(false, RegistryUtils.unregsiterInstance());
    }

    @Test
    public void testRegistryUtilsWithStubHeartbeatFailure(
            final @Mocked ServiceRegistryClientImpl oMockServiceRegistryClient) throws Exception {
        final HeartbeatResponse response = new HeartbeatResponse();
        response.setOk(false);
        response.setMessage("FAIL");

        new Expectations() {
            {
                oMockServiceRegistryClient.init();
                oMockServiceRegistryClient.registerMicroservice((Microservice) any);
                result = "sampleServiceID";
                oMockServiceRegistryClient.registerMicroserviceInstance((MicroserviceInstance) any);
                result = "sampleInstanceID";
                oMockServiceRegistryClient.heartbeat(anyString, anyString);
                result = response;
                oMockServiceRegistryClient.unregisterMicroserviceInstance(anyString, anyString);
                result = false;
            }
        };

        RegistryUtils.setSrClient(oMockServiceRegistryClient);
        RegistryUtils.init();
        Assert.assertEquals(false, RegistryUtils.heartbeat().isOk());
        Assert.assertEquals(false, RegistryUtils.unregsiterInstance());
    }

    @Test
    public void testAllowCrossApp() {
        Map<String, String> propertiesMap = new HashMap<>();
        Assert.assertFalse(RegistryUtils.allowCrossApp(propertiesMap));

        propertiesMap.put("allowCrossApp", "true");
        Assert.assertTrue(RegistryUtils.allowCrossApp(propertiesMap));

        propertiesMap.put("allowCrossApp", "false");
        Assert.assertFalse(RegistryUtils.allowCrossApp(propertiesMap));

        propertiesMap.put("allowCrossApp", "asfas");
        Assert.assertFalse(RegistryUtils.allowCrossApp(propertiesMap));
    }

    @Test
    public void testInit() {
        boolean validAssert = true;
        try {
            RegistryUtils.init();
        } catch (Exception e) {
            validAssert = false;
        }
        Assert.assertTrue(validAssert);

    }

    @Test
    public void testRegsiterInstanceEmpty(@Mocked ServiceRegistryClientImpl oMockServiceRegistryClient) {
        RegistryUtils.getMicroserviceInstance().setHostName("test");
        RegistryUtils.getMicroserviceInstance().setServiceId("testServiceID");
        RegistryUtils.getMicroserviceInstance().setInstanceId("testID");
        RegistryUtils.getMicroserviceInstance().setStage("testStage");

        new Expectations() {
            {

                oMockServiceRegistryClient.registerMicroserviceInstance((MicroserviceInstance) any);
                result = "";

            }
        };
        try {
            boolean assertValid = RegistryUtils.regsiterInstance();
            Assert.assertFalse(assertValid);

        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testregsiterMicroserviceServiceIdEmpty(@Mocked ServiceRegistryClientImpl oMockServiceRegistryClient) {
        RegistryUtils.getMicroserviceInstance().setHostName("test");
        RegistryUtils.getMicroserviceInstance().setServiceId("testServiceID");
        RegistryUtils.getMicroserviceInstance().setInstanceId("testID");
        RegistryUtils.getMicroserviceInstance().setStage("testStage");
        new Expectations() {
            {
                oMockServiceRegistryClient.getMicroserviceId(RegistryUtils.getMicroservice().getAppId(),
                        RegistryUtils.getMicroservice().getServiceName(),
                        RegistryUtils.getMicroservice().getVersion());
                result = "test";

            }
        };
        boolean validAssert = true;
        try {
            RegistryUtils.init();
        } catch (Exception e) {
            validAssert = false;
        }
        Assert.assertTrue(validAssert);
    }

    @Test
    public void testRegistryUtilsWithStubHeartbeatFailureException(
            final @Mocked ServiceRegistryClientImpl oMockServiceRegistryClient) throws Exception {
        HeartbeatResponse response = new HeartbeatResponse();
        response.setOk(true);
        response.setMessage("OK");
        try {
            new Expectations() {
                {
                    oMockServiceRegistryClient.init();
                    oMockServiceRegistryClient.registerMicroservice((Microservice) any);
                    result = "sampleServiceID";
                    oMockServiceRegistryClient.registerMicroserviceInstance((MicroserviceInstance) any);
                    result = "sampleInstanceID";
                    oMockServiceRegistryClient.heartbeat(anyString, anyString);
                    result = null;

                }
            };

            RegistryUtils.setSrClient(oMockServiceRegistryClient);
            RegistryUtils.init();

            new MockUp<Timer>() {
                @Mock
                public void sleep() throws TimerException {
                    throw new TimerException();
                }
            };

            boolean validAssert = RegistryUtils.heartbeat().isOk();
            Assert.assertTrue(validAssert);
        } catch (Exception e) {
            Assert.assertEquals("java.lang.NullPointerException", e.getClass().getName());
        }

    }

    @Test
    public void testFindServiceInstance() {
        List<MicroserviceInstance> microserviceInstanceList =
            RegistryUtils.findServiceInstance("appId", "serviceName", "versionRule");
        Assert.assertNull(microserviceInstanceList);
    }

    @Test
    public void testFindServiceInstanceWithMicroServiceInstance() {
        List<MicroserviceInstance> microserviceInstanceList = new ArrayList<MicroserviceInstance>();
        microserviceInstanceList.add(new MicroserviceInstance());

        new MockUp<ServiceRegistryClientImpl>() {
            @Mock
            List<MicroserviceInstance> findServiceInstance(String selfMicroserviceId, String appId,
                    String serviceName,
                    String versionRule) {
                return microserviceInstanceList;

            }
        };

        List<MicroserviceInstance> microserviceInstanceListt =
            RegistryUtils.findServiceInstance("appId", "serviceName", "versionRule");
        Assert.assertNotNull(microserviceInstanceListt);
    }

    @Test
    public void testNotifyRegistryEventINSTANCE_CHANGED() {
        boolean status = true;
        new MockUp<CacheRegistryListener>() {
            @Mock
            public void onMicroserviceInstanceChanged(MicroserviceInstanceChangedEvent changedEvent) {
            }
        };
        try {
            NotifyManager.INSTANCE.notifyListeners(RegistryEvent.INSTANCE_CHANGED, null);
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testNotifyRegistryEventINSTANCE_CHANGEDWITHEXCEPTION() {
        boolean status = true;
        try {
            NotifyManager.INSTANCE.notifyListeners(RegistryEvent.INSTANCE_CHANGED, null);
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);

    }

    @Test
    public void testNotifyRegistryEventEXCEPTION() {
        boolean status = true;
        new MockUp<CacheRegistryListener>() {
            @Mock
            public void onMicroserviceInstanceChanged(MicroserviceInstanceChangedEvent changedEvent) {
            }
        };
        try {
            NotifyManager.INSTANCE.notifyListeners(RegistryEvent.EXCEPTION, null);
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testRegistryUtilGetPublishAddress() {
        String address = RegistryUtils.getPublishAddress();
        Assert.assertNotNull(address);
        System.setProperty("cse.service.publishAddress", "{eth0}");
        address = RegistryUtils.getPublishAddress();
        Assert.assertNotNull(address);
    }

    @Test
    public void testRegistryUtilGetHostName() {
        String host = RegistryUtils.getPublishHostName();
        Assert.assertNotNull(host);
    }

    @Test
    public void testgetRealListenAddress() throws Exception {
        Assert.assertEquals(RegistryUtils.getPublishAddress("rest", "172.0.0.0:8080"), "rest://172.0.0.0:8080");
        Assert.assertEquals(RegistryUtils.getPublishAddress("rest", null), null);
        URI uri = new URI(RegistryUtils.getPublishAddress("rest", "0.0.0.0:8080"));
        Assert.assertNotEquals(uri.getAuthority(), "0.0.0.0:8080");
    }

    @Test
    public void testUpdateInstanceProperties() {
        MicroserviceInstance instance = RegistryUtils.getMicroserviceInstance();
        instance.setServiceId("1");
        instance.setInstanceId("1");
        new MockUp<ServiceRegistryClientImpl>() {
            @Mock
            public boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId,
                    Map<String, String> instanceProperties) {
                return true;
            }
        };
        Assert.assertEquals(1, instance.getProperties().size());
        Map<String, String> properties = new HashMap<>();
        properties.put("tag1", "value1");
        RegistryUtils.updateInstanceProperties(properties);
        Assert.assertEquals(properties, instance.getProperties());

        new MockUp<ServiceRegistryClientImpl>() {
            @Mock
            public boolean updateInstanceProperties(String microserviceId, String microserviceInstanceId,
                    Map<String, String> instanceProperties) {
                return false;
            }
        };
        RegistryUtils.updateInstanceProperties(new HashMap<>());
        Assert.assertEquals(properties, instance.getProperties());
    }

}
