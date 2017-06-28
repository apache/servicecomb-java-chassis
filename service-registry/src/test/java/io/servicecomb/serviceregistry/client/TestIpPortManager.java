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

package io.servicecomb.serviceregistry.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.cache.CacheEndpoint;
import io.servicecomb.serviceregistry.cache.InstanceCache;
import io.servicecomb.serviceregistry.registry.AbstractServiceRegistry;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestIpPortManager {
    @Mocked
    ServiceRegistryClient srClient;

    AbstractServiceRegistry serviceRegistry;

    IpPortManager manager;

    @Before
    public void setup() {
        serviceRegistry = (AbstractServiceRegistry) ServiceRegistryFactory.createLocal();
        serviceRegistry.setServiceRegistryClient(srClient);
        serviceRegistry.init();

        manager = serviceRegistry.getIpPortManager();
    }

    InstanceCache instanceCache = null;

    Map<String, List<String>> oListTransportMap = null;

    @Test
    public void testCreateServiceRegistryCache() {
        List<MicroserviceInstance> list = new ArrayList<MicroserviceInstance>();
        MicroserviceInstance e1 = new MicroserviceInstance();
        List<String> endpoints = new ArrayList<>();
        endpoints.add("rest://127.0.0.1:8080");
        e1.setEndpoints(endpoints);
        list.add(e1);

        new Expectations() {
            {
                srClient.findServiceInstance(anyString, anyString, anyString, anyString);
                result = list;
            }
        };

        try {
            manager.createServiceRegistryCache();
            Assert.assertNotNull(manager.get());
            Assert.assertNull(manager.next());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testCreateServiceRegistryCacheWithInstanceCache() {

        boolean validAssert = true;

        List<MicroserviceInstance> list = new ArrayList<MicroserviceInstance>();
        MicroserviceInstance e1 = new MicroserviceInstance();
        list.add(e1);

        new MockUp<RegistryUtils>() {

            @Mock
            public List<MicroserviceInstance> findServiceInstance(String appId, String serviceName,
                    String versionRule) {
                return list;

            }
        };

        try {
            Deencapsulation.setField(manager, "instanceCache", Mockito.mock(InstanceCache.class));
            manager.createServiceRegistryCache();
            Assert.assertNotNull(manager.get());
            Assert.assertNull(manager.next());
        } catch (Exception e) {

            validAssert = false;
        }

        Assert.assertTrue(validAssert);
    }

    @Test
    public void testGetDefaultIpPortAddressesisNUll() {
        new MockUp<IpPortManager>() {
            @Mock
            public ArrayList<IpPort> getDefaultIpPortList() {
                return null;

            }
        };
        Assert.assertNull(manager.getDefaultIpPort());;
    }

    @Test
    public void testNextDefaultIpPortAddressesisNUll() {
        new MockUp<IpPortManager>() {
            @Mock
            public ArrayList<IpPort> getDefaultIpPortList() {
                return null;

            }
        };
        Assert.assertNull(manager.nextDefaultIpPort());
    }

    @Test
    public void testNextDefaultIpPortAddressesSize() {

        new MockUp<IpPortManager>() {
            @Mock
            public ArrayList<IpPort> getDefaultIpPortList() {
                List<IpPort> ipPorts = new ArrayList<IpPort>();
                IpPort ipport1 = new IpPort();
                ipport1.setHostOrIp("hostOrIp");
                IpPort ipport2 = new IpPort();
                ipport2.setHostOrIp("hostOrIp");
                ipPorts.add(ipport1);
                ipPorts.add(ipport2);
                return (ArrayList<IpPort>) ipPorts;

            }
        };
        IpPort ipPort = manager.nextDefaultIpPort();
        Assert.assertEquals("hostOrIp", ipPort.getHostOrIp());
    }

    @Test
    public void testNextDefaultIpPortAddresses() {

        Assert.assertNull(manager.nextDefaultIpPort());
    }

    @Test
    public void testNextAddressesSize() {

        new MockUp<IpPortManager>() {
            @Mock
            public List<CacheEndpoint> getAddressCaches() {
                List<CacheEndpoint> addressCaches = new ArrayList<>();
                addressCaches.add(new CacheEndpoint("http://127.0.0.3:8080", null));
                addressCaches.add(new CacheEndpoint("http://127.0.0.3:8080", null));
                return addressCaches;
            }
        };

        Map<Integer, Boolean> addressCanUsed = new HashMap<Integer, Boolean>();
        addressCanUsed.get(0);
        addressCanUsed.put(0, true);
        Deencapsulation.setField(manager, "addressCanUsed", addressCanUsed);
        Assert.assertEquals(manager.next().getHostOrIp(), "127.0.0.3");
    }

}
